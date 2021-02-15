package com.lumiomedical.etl.transformer.filesystem;

import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.actor.extractor.ExtractionException;
import com.lumiomedical.flow.actor.extractor.Extractor;
import com.lumiomedical.flow.actor.transformer.TransformationException;
import com.lumiomedical.flow.actor.transformer.Transformer;
import com.noleme.commons.file.Files;
import com.noleme.commons.file.Resources;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/05/26
 */
public class FlexibleStreamer implements Transformer<String, InputStream>
{
    @Override
    public InputStream transform(String path) throws TransformationException
    {
        try {
            if (Files.fileExists(path))
            {
                Logging.logger.info("Initializing stream from filesystem at " + path);
                return Files.streamFrom(path);
            }

            if (Resources.exists(path))
            {
                Logging.logger.info("Initializing stream from resources at " + path);
                return Resources.streamFrom(path);
            }

            throw new TransformationException("No file nor resource could be found at path " + path);
        }
        catch (IOException e) {
            throw new TransformationException(e.getMessage(), e);
        }
    }
}
