package com.lumiomedical.etl.transformer.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.lumiomedical.flow.etl.transformer.Transformer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/04/13
 */
public class JsonInputStreamTransformer<J extends JsonNode> implements Transformer<J, InputStream>
{
    private final boolean prettify;
    private final Charset charset;

    /**
     *
     */
    public JsonInputStreamTransformer()
    {
        this(false, Charset.defaultCharset());
    }

    /**
     *
     * @param prettify
     * @param charset
     */
    public JsonInputStreamTransformer(boolean prettify, Charset charset)
    {
        this.prettify = prettify;
        this.charset = charset;
    }

    @Override
    public InputStream transform(J json)
    {
        String jsonString = this.prettify ? json.toPrettyString() : json.toString();
        return new ByteArrayInputStream(jsonString.getBytes(this.charset));
    }
}
