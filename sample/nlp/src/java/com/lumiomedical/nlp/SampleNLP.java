package com.lumiomedical.nlp;

import com.fasterxml.jackson.databind.JsonNode;
import com.lumiomedical.etl.ETL;
import com.lumiomedical.etl.extractor.http.BasicHttpStreamer;
import com.lumiomedical.etl.generator.IterableGenerator;
import com.lumiomedical.etl.loader.file.FileWriteJson;
import com.lumiomedical.etl.transformer.Transformers;
import com.lumiomedical.etl.transformer.filesystem.CreateDirectory;
import com.lumiomedical.etl.transformer.http.BasicHttpRequestStreamer;
import com.lumiomedical.etl.transformer.json.JsonArrayToCollection;
import com.lumiomedical.etl.transformer.json.ParseJsonArray;
import com.lumiomedical.etl.transformer.json.ParseJsonObject;
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
     * @param outputPath Directory path in which to output stats files
     * @param parallelism Max parallelism factor to use for the document stream
     * @param locale The reference locale to use for the Wikipedia API and NLP implementations
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
        StreamOut<String> titleFlow = streamTitles();
        StreamOut<Document> documentFlow = createDocument(titleFlow);
        StreamOut<Document> processedFlow = processDocument(documentFlow);

        /* We compile all documents and produce various stats */
        addStats(processedFlow);
        addSize(processedFlow);

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
            .from(new BasicHttpStreamer(
                HttpRequest.newBuilder(URI.create(url)).build()
            ))
            .pipe(new ParseJsonArray())
            .pipe(new JsonArrayToCollection<>(JsonNode::asText, Collectors.toSet()))
       ;
    }

    /**
     * At the end of this pipe segment, the input titles collection should be turned into a stream of titles that can be processed individually.
     *
     * @return A stream of Wikipedia article titles
     */
    private StreamOut<String> streamTitles()
    {
        return Flow
            .<List<String>>from("titles")
            .pipe(new CreateDirectory<>(this.outputPath))
            .stream(IterableGenerator::new).setMaxParallelism(this.parallelism)
        ;
    }

    /**
     * At the end of this pipe segment, Documents should be populated with their Wikipedia title and wikipedia introduction paragraph.
     *
     * @param titleFlow A stream of titles from which to derive Documents
     * @return A stream of Wikipedia Documents
     */
    private StreamOut<Document> createDocument(StreamOut<String> titleFlow)
    {
        return titleFlow
            .pipe(title -> HttpRequest.newBuilder(
                URI.create("https://"+this.locale.getLanguage()+".wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro&explaintext&redirects=1&titles="+title)
            ).build())
            .pipe(nonFatal(new BasicHttpRequestStreamer()))
            .pipe(new ParseJsonObject())
            .pipe(Transformers.nonFatal(new WikipediaDocumentCreator()))
        ;
    }

    /**
     * At the end of this pipe segment, Documents should be processed and have various properties resulting from their processing (sentences, tokens).
     *
     * @param documentFlow A stream of Documents to process
     * @return A stream of processed Wikipedia Documents
     */
    private StreamOut<Document> processDocument(StreamOut<Document> documentFlow)
    {
        FlowOut<Set<String>> stopwordFlow = extractStopwords("https://raw.githubusercontent.com/6/stopwords-json/master/dist/"+locale.getLanguage()+".json");

        return documentFlow
            /* We run some regexes over the whole text in order to remove/clean-up some patterns */
            .pipe(new WikipediaTextCleaner())
            /* We produce sentences from the text */
            .pipe(new WikipediaSentenceSplitter(this.locale))
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
     * @param documentFlow A stream of Documents from which to compute stats
     */
    private void addStats(StreamOut<Document> documentFlow)
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
            /* We accumulate all documents (and their token frequency map) and produce a JSON map with a top-5 of the most frequent tokens */
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
            .sink(new FileWriteJson<>(this.outputPath+"frequencies.json"))
        ;
    }

    /**
     *
     * @param documentFlow A stream of Documents from which to compute stats
     */
    private void addSize(StreamOut<Document> documentFlow)
    {
        /* We accumulate all produced Documents and produce JSON map containing each document's size */
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
            .sink(new FileWriteJson<>(this.outputPath+"sizes.json"))
        ;
    }
}
