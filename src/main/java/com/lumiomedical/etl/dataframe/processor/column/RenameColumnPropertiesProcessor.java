package com.lumiomedical.etl.dataframe.processor.column;

import com.lumiomedical.etl.dataframe.processor.TableProcessor;
import com.lumiomedical.etl.dataframe.configuration.TableProperties;
import tech.tablesaw.api.Table;

/**
 * Renames an arbitrary number of columns from a given table using a name map provided by a TableProperties instance.
 *
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/03/01
 */
public class RenameColumnPropertiesProcessor implements TableProcessor
{
    private final TableProperties properties;

    /**
     *
     * @param properties
     */
    public RenameColumnPropertiesProcessor(TableProperties properties)
    {
        this.properties = properties;
    }

    @Override
    public Table process(Table table)
    {
        this.properties.getActiveMapping().forEach(cp -> {
            table.column(cp.getTargetIndex()).setName(cp.getName());
        });

        return table;
    }
}
