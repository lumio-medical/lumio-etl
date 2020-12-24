package com.lumiomedical.crawl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lumiomedical.crawl.data.Page;
import com.lumiomedical.crawl.etl.PageLinkExtractor;
import com.lumiomedical.crawl.etl.PageLoader;
import com.lumiomedical.etl.ETL;
import com.lumiomedical.etl.generator.IterableGenerator;
import com.lumiomedical.etl.loader.file.FileWriteJson;
import com.lumiomedical.etl.transformer.filesystem.CreateDirectory;
import com.lumiomedical.etl.transformer.http.HttpTransformers;
import com.lumiomedical.etl.transformer.jsoup.JsoupRequestTransformer;
import com.lumiomedical.flow.Flow;
import com.lumiomedical.flow.FlowOut;
import com.lumiomedical.flow.compiler.FlowCompiler;
import com.lumiomedical.flow.impl.parallel.ParallelCompiler;
import com.lumiomedical.flow.node.Node;
import com.lumiomedical.flow.stream.StreamOut;
import com.noleme.json.Json;
import org.jsoup.nodes.Document;

import java.util.Collection;
import java.util.List;

import static com.lumiomedical.etl.transformer.Transformers.nonFatal;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/12/24
 */
public class SampleCrawl extends ETL
{
    private final String outputDir;
    private final int parallelism;

    public SampleCrawl(String outputDir, int parallelism)
    {
        this.outputDir = outputDir;
        this.parallelism = parallelism;
    }

    @Override
    protected Collection<Node> provideFlows()
    {
        var statsFile = outputDir+"stats.json";

        /* We extract the root page, then we extract all its links */
        var rootFlow  = extractRootPage(outputDir);
        var crawlFlow = crawlPage(rootFlow, outputDir, this.parallelism);

        /* From these flows, we produce some stats */
        var statsFlow = createStats(rootFlow, statsFile);
        updateStats(crawlFlow, statsFlow, statsFile);

        return List.of(rootFlow);
    }

    @Override
    protected FlowCompiler provideCompiler()
    {
        return new ParallelCompiler(this.parallelism, true);
    }

    /**
     *
     * @param outputDir
     * @return
     */
    private static FlowOut<Page> extractRootPage(String outputDir)
    {
        FlowOut<String> urlFlow = Flow.from("url");

        return urlFlow
            .pipe(new CreateDirectory<>(outputDir))
            /* We produce a Page entity and extract links in the document */
            .pipe(HttpTransformers::asURL)
            .pipe(new JsoupRequestTransformer())
            .pipe(SampleCrawl::createPage)
            .pipe(new PageLinkExtractor())
            .driftSink(new PageLoader(outputDir))
        ;
    }

    /**
     *
     * @param rootFlow
     * @param outputDir
     * @param parallelism
     * @return
     */
    private static StreamOut<Page> crawlPage(FlowOut<Page> rootFlow, String outputDir, int parallelism)
    {
        return rootFlow
            .pipe(Page::getLinks)
            .stream(IterableGenerator::new).setMaxParallelism(parallelism)
            /* For each link, we query the page and stop the stream flow if it isn't successful */
            .pipe(nonFatal(HttpTransformers::asURL))
            .pipe(nonFatal(new JsoupRequestTransformer()))
            .pipe(SampleCrawl::createPage)
            .driftSink(new PageLoader(outputDir))
        ;
    }

    /**
     *
     * @param rootFlow
     * @param statsFilePath
     * @return
     */
    private static FlowOut<ObjectNode> createStats(FlowOut<Page> rootFlow, String statsFilePath)
    {
        /* We initialize a "stats" file with some metadata */
        return rootFlow
            .pipe(page -> {
                var json = Json.newObject();

                var links = Json.newArray();
                for (String link : page.getLinks())
                    links.add(link);

                json.set("found_links", links);

                return json;
            })
            .driftSink(new FileWriteJson<>(statsFilePath))
        ;
    }

    /**
     *
     * @param pageFlow
     * @param statsFlow
     * @param statsFilePath
     * @return
     */
    private static Node updateStats(StreamOut<Page> pageFlow, FlowOut<ObjectNode> statsFlow, String statsFilePath)
    {
        return pageFlow
            /* We accumulate all processed links and update the "stats" file */
            .accumulate(Collection::size)
            .join(statsFlow, (pageCount, stats) -> stats.put("downloaded_pages", pageCount))
            .into(new FileWriteJson<>(statsFilePath))
        ;
    }

    /**
     *
     * @param document
     * @return
     */
    private static Page createPage(Document document)
    {
        return new Page(document.baseUri())
            .setDocument(document)
        ;
    }
}
