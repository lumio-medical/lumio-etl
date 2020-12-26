package com.lumiomedical.etl.dataframe;

import com.lumiomedical.etl.dataframe.configuration.loader.TablePropertiesLoadingException;
import com.lumiomedical.etl.dataframe.configuration.loader.iostream.TablePropertiesJSONStreamLoader;
import com.lumiomedical.etl.dataframe.configuration.loader.resource.TablePropertiesResourceLoader;
import com.lumiomedical.etl.extractor.filesystem.ResourceStreamer;
import com.lumiomedical.etl.transformer.tablesaw.TablesawCSVParse;
import com.lumiomedical.etl.transformer.tablesaw.TablesawPrint;
import com.lumiomedical.flow.Flow;
import com.lumiomedical.flow.compiler.CompilationException;
import com.lumiomedical.flow.compiler.RunException;
import org.junit.jupiter.api.Test;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/05/01
 */
public class ConfigurationParsingTest
{
    @Test
    void transformerTest() throws TablePropertiesLoadingException, CompilationException, RunException
    {
        var loader = new TablePropertiesResourceLoader(new TablePropertiesJSONStreamLoader());
        var props = loader.load("com/lumiomedical/etl/dataframe/test.json");

        Flow.runAsPipeline(
            Flow
                .from(new ResourceStreamer("com/lumiomedical/etl/dataframe/test.csv"))
                .into(new TablesawCSVParse(props))
                .into(new TablesawPrint())
        );
    }
}
