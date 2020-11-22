package com.lumiomedical.etl.transformer.tablesaw;

import com.lumiomedical.flow.etl.transformer.TransformationException;
import com.lumiomedical.flow.etl.transformer.Transformer;
import tech.tablesaw.api.Table;

import java.io.InputStream;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/02/27
 */
public class TablesawCachedTransformer implements Transformer<InputStream, Table>
{
    private final Transformer<InputStream, Table> transformer;
    private final boolean copy;
    private Table cache;

    /**
     *
     * @param transformer
     */
    public TablesawCachedTransformer(Transformer<InputStream, Table> transformer)
    {
        this(transformer, true);
    }

    /**
     *
     * @param transformer
     * @param copy
     */
    public TablesawCachedTransformer(Transformer<InputStream, Table> transformer, boolean copy)
    {
        this.transformer = transformer;
        this.copy = copy;
    }

    @Override
    public Table transform(InputStream input) throws TransformationException
    {
        if (this.cache == null)
            this.cache = this.transformer.transform(input);
        return this.copy ? this.cache.copy() : this.cache;
    }
}
