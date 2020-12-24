package com.lumiomedical.etl.transformer.iostream;

import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.actor.transformer.TransformationException;
import com.lumiomedical.flow.actor.transformer.Transformer;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/12/07
 */
public class SkipLineTransformer implements Transformer<InputStream, InputStream>
{
    private final int skipCount;

    public SkipLineTransformer(int skipCount)
    {
        this.skipCount = skipCount;
    }

    public SkipLineTransformer()
    {
        this(1);
    }

    @Override
    public InputStream transform(InputStream input) throws TransformationException
    {
        try {
            for (int i = 0 ; i < skipCount ; ++i)
            {
                int read = input.read();
                while (read != -1 && (char)read != '\n')
                    read = input.read();
            }

            return input;
        }
        catch (IOException e) {
            Logging.logger.error("SkipLineTransformer encountered an error: "+e.getMessage(), e);
            throw new TransformationException(e.getMessage(), e);
        }
    }
}
