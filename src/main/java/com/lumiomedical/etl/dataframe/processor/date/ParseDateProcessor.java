package com.lumiomedical.etl.dataframe.processor.date;

import com.lumiomedical.etl.dataframe.processor.TableProcessor;
import com.lumiomedical.etl.logging.Logging;
import tech.tablesaw.api.InstantColumn;
import tech.tablesaw.api.Table;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/04/14
 */
public class ParseDateProcessor implements TableProcessor
{
    private final DateTimeFormatter formatter;
    private final String pattern;
    private final Collection<String> columnNames;
    private final ZoneId zone;
    private Predicate<String> predicate;
    private Function<String, String> preparator;

    public ParseDateProcessor(String pattern, String... columnNames)
    {
        this(pattern, Arrays.asList(columnNames));
    }

    public ParseDateProcessor(String pattern, ZoneId zone, String... columnNames)
    {
        this(pattern, zone, Arrays.asList(columnNames));
    }

    public ParseDateProcessor(String pattern, Collection<String> columnNames)
    {
        this(pattern, ZoneOffset.UTC, columnNames);
    }

    /**
     *
     * @param pattern
     * @param zone
     * @param columnNames
     */
    public ParseDateProcessor(String pattern, ZoneId zone, Collection<String> columnNames)
    {
        this.formatter = DateTimeFormatter.ofPattern(pattern);
        this.zone = zone;
        this.pattern = pattern;
        this.columnNames = columnNames;
        this.predicate = s -> true;
        this.preparator = s -> s;
    }

    public ParseDateProcessor setPredicate(Predicate<String> predicate)
    {
        this.predicate = predicate;
        return this;
    }

    public ParseDateProcessor setPreparator(Function<String, String> preparator)
    {
        this.preparator = preparator;
        return this;
    }

    @Override
    public Table process(Table table)
    {
        this.columnNames.forEach(name -> {
            var dateColumn = InstantColumn.create(name, 0);

            table.forEach(row -> {
                String value = row.getString(name);

                Long rowIndex = row.columnNames().contains("index") ? row.getLong("index") : null;

                try {
                    if (!this.predicate.test(value))
                        dateColumn.append((Instant) null);

                    value = this.preparator.apply(value);

                    if (value.isEmpty())
                        dateColumn.append((Instant) null);
                    else {
                        TemporalAccessor accessor = this.formatter.parseBest(value, LocalDateTime::from, LocalDate::from);

                        dateColumn.append(
                            accessor instanceof LocalDateTime
                                ? ((LocalDateTime) accessor).atZone(this.zone).toInstant()
                                : ((LocalDate) accessor).atStartOfDay().atZone(this.zone).toInstant()
                        );
                    }
                }
                catch (DateTimeParseException ex) {
                    Logging.logger.error(String.format("Could not parse date column '%s'='%s' to format %s (index : %s)", name, value, this.pattern, rowIndex));
                    dateColumn.append((Instant) null);
                }
            });

            table.replaceColumn(dateColumn);
        });

        return table;
    }
}
