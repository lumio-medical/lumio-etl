package com.lumiomedical.etl.transformer.tablesaw;

import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.actor.transformer.Transformer;
import tech.tablesaw.api.Table;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/11/19
 */
public class TablesawForkTransformer implements Transformer<Table, Table>
{
    private final String name;

    /**
     *
     */
    public TablesawForkTransformer()
    {
        this(null);
    }

    /**
     *
     * @param name
     */
    public TablesawForkTransformer(String name)
    {
        this.name = name;
    }

    @Override
    public Table transform(Table table)
    {
        var forkName = this.name == null ? table.name() : this.name;

        Logging.logger.info("Forking dataframe \""+table.name()+"\" into \""+forkName+"\"...");

        return table.copy().setName(forkName);
    }
}
