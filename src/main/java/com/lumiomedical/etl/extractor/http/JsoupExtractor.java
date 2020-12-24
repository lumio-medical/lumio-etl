package com.lumiomedical.etl.extractor.http;

import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.actor.extractor.ExtractionException;
import com.lumiomedical.flow.actor.extractor.Extractor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/12/22
 */
public class JsoupExtractor implements Extractor<Document>
{
    private final URL url;

    public JsoupExtractor(URL url)
    {
        this.url = url;
    }

    public JsoupExtractor(String url)
    {
        try {
            this.url = new URL(url);
        }
        catch (MalformedURLException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @Override
    public Document extract() throws ExtractionException
    {
        try {
            Logging.logger.info("Extracting document from HTTP resource at " + this.url.toString());
            return Jsoup.parse(this.url, 0);
        }
        catch (IOException e) {
            throw new ExtractionException(e.getMessage(), e);
        }
    }
}
