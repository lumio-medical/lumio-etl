package com.lumiomedical.etl.transformer.iostream;

import com.lumiomedical.flow.actor.transformer.Transformer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/08/21
 */
public class StringToInputStreamTransformer implements Transformer<String, InputStream>
{
    private final Charset charset;

    public StringToInputStreamTransformer()
    {
        this.charset = Charset.defaultCharset();
    }

    /**
     *
     * @param charset
     */
    public StringToInputStreamTransformer(Charset charset)
    {
        this.charset = charset;
    }

    @Override
    public InputStream transform(String input)
    {
        return new ByteArrayInputStream(input.getBytes(this.charset));
    }
}
