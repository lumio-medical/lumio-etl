package com.lumiomedical.etl;

import com.lumiomedical.etl.dataframe.Tablesaw;
import com.lumiomedical.etl.dataframe.configuration.TableProperties;
import com.lumiomedical.etl.dataframe.processor.column.RemoveColumnProcessor;
import com.lumiomedical.etl.transformer.tablesaw.TablesawCSVTransformer;
import com.lumiomedical.flow.Flow;
import com.lumiomedical.flow.FlowOut;
import com.lumiomedical.flow.compiler.CompilationException;
import com.lumiomedical.flow.compiler.FlowRuntime;
import com.lumiomedical.flow.compiler.RunException;
import com.lumiomedical.flow.compiler.pipeline.PipelineCompiler;
import com.lumiomedical.flow.etl.extractor.Extractor;
import com.lumiomedical.flow.node.Node;
import tech.tablesaw.api.Table;

import java.io.InputStream;
import java.util.Collection;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/03/09
 */
public abstract class ETL
{
    private FlowRuntime runtime;

    /**
     *
     * @throws ETLCompilationException
     */
    public ETL compile() throws ETLCompilationException
    {
        this.runtime = this.providePipeline();
        return this;
    }

    /**
     *
     * @return
     * @throws ETLCompilationException
     */
    protected FlowRuntime providePipeline() throws ETLCompilationException
    {
        try {
            var flows = this.provideFlows();

            return new PipelineCompiler().compile(flows);
        }
        catch (CompilationException e) {
            throw new ETLCompilationException("An error occurred while attempting to compile the ETL pipeline.", e);
        }
    }

    /**
     *
     * @return
     * @throws ETLCompilationException
     */
    abstract protected Collection<Node> provideFlows() throws ETLCompilationException;

    /**
     *
     * @throws ETLRunException
     */
    public void run() throws ETLRunException
    {
        try {
            this.runtime.run();
        }
        catch (RunException e) {
            throw new ETLRunException("An error occurred while running the pipeline.", e);
        }
    }

    /**
     * Produces a generic source flow based on a generic Extractor and a pre-loaded tablesaw mapping properties instance.
     *
     * @param extractor
     * @param properties
     * @return
     */
    public static FlowOut<Table> provideSourceFlow(Extractor<InputStream> extractor, TableProperties properties)
    {
        return provideSourceFlow(extractor, properties, false);
    }

    /**
     * Produces a generic source flow based on a generic Extractor and a pre-loaded tablesaw mapping properties instance.
     * If dropIndex is set to true, the 'index' column will be removed from the resulting dataframe.
     * This can be useful for tables that are expected to be joined with others and which index will only become a hindrance.
     *
     * @param extractor
     * @param properties
     * @param dropIndex
     * @return
     */
    public static FlowOut<Table> provideSourceFlow(Extractor<InputStream> extractor, TableProperties properties, boolean dropIndex)
    {
        FlowOut<Table> flow = Flow
            .from(extractor)
            .into(new TablesawCSVTransformer(properties));

        if (dropIndex)
        {
            flow = flow.into(Tablesaw.processors(
                new RemoveColumnProcessor("index")
            ));
        }

        return flow;
    }
}
