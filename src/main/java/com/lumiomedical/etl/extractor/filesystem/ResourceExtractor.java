package com.lumiomedical.etl.extractor.filesystem;

import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.actor.extractor.ExtractionException;
import com.lumiomedical.flow.actor.extractor.Extractor;
import com.noleme.commons.file.Resources;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/02/27
 */
public class ResourceExtractor implements Extractor<InputStream>
{
    private final String path;

    /**
     *
     * @param path
     */
    public ResourceExtractor(String path)
    {
        this.path = path;
    }

    @Override
    public InputStream extract() throws ExtractionException
    {
        try {
            Logging.logger.info("Initializing stream from resources at " + this.path);
            return Resources.streamFrom(this.path);
        }
        catch (IOException e) {
            throw new ExtractionException(e.getMessage(), e);
        }
    }
}
