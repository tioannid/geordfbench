package gr.uoa.di.rdf.geordfbench.runtime.executionspecs;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import gr.uoa.di.rdf.geordfbench.runtime.executionspecs.impl.SimpleES;
import java.util.Map;

/**
 * An interface that represents an Execution Specification.
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
@JsonDeserialize(as = SimpleES.class)
public interface IExecutionSpec {

    Map<ExecutionType, Integer> getExecTypeReps();

    // --- Methods -----------------------------------
    public static enum Action {
        TRANSLATE,
        RUN,
        PRINT
    }

    public static enum AverageFunction {
        QUERY_MEDIAN,
        QUERY_MEAN,
        QUERYSET_MEAN
    }

    public static enum BehaviourOnColdExecutionFailure {
        SKIP_REMAINING_ALL_QUERY_EXECUTIONS,
        SKIP_REMAINING_COLD_EXECUTIONS,
        EXCLUDE_AND_PROCEED
    }

    public static enum ExecutionType {
        COLD,
        WARM,
        COLD_CONTINUOUS
    }

    Action getAction();

    void setAction(Action action);

    AverageFunction getAvgFunc();

    long getMaxDurationSecs();

    long getMaxDurationSecsPerQueryRep();

    BehaviourOnColdExecutionFailure getOnColdFailure();

    void setAvgFunc(AverageFunction avgFunc);

    void setMaxDurationSecs(long maxDurationSecs);

    void setMaxDurationSecsPerQueryRep(long maxDurationSecsPerQueryRep);

    void setOnColdFailure(BehaviourOnColdExecutionFailure onColdFailure);

    int getClearCacheDelaymSecs();

    int getExecTypeReps(ExecutionType execType);

}
