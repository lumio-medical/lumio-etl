package com.lumiomedical.etl.extractor.filesystem;

import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.etl.extractor.ExtractionException;
import com.lumiomedical.flow.etl.extractor.Extractor;
import com.noleme.commons.file.Files;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/02/27
 */
public class FilesystemExtractor implements Extractor<InputStream>
{
    private final String path;

    /**
     *
     * @param path
     */
    public FilesystemExtractor(String path)
    {
        this.path = path;
    }

    @Override
    public InputStream extract() throws ExtractionException
    {
        try {
            Logging.logger.info("Initializing stream from filesystem at " + this.path);
            return Files.streamFrom(this.path);
        }
        catch (FileNotFoundException e) {
            throw new ExtractionException(e.getMessage(), e);
        }
    }
}
