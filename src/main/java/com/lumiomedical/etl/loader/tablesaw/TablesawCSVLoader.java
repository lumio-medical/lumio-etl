package com.lumiomedical.etl.loader.tablesaw;

import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.actor.loader.Loader;
import com.lumiomedical.flow.actor.loader.LoadingException;
import tech.tablesaw.api.Table;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/04/10
 */
public class TablesawCSVLoader implements Loader<Table>
{
    private final String path;
    private final Charset charset;

    /**
     *
     * @param path
     */
    public TablesawCSVLoader(String path)
    {
        this(path, Charset.defaultCharset());
    }

    /**
     *
     * @param path
     * @param charset
     */
    public TablesawCSVLoader(String path, Charset charset)
    {
        this.path = path;
        this.charset = charset;
    }

    @Override
    public void load(Table table) throws LoadingException
    {
        try {
            Logging.logger.info("Loading table contents as CSV into " + this.path);
            table.write().csv(new FileWriter(this.path, this.charset));
        }
        catch (IOException e) {
            throw new LoadingException("An unexpected error occurred while attempting to write table at " + this.path, e);
        }
    }
}
