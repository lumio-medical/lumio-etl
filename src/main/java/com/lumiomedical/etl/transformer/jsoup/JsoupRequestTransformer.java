package com.lumiomedical.etl.transformer.jsoup;

import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.actor.transformer.TransformationException;
import com.lumiomedical.flow.actor.transformer.Transformer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/12/22
 */
public class JsoupRequestTransformer implements Transformer<URL, Document>
{
    private final int timeout;

    public JsoupRequestTransformer(int timeout)
    {
        this.timeout = timeout;
    }

    public JsoupRequestTransformer()
    {
        this(30000);
    }

    @Override
    public Document transform(URL url) throws TransformationException
    {
        try {
            Logging.logger.info("Initializing document from HTTP resource at " + url.toString());
            return Jsoup.parse(url, this.timeout);
        }
        catch (IOException e) {
            throw new TransformationException(e.getMessage(), e);
        }
    }
}
