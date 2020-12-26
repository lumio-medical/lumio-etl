package com.lumiomedical.etl.dataframe;

import com.lumiomedical.etl.dataframe.processor.TableProcessor;
import com.lumiomedical.etl.dataframe.processor.print.PrintSchemaProcessor;
import com.lumiomedical.etl.dataframe.processor.print.PrintStructureProcessor;
import com.lumiomedical.etl.dataframe.processor.print.PrintTableProcessor;
import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.etl.transformer.tablesaw.TablesawProcessor;
import com.lumiomedical.flow.actor.transformer.Transformer;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import tech.tablesaw.selection.Selection;

import java.time.Instant;
import java.util.function.Function;

/**
 * An experimental class which goal is to allow slightly more concise code for some common use-cases.
 * The main strategies at work here are:
 * - shortcuts for generic ETL classes ctors
 * - explicit Transformer type declarations (for cases such as Table.select or Table.where that are considered ambiguous between Transformers and Loaders as lambdas)
 *
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/05/20
 */
public final class Tablesaw
{
    private Tablesaw()
    {
    }

    public static Transformer<Table, Table> where(Selection selection)
    {
        return table -> table.where(selection);
    }

    public static Transformer<Table, Table> where(Function<Table, Selection> selection)
    {
        return table -> table.where(selection);
    }

    public static Transformer<Table, Table> select(Column<?>... columns)
    {
        return table -> table.select(columns);
    }

    public static Transformer<Table, Table> select(String... columnNames)
    {
        return table -> table.select(columnNames);
    }

    public static Transformer<Table, Table> fork()
    {
        return fork(null);
    }

    /**
     *
     * @param name
     * @return
     */
    public static Transformer<Table, Table> fork(String name)
    {
        return table -> {
            var forkName = name == null ? table.name() : name;

            Logging.logger.info("Forking dataframe \""+table.name()+"\" into \""+forkName+"\"...");

            return table.copy().setName(forkName);
        };
    }

    /**
     * Produces a flow transformer from a stack of TableProcessor instances.
     *
     * @param processors
     * @return
     */
    public static TablesawProcessor processors(TableProcessor... processors)
    {
        return new TablesawProcessor(processors);
    }

    public static Table print(Table table)
    {
        return new PrintTableProcessor().process(table);
    }

    public static Table printAll(Table table)
    {
        return new PrintTableProcessor(-1).process(table);
    }

    public static Table printSchema(Table table)
    {
        return new PrintSchemaProcessor().process(table);
    }

    public static Table printStructure(Table table)
    {
        return new PrintStructureProcessor().process(table);
    }

    public static final class Criterion
    {
        /* IsMissing criteria */

        public static Transformer<Table, Table> whereIsMissing(String column)
        {
            return t -> t.where(table -> table.column(column).isMissing());
        }

        public static Transformer<Table, Table> whereIsNotMissing(String column)
        {
            return t -> t.where(table -> table.column(column).isNotMissing());
        }

        /* IsEqualTo criteria */

        public static Transformer<Table, Table> whereIsEqualTo(String column, String value)
        {
            return t -> t.where(table -> table.stringColumn(column).isEqualTo(value));
        }

        public static Transformer<Table, Table> whereIsEqualTo(String column, Long value)
        {
            return t -> t.where(table -> table.longColumn(column).isEqualTo(value));
        }

        public static Transformer<Table, Table> whereIsEqualTo(String column, Integer value)
        {
            return t -> t.where(table -> table.intColumn(column).isEqualTo(value));
        }

        public static Transformer<Table, Table> whereIsEqualTo(String column, Float value)
        {
            return t -> t.where(table -> table.floatColumn(column).isEqualTo(value));
        }

        public static Transformer<Table, Table> whereIsEqualTo(String column, Double value)
        {
            return t -> t.where(table -> table.doubleColumn(column).isEqualTo(value));
        }

        public static Transformer<Table, Table> whereIsEqualTo(String column, Instant value)
        {
            return t -> t.where(table -> table.instantColumn(column).isEqualTo(value));
        }

        public static Transformer<Table, Table> whereIsTrue(String column)
        {
            return t -> t.where(table -> table.booleanColumn(column).isTrue());
        }

        public static Transformer<Table, Table> whereIsFalse(String column)
        {
            return t -> t.where(table -> table.booleanColumn(column).isFalse());
        }

        /* IsIn criteria */

        public static Transformer<Table, Table> whereIsIn(String column, String... values)
        {
            return t -> t.where(table -> table.stringColumn(column).isIn(values));
        }

        public static Transformer<Table, Table> whereIsIn(String column, Long... values)
        {
            return t -> t.where(table -> table.longColumn(column).isIn(values));
        }

        public static Transformer<Table, Table> whereIsIn(String column, Integer... values)
        {
            return t -> t.where(table -> table.intColumn(column).isIn(values));
        }

        public static Transformer<Table, Table> whereIsIn(String column, Float... values)
        {
            return t -> t.where(table -> table.floatColumn(column).isIn(values));
        }

        public static Transformer<Table, Table> whereIsIn(String column, Double... values)
        {
            return t -> t.where(table -> table.doubleColumn(column).isIn(values));
        }
    }
}
