package com.lumiomedical.etl.loader.file;

import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.etl.loader.Loader;
import com.lumiomedical.flow.etl.loader.LoadingException;
import com.noleme.commons.stream.Streams;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/04/01
 */
public class FileWriteLoader<I extends InputStream> implements Loader<I>
{
    private final String outputFile;
    private final boolean append;

    /**
     *
     * @param outputFile
     */
    public FileWriteLoader(String outputFile)
    {
        this(outputFile, false);
    }

    /**
     *
     * @param outputFile
     * @param append
     */
    public FileWriteLoader(String outputFile, boolean append)
    {
        this.outputFile = outputFile;
        this.append = append;
    }

    @Override
    public void load(I is) throws LoadingException
    {
        try {
            Logging.logger.info("Loading contents into " + this.outputFile + " (mode: " + (this.append ? "append" : "create") + ")");
            Streams.flow(is, new FileOutputStream(this.outputFile, this.append));
        }
        catch (IOException e) {
            throw new LoadingException("An error occurred while attempting to offload file at " + this.outputFile + ".", e);
        }
    }
}
