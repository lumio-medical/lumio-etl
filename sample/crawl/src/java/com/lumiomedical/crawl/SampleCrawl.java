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
import com.lumiomedical.etl.transformer.jsoup.JsoupRequest;
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
        /* We extract the root page, then we extract all its links */
        var rootFlow  = extractRootPage();
        var crawlFlow = crawlPage(rootFlow);

        /* From these flows, we produce some stats */
        FlowOut<ObjectNode> statsFlow;
        statsFlow = createStats(rootFlow);
        statsFlow = updateStats(crawlFlow, statsFlow);
        statsFlow.sink(new FileWriteJson<>(outputDir+"stats.json"));

        return List.of(rootFlow);
    }

    @Override
    protected FlowCompiler provideCompiler()
    {
        return new ParallelCompiler(this.parallelism, true);
    }

    /**
     *
     * @return The root page flow
     */
    private FlowOut<Page> extractRootPage()
    {
        FlowOut<String> urlFlow = Flow.from("url");

        return urlFlow
            .pipe(new CreateDirectory<>(this.outputDir))
            /* We produce a Page entity and extract links in the document */
            .pipe(HttpTransformers::asURL)
            .pipe(new JsoupRequest())
            .pipe(SampleCrawl::createPage)
            .pipe(new PageLinkExtractor())
            .driftSink(new PageLoader(this.outputDir))
        ;
    }

    /**
     *
     * @param rootFlow The root page flow
     * @return A stream of Pages found in the root page
     */
    private StreamOut<Page> crawlPage(FlowOut<Page> rootFlow)
    {
        return rootFlow
            .pipe(Page::getLinks)
            .stream(IterableGenerator::new).setMaxParallelism(this.parallelism)
            /* For each link, we query the page and stop the stream flow if it isn't successful */
            .pipe(nonFatal(HttpTransformers::asURL))
            .pipe(nonFatal(new JsoupRequest()))
            .pipe(SampleCrawl::createPage)
            .driftSink(new PageLoader(this.outputDir))
        ;
    }

    /**
     * From the root flow, we produce an array of links found in the root page.
     *
     * @param rootFlow The root page flow
     * @return The stats flow as a JSON object
     */
    private FlowOut<ObjectNode> createStats(FlowOut<Page> rootFlow)
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
        ;
    }

    /**
     * From the child page stream, we make an accumulation for counting how many pages we could download (remember that failed downloads result in a flow interruption).
     *
     * @param pageFlow The root page flow
     * @param statsFlow The stats flow
     * @return The stats flow as a JSON object
     */
    private FlowOut<ObjectNode> updateStats(StreamOut<Page> pageFlow, FlowOut<ObjectNode> statsFlow)
    {
        return pageFlow
            /* We accumulate all processed links and update the "stats" file */
            .accumulate(Collection::size)
            .join(statsFlow, (pageCount, stats) -> stats.put("downloaded_pages", pageCount))
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
