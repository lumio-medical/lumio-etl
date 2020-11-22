package com.lumiomedical.etl.dataframe.processor.row;

import com.lumiomedical.etl.dataframe.processor.TableProcessor;
import tech.tablesaw.api.Table;

public class DropRowDuplicatesProcessor implements TableProcessor
{
    @Override
    public Table process(Table table)
    {
        return table.dropDuplicateRows();
    }
}
