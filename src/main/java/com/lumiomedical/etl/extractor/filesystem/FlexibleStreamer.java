package com.lumiomedical.etl.extractor.filesystem;

import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.actor.extractor.ExtractionException;
import com.lumiomedical.flow.actor.extractor.Extractor;
import com.noleme.commons.file.Files;
import com.noleme.commons.file.Resources;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/05/26
 */
public class FlexibleStreamer implements Extractor<InputStream>
{
    private final String path;

    /**
     * @param path
     */
    public FlexibleStreamer(String path)
    {
        this.path = path;
    }

    @Override
    public InputStream extract() throws ExtractionException
    {
        try {
            if (Files.fileExists(this.path))
            {
                Logging.logger.info("Initializing stream from filesystem at " + this.path);
                return Files.streamFrom(this.path);
            }

            if (Resources.exists(this.path))
            {
                Logging.logger.info("Initializing stream from resources at " + this.path);
                return Resources.streamFrom(this.path);
            }

            throw new ExtractionException("No file nor resource could be found at path " + this.path);
        }
        catch (IOException e) {
            throw new ExtractionException(e.getMessage(), e);
        }
    }
}
