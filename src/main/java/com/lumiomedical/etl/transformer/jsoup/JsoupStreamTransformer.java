package com.lumiomedical.etl.transformer.jsoup;

import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.actor.transformer.TransformationException;
import com.lumiomedical.flow.actor.transformer.Transformer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/12/22
 */
public class JsoupStreamTransformer implements Transformer<InputStream, Document>
{
    private final Charset charset;

    public JsoupStreamTransformer(Charset charset)
    {
        this.charset = charset;
    }

    public JsoupStreamTransformer()
    {
        this(Charset.defaultCharset());
    }

    @Override
    public Document transform(InputStream input) throws TransformationException
    {
        try {
            Logging.logger.info("Initializing document from stream");
            return Jsoup.parse(input, this.charset.displayName(), "");
        }
        catch (IOException e) {
            throw new TransformationException(e.getMessage(), e);
        }
    }
}
