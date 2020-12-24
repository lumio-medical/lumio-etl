package com.lumiomedical.etl.loader.file;

import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.actor.loader.Loader;
import com.lumiomedical.flow.actor.loader.LoadingException;
import com.noleme.commons.stream.Streams;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/04/01
 */
public class FileWriteString implements Loader<String>
{
    private final String outputFile;
    private final boolean append;

    /**
     *
     * @param outputFile
     */
    public FileWriteString(String outputFile)
    {
        this(outputFile, false);
    }

    /**
     *
     * @param outputFile
     * @param append
     */
    public FileWriteString(String outputFile, boolean append)
    {
        this.outputFile = outputFile;
        this.append = append;
    }

    @Override
    public void load(String string) throws LoadingException
    {
        try {
            Logging.logger.info("Loading contents into " + this.outputFile + " (mode: " + (this.append ? "append" : "create") + ")");
            Streams.flow(
                new ByteArrayInputStream(string.getBytes()),
                new FileOutputStream(this.outputFile, this.append)
            );
        }
        catch (IOException e) {
            throw new LoadingException("An error occurred while attempting to offload file at " + this.outputFile + ".", e);
        }
    }
}
