package com.lumiomedical.nlp;

import com.fasterxml.jackson.databind.JsonNode;
import com.lumiomedical.etl.ETL;
import com.lumiomedical.etl.extractor.http.BasicHttpClientExtractor;
import com.lumiomedical.etl.generator.IterableGenerator;
import com.lumiomedical.etl.loader.file.FileWriteString;
import com.lumiomedical.etl.transformer.Transformers;
import com.lumiomedical.etl.transformer.filesystem.CreateDirectory;
import com.lumiomedical.etl.transformer.http.BasicHttpClientTransformer;
import com.lumiomedical.etl.transformer.json.JsonArrayToCollectionTransformer;
import com.lumiomedical.etl.transformer.json.JsonArrayTransformer;
import com.lumiomedical.etl.transformer.json.JsonObjectTransformer;
import com.lumiomedical.flow.Flow;
import com.lumiomedical.flow.FlowOut;
import com.lumiomedical.flow.compiler.FlowCompiler;
import com.lumiomedical.flow.impl.parallel.ParallelCompiler;
import com.lumiomedical.flow.node.Node;
import com.lumiomedical.flow.stream.StreamOut;
import com.lumiomedical.nlp.data.Document;
import com.lumiomedical.nlp.data.Sentence;
import com.lumiomedical.nlp.data.Token;
import com.lumiomedical.nlp.etl.*;
import com.noleme.commons.container.Pair;
import com.noleme.json.Json;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.*;
import java.util.stream.Collectors;

import static com.lumiomedical.etl.transformer.Transformers.nonFatal;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/12/23
 */
public class SampleNLP extends ETL
{
    private final String outputPath;
    private final int parallelism;
    private final Locale locale;

    /**
     *
     * @param outputPath
     * @param parallelism
     * @param locale
     */
    public SampleNLP(String outputPath, int parallelism, Locale locale)
    {
        this.outputPath = outputPath;
        this.parallelism = parallelism;
        this.locale = locale;
    }

    @Override
    protected Collection<Node> provideFlows()
    {
        /* For each article title, we produce Document entities */
        StreamOut<String> titleFlow = streamTitles(outputPath, parallelism);
        StreamOut<Document> documentFlow = createDocument(titleFlow, locale);
        StreamOut<Document> processedFlow = processDocument(documentFlow, locale);

        /* We compile all documents and produce various stats */
        addStats(processedFlow, outputPath);
        addSize(processedFlow, outputPath);

        return List.of(processedFlow);
    }

    @Override
    protected FlowCompiler provideCompiler()
    {
        return new ParallelCompiler(this.parallelism, true);
    }

    /**
     *
     * @param url
     * @return
     */
    private static FlowOut<Set<String>> extractStopwords(String url)
    {
        return Flow
            .from(new BasicHttpClientExtractor(
                HttpRequest.newBuilder(URI.create(url)).build()
            ))
            .pipe(new JsonArrayTransformer())
            .pipe(new JsonArrayToCollectionTransformer<>(JsonNode::asText, Collectors.toSet()))
       ;
    }

    /**
     *
     * @param outputPath
     * @param parallelism
     * @return
     */
    private static StreamOut<String> streamTitles(String outputPath, int parallelism)
    {
        return Flow
            .<List<String>>from("titles")
            .pipe(ts -> {
                System.out.println("Compiling top 5 tokens for the following "+ts.size()+" wikipedia articles:");
                for (String title : ts)
                    System.out.println(" - "+title);
                return ts;
            })
            .pipe(new CreateDirectory<>(outputPath))
            .stream(IterableGenerator::new).setMaxParallelism(parallelism)
        ;
    }

    /**
     *
     * @param titleFlow
     * @param locale
     * @return
     */
    private static StreamOut<Document> createDocument(StreamOut<String> titleFlow, Locale locale)
    {
        return titleFlow
            .pipe(title -> HttpRequest.newBuilder(
                URI.create("https://"+locale.getLanguage()+".wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro&explaintext&redirects=1&titles="+title)
            ).build())
            .pipe(nonFatal(new BasicHttpClientTransformer()))
            .pipe(new JsonObjectTransformer())
            .pipe(Transformers.nonFatal(new WikipediaDocumentCreator()))
        ;
    }

    /**
     *
     * @param documentFlow
     * @param locale
     * @return
     */
    private static StreamOut<Document> processDocument(StreamOut<Document> documentFlow, Locale locale)
    {
        FlowOut<Set<String>> stopwordFlow = extractStopwords("https://raw.githubusercontent.com/6/stopwords-json/master/dist/"+locale.getLanguage()+".json");

        return documentFlow
            /* We run some regexes over the whole text in order to remove/clean-up some patterns */
            .pipe(new WikipediaTextCleaner())
            /* We produce sentences from the text */
            .pipe(new WikipediaSentenceSplitter(locale))
            .pipe(new WikipediaSentenceTokenizer())
            .pipe(new WikipediaTokenMapping(String::toLowerCase))
            /* We filter out stopwords */
            .join(stopwordFlow, (document, stopwords) -> {
                for (Sentence sentence : document.getSentences())
                    sentence.getTokens().removeIf(stopwords::contains);
                return document;
            })
        ;
    }

    /**
     *
     * @param documentFlow
     * @param outputPath
     */
    private static void addStats(StreamOut<Document> documentFlow, String outputPath)
    {
        documentFlow
            /* We produce a map of token frequencies for the document */
            .pipe(document -> {
                Map<String, Integer> frequency = new HashMap<>();
                for (Sentence sentence : document.getSentences())
                {
                    for (String token : sentence.getTokens())
                    {
                        if (!frequency.containsKey(token))
                            frequency.put(token, 1);
                        else
                            frequency.put(token, frequency.get(token) + 1);
                    }
                }
                return new Pair<>(document, frequency);
            })
            .accumulate()
            .pipe(documents -> {
                var json = Json.newObject();

                for (var document : documents)
                {
                    var documentNode = Json.newObject();

                    document.second.entrySet().stream()
                        .sorted((t1, t2) -> t2.getValue() - t1.getValue())
                        .limit(5)
                        .map(e -> new Token(e.getKey(), e.getValue()))
                        .forEach(token -> documentNode.put(token.value, token.frequency))
                    ;

                    json.set(document.first.getTitle(), documentNode);
                }

                return json;
            })
            .pipe(Json::prettyPrint)
            .sink(new FileWriteString(outputPath+"frequencies.json"))
        ;
    }

    /**
     *
     * @param documentFlow
     * @param outputPath
     */
    private static void addSize(StreamOut<Document> documentFlow, String outputPath)
    {
        documentFlow
            .accumulate()
            .pipe(docs -> {
                var json = Json.newObject();

                docs.stream()
                    .sorted((d1, d2) -> d2.getText().length() - d1.getText().length())
                    .forEach(doc -> json.put(doc.getTitle(), doc.getText().length()))
                ;

                return json;
            })
            .pipe(Json::prettyPrint)
            .sink(new FileWriteString(outputPath+"sizes.json"))
        ;
    }
}
