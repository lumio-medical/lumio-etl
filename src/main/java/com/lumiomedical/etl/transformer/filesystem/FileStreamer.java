package com.lumiomedical.etl.transformer.filesystem;

import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.actor.extractor.ExtractionException;
import com.lumiomedical.flow.actor.extractor.Extractor;
import com.lumiomedical.flow.actor.transformer.TransformationException;
import com.lumiomedical.flow.actor.transformer.Transformer;
import com.noleme.commons.file.Files;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/02/27
 */
public class FileStreamer implements Transformer<String, InputStream>
{
    @Override
    public InputStream transform(String path) throws TransformationException
    {
        try {
            Logging.logger.info("Initializing stream from filesystem at " + path);
            return Files.streamFrom(path);
        }
        catch (FileNotFoundException e) {
            throw new TransformationException(e.getMessage(), e);
        }
    }
}
