package gr.uoa.di.rdf.geordfbench.runtime.resultscollector;

import gr.uoa.di.rdf.geordfbench.runtime.executionspecs.IExecutionSpec.ExecutionType;
import gr.uoa.di.rdf.geordfbench.runtime.experiments.Experiment;
import gr.uoa.di.rdf.geordfbench.runtime.resultscollector.impl.QueryRepResults;
import gr.uoa.di.rdf.geordfbench.runtime.resultscollector.impl.QueryRepResults.QueryRepResult;
import java.util.Map;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public interface IExperimentResultsCollector {

    Map<Integer, QueryRepResults> getExecTypeResults(ExecutionType execType);
    
    void updateResult(ExecutionType cacheType, int queryNo, int queryRepNo, QueryRepResult queryRepResult);

    String getExecResultString(ExecutionType cacheType, int queryNo, int queryRepNo);

    // - based on the sut creates, if missing, the "SUT-experiment" subdirectory
    // - it uses only the total times, that is measurements[..][..][2]
    // - it calculates the median of the total times
    // - creates a short file
    // - creates a long file
    void exportStatistics(Experiment exp);

    int getExecTypeReps(ExecutionType execType);

    boolean hasExecutionAnyProblems(ExecutionType execType, int queryNo);
    
    boolean hasExecutionTimedOut(ExecutionType execType, int queryNo);

    boolean hasExecutionUnsupportedOperation(ExecutionType execType, int queryNo);

    boolean hasExecutionException(ExecutionType execType, int queryNo);

    void printAll();
}
