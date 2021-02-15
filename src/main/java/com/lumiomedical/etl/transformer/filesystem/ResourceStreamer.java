package com.lumiomedical.etl.transformer.filesystem;

import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.actor.transformer.TransformationException;
import com.lumiomedical.flow.actor.transformer.Transformer;
import com.noleme.commons.file.Resources;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/02/27
 */
public class ResourceStreamer implements Transformer<String, InputStream>
{
    @Override
    public InputStream transform(String path) throws TransformationException
    {
        try {
            Logging.logger.info("Initializing stream from resources at " + path);
            return Resources.streamFrom(path);
        }
        catch (IOException e) {
            throw new TransformationException(e.getMessage(), e);
        }
    }
}
