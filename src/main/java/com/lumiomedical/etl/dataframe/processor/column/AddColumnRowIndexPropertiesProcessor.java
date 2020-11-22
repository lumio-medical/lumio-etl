package com.lumiomedical.etl.dataframe.processor.column;

import com.lumiomedical.etl.dataframe.configuration.TableProperties;
import com.lumiomedical.etl.dataframe.processor.TableProcessorException;
import tech.tablesaw.api.Table;

/**
 * Adds a row index column
 *
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/03/10
 */
public class AddColumnRowIndexPropertiesProcessor extends AddColumnRowIndexProcessor
{
    private final TableProperties properties;

    /**
     *
     * @param properties
     */
    public AddColumnRowIndexPropertiesProcessor(TableProperties properties)
    {
        this.properties = properties;
    }

    @Override
    public Table process(Table table) throws TableProcessorException
    {
        if (this.properties.requiresRowIndex())
            return super.process(table);

        return table;
    }
}
