package com.lumiomedical.etl.transformer.tablesaw;

import com.lumiomedical.etl.dataframe.processor.print.PrintTableProcessor;
import com.lumiomedical.flow.etl.transformer.Transformer;
import tech.tablesaw.api.Table;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/04/10
 */
public class TablesawPrintTransformer implements Transformer<Table, Table>
{
    private final PrintTableProcessor processor;

    /**
     *
     * @param columnNames
     */
    public TablesawPrintTransformer(String... columnNames)
    {
        this(20, columnNames);
    }

    /**
     *
     * @param max
     * @param columnNames
     */
    public TablesawPrintTransformer(int max, String... columnNames)
    {
        this.processor = new PrintTableProcessor(max, columnNames);
    }

    @Override
    public Table transform(Table input)
    {
        return this.processor.process(input);
    }
}
