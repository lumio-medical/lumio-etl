package com.lumiomedical.etl.transformer.tablesaw;

import com.lumiomedical.etl.dataframe.processor.print.PrintTableProcessor;
import com.lumiomedical.flow.actor.transformer.Transformer;
import tech.tablesaw.api.Table;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/04/10
 */
public class TablesawPrint implements Transformer<Table, Table>
{
    private final PrintTableProcessor processor;

    /**
     *
     * @param columnNames
     */
    public TablesawPrint(String... columnNames)
    {
        this(20, columnNames);
    }

    /**
     *
     * @param max
     * @param columnNames
     */
    public TablesawPrint(int max, String... columnNames)
    {
        this.processor = new PrintTableProcessor(max, columnNames);
    }

    @Override
    public Table transform(Table input)
    {
        return this.processor.process(input);
    }
}
