package com.lumiomedical.etl.transformer.tablesaw;

import com.lumiomedical.etl.dataframe.configuration.ColumnProperties;
import com.lumiomedical.etl.dataframe.configuration.TableProperties;
import com.lumiomedical.etl.dataframe.configuration.loader.TablePropertiesLoadingException;
import com.lumiomedical.etl.dataframe.configuration.loader.file.TablePropertiesFileLoader;
import com.lumiomedical.etl.dataframe.configuration.loader.iostream.TablePropertiesJSONStreamLoader;
import com.lumiomedical.etl.dataframe.processor.CompositeProcessor;
import com.lumiomedical.etl.dataframe.processor.TableProcessor;
import com.lumiomedical.etl.dataframe.processor.TableProcessorException;
import com.lumiomedical.etl.dataframe.processor.column.AddColumnRowIndexPropertiesProcessor;
import com.lumiomedical.etl.dataframe.processor.column.RenameColumnPropertiesProcessor;
import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.actor.transformer.TransformationException;
import com.lumiomedical.flow.actor.transformer.Transformer;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.Source;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/02/27
 */
public class TablesawCSVParse implements Transformer<InputStream, Table>
{
    private final TableProperties properties;
    private final TableProcessor preprocessor;

    public TablesawCSVParse()
    {
        this(new TableProperties());
    }

    /**
     *
     * @param properties
     */
    public TablesawCSVParse(TableProperties properties)
    {
        this.properties = properties;
        this.preprocessor = new CompositeProcessor()
            .addProcessor(new RenameColumnPropertiesProcessor(properties))
            .addProcessor(new AddColumnRowIndexPropertiesProcessor(properties));
    }

    /**
     *
     * @param confPath
     * @throws TablePropertiesLoadingException
     */
    public TablesawCSVParse(String confPath) throws TablePropertiesLoadingException
    {
        this(new TablePropertiesFileLoader(new TablePropertiesJSONStreamLoader()).load(confPath));
    }

    @Override
    public Table transform(InputStream input) throws TransformationException
    {
        try {
            CsvReadOptions options = this.prepareOptions(input);

            Logging.logger.info("Extracting CSV data into dataframe...");

            Table table = Table.read().csv(options);
            table = this.postBuild(table);

            Logging.logger.info("Extracted " + table.rowCount() + " lines into" + (!table.name().isEmpty() ? " \"" + table.name() + "\"" : "") + " dataframe.");

            return table;
        }
        catch (IOException | IndexOutOfBoundsException | TableProcessorException e) {
            throw new TransformationException(e.getMessage(), e);
        }
    }

    /**
     *
     * @param input
     * @return
     */
    private CsvReadOptions prepareOptions(InputStream input)
    {
        CsvReadOptions.Builder builder = CsvReadOptions.builder(new Source(input, this.properties.getCharset()))
            .separator(this.properties.getSeparator())
            .quoteChar(this.properties.getQuoteChar())
            .header(this.properties.hasHeader())
            .maxCharsPerColumn(this.properties.getMaxCharsPerColumn());

        if (this.properties.getSampleSize() >= 0)
            builder.sampleSize(this.properties.getSampleSize());
        if (!this.properties.getMapping().isEmpty())
            builder.columnTypes(computeColumnTypes(this.properties));

        return builder.build();
    }

    /**
     *
     * @param table
     * @return
     * @throws TableProcessorException
     */
    private Table postBuild(Table table) throws TableProcessorException
    {
        if (this.properties.getName() != null)
            table.setName(this.properties.getName());

        return this.preprocessor.process(table);
    }

    /**
     * Computes a complete array of ColumnType that we can give to the Tablesaw builder in order to detect column types.
     * The idea here is that we SKIP everything that isn't explicitly declared in the mapping configuration, so we need to fill the blank areas with SKIP ColumnTypes.
     *
     * @param properties
     * @return
     */
    private static ColumnType[] computeColumnTypes(TableProperties properties)
    {
        List<ColumnProperties> declaredTypes = properties.getMapping()
            .stream()
            .sorted(Comparator.comparingInt(ColumnProperties::getSourceIndex))
            .collect(Collectors.toList());

        ArrayList<ColumnType> autocompleted = new ArrayList<>();

        int i = 0;
        for (ColumnProperties cp : declaredTypes) {
            for (; i < cp.getSourceIndex(); ++i)
                autocompleted.add(ColumnType.SKIP);

            autocompleted.add(cp.getType());

            i = cp.getSourceIndex() + 1;
        }
        for (; i < properties.getColumnCount(); ++i)
            autocompleted.add(ColumnType.SKIP);

        return autocompleted.toArray(new ColumnType[0]);
    }
}
