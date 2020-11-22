package com.lumiomedical.etl.composite;

import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.etl.extractor.Extractor;
import com.lumiomedical.flow.etl.loader.Loader;

/**
 * A stateful loader and extractor that can be used for reusing the output of a pipeline into other pipes.
 *
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/05/15
 */
public class CacheEL<T> implements Loader<T>, Extractor<T>
{
    private final String name;
    private T cache;

    public CacheEL()
    {
        this(null);
    }

    /**
     *
     * @param name
     */
    public CacheEL(String name)
    {
        this.name = name;
    }

    @Override
    public T extract()
    {
        Logging.logger.info("Extracting data from " + this.name());
        return this.cache;
    }

    @Override
    public void load(T input)
    {
        Logging.logger.info("Loading data into " + this.name());
        this.cache = input;
    }

    /**
     *
     * @return
     */
    private String name()
    {
        return this.name != null ? this.name + " cache" : "cache";
    }
}
