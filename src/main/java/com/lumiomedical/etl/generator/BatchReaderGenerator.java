package com.lumiomedical.etl.generator;

import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.actor.generator.Generator;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/12/07
 */
public class BatchReaderGenerator implements Generator<InputStream>
{
    private final BufferedReader reader;
    private final int batchSize;
    private boolean hasNext;

    /**
     *
     * @param inputStream
     * @param batchSize
     */
    public BatchReaderGenerator(InputStream inputStream, int batchSize)
    {
        this.reader = new BufferedReader(new InputStreamReader(inputStream));
        this.batchSize = batchSize;
        this.hasNext = true;
    }

    @Override
    public boolean hasNext()
    {
        try {
            return this.reader.ready() && this.hasNext;
        }
        catch (IOException e) {
            Logging.logger.error("An error occurred while attempting to determine the stream readiness: "+e.getMessage(), e);
            return false;
        }
    }

    @Override
    public InputStream generate()
    {
        List<String> lines = this.reader.lines()
            .limit(this.batchSize)
            .collect(Collectors.toList())
        ;

        this.hasNext = lines.size() == this.batchSize;

        return new ByteArrayInputStream(String.join("\n", lines).getBytes());
    }
}
