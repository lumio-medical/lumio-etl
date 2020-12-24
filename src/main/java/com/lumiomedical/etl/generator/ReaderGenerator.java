package com.lumiomedical.etl.generator;

import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.actor.generator.GenerationException;
import com.lumiomedical.flow.actor.generator.Generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/12/17
 */
public class ReaderGenerator implements Generator<String>
{
    private final BufferedReader reader;

    public ReaderGenerator(InputStream stream)
    {
        this.reader = new BufferedReader(new InputStreamReader(stream));
    }

    @Override
    public boolean hasNext()
    {
        try {
            return this.reader.ready();
        }
        catch (IOException e) {
            Logging.logger.error("An error occurred while attempting to determine the stream readiness: "+e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String generate() throws GenerationException
    {
        try {
            return this.reader.readLine();
        }
        catch (IOException e) {
            Logging.logger.error("An error occurred while attempting to read a line from the stream: "+e.getMessage(), e);
            throw new GenerationException(e.getMessage(), e);
        }
    }
}
