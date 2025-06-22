package gr.uoa.di.rdf.geordfbench.runtime.executionspecs.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.geordfbench.runtime.executionspecs.IExecutionSpec;
import java.util.HashMap;

/**
 * An abstract base implementation of an Execution Specification. Describes how
 * an experiment should be conducted.
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY,
        property = "classname")
public class SimpleES implements IExecutionSpec {

    // --- Static members -----------------------------
    static Logger logger = Logger.getLogger(SimpleES.class.getSimpleName());

    /**
     * Returns an instance of the standard Scalability execution specification.
     * 3 COLD and 3 WARN runs, 24 hours max duration per query execution, 7 days
     * max duration for the entire experiment, Median calculation for query
     * statistics, Skip remaining executions upon failure on COLD runs, 5000
     * msecs delay for clearing caches and garbage collections between
     * executions
     *
     * @return SimpleES instance for Scalability queryset execution
     */
    public static SimpleES newScalabilityES() {
        Map<ExecutionType, Integer> execTypeReps
                = new HashMap<>();
        execTypeReps.put(ExecutionType.COLD, 3);
        execTypeReps.put(ExecutionType.WARM, 3);
        return new SimpleES(execTypeReps, 24 * 60 * 60, 7 * 24 * 60 * 60,
                Action.RUN,
                AverageFunction.QUERY_MEDIAN,
                BehaviourOnColdExecutionFailure.SKIP_REMAINING_ALL_QUERY_EXECUTIONS,
                5000);
    }

    /**
     * Returns an instance of the standard Micro execution specification. 
     * 3 COLD and 3 WARN runs, 30 mins max duration per query execution, 
     * 1 hour max duration for the entire experiment, Median calculation for query
     * statistics, Skip remaining executions upon failure on COLD runs, 5000
     * msecs delay for clearing caches and garbage collections between
     * executions
     *
     * @return SimpleES instance for RealWorld Micro queryset execution
     */
    public static SimpleES newMicroES() {
        Map<ExecutionType, Integer> execTypeReps
                = new HashMap<>();
        execTypeReps.put(ExecutionType.COLD, 3);
        execTypeReps.put(ExecutionType.WARM, 3);
        return new SimpleES(execTypeReps, 30 * 60, 60 * 60,
                Action.RUN,
                AverageFunction.QUERY_MEDIAN,
                BehaviourOnColdExecutionFailure.SKIP_REMAINING_ALL_QUERY_EXECUTIONS,
                5000);

    }

    /**
     * Returns an instance of the standard Macro execution specification.
     * COLD_CONTINUOUS run,
     * 1 hour max duration per query execution, 1 hour max duration for the
     * entire experiment, Mean calculation for query statistics, Exclude query
     * and proceed upon failure on COLD runs, 5000 msecs delay for
     * clearing caches and garbage collections between executions
     *
     * @return SimpleES instance for RealWorld Macro queryset execution
     */
    public static SimpleES newMacroES() {
        Map<ExecutionType, Integer> execTypeReps
                = new HashMap<>();
        execTypeReps.put(ExecutionType.COLD_CONTINUOUS, -1);
        return new SimpleES(execTypeReps, 60 * 60 + 10, 60 * 60,
                Action.RUN,
                AverageFunction.QUERYSET_MEAN,
                BehaviourOnColdExecutionFailure.EXCLUDE_AND_PROCEED,
                5000);
    }

    // --- Data members ------------------------------
    Map<ExecutionType, Integer> execTypeReps;
    long maxDurationSecsPerQueryRep;    // max duration (secs) per query repetition
    long maxDurationSecs;   // max duration (secs) per execution
    Action action;  // print or run
    AverageFunction avgFunc;    // the "average" function to use as the query execution time
    BehaviourOnColdExecutionFailure onColdFailure;  // what to do if cold query exec fails
    int clearCacheDelaymSecs;    // delay for clear system cache to take effect (5000 msecs)

    // --- Constructors ------------------------------
    public SimpleES(Map<ExecutionType, Integer> execTypeReps,
            long maxDurationSecsPerQueryRep, long maxDurationSecs,
            Action action, AverageFunction avgFunc,
            BehaviourOnColdExecutionFailure onColdFailure,
            int clearCacheDelaySecs) {
        this.execTypeReps = execTypeReps;
        this.maxDurationSecsPerQueryRep = maxDurationSecsPerQueryRep;
        this.maxDurationSecs = maxDurationSecs;
        this.action = action;
        this.avgFunc = avgFunc;
        this.onColdFailure = onColdFailure;
        this.clearCacheDelaymSecs = clearCacheDelaySecs;
    }

    public SimpleES() {
    }

    // --- Data Accessors ------------------------------
    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        SimpleES.logger = logger;
    }

    public Map<ExecutionType, Integer> getExecTypeReps() {
        return execTypeReps;
    }

    public void setExecTypeReps(Map<ExecutionType, Integer> execTypeReps) {
        this.execTypeReps = execTypeReps;
    }

    @Override
    public long getMaxDurationSecsPerQueryRep() {
        return maxDurationSecsPerQueryRep;
    }

    @Override
    public void setMaxDurationSecsPerQueryRep(long maxDurationSecsPerQueryRep) {
        this.maxDurationSecsPerQueryRep = maxDurationSecsPerQueryRep;
    }

    @Override
    public long getMaxDurationSecs() {
        return maxDurationSecs;
    }

    @Override
    public void setMaxDurationSecs(long maxDurationSecs) {
        this.maxDurationSecs = maxDurationSecs;
    }

    @Override
    public Action getAction() {
        return action;
    }

    @Override
    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public AverageFunction getAvgFunc() {
        return avgFunc;
    }

    @Override
    public void setAvgFunc(AverageFunction avgFunc) {
        this.avgFunc = avgFunc;
    }

    @Override
    public BehaviourOnColdExecutionFailure getOnColdFailure() {
        return onColdFailure;
    }

    @Override
    public void setOnColdFailure(BehaviourOnColdExecutionFailure onColdFailure) {
        this.onColdFailure = onColdFailure;
    }

    @Override
    public int getClearCacheDelaymSecs() {
        return clearCacheDelaymSecs;
    }

    public void setClearCacheDelaymSecs(int clearCacheDelaymSecs) {
        this.clearCacheDelaymSecs = clearCacheDelaymSecs;
    }

    // --- Methods -----------------------------------
    @Override
    public int getExecTypeReps(ExecutionType execType) {
        return (this.execTypeReps.containsKey(execType) ? this.execTypeReps.get(execType) : 0);
    }

    /**
     * Serialize an execution specification to a JSON file
     *
     * @param serObjFile File with the serialized object
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    public void serializeToJSON(File serObjFile) throws JsonGenerationException,
            JsonMappingException, IOException {
        // create the mapper
        ObjectMapper mapper = new ObjectMapper();
        // do not serialize null fields
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // enable pretty printing
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // serialize the object
        mapper.writeValue(serObjFile, this);
    }

    @Override
    public String toString() {
        String execTypeRepsMsg = "";
        for (Entry<ExecutionType, Integer> et : this.execTypeReps.entrySet()) {
            execTypeRepsMsg = execTypeRepsMsg
                    + et.getKey().name() + "=" + et.getValue() + ", ";
        }
        return this.getClass().getSimpleName() + "{ "
                + execTypeRepsMsg
                + "action=" + this.action.name() + ", "
                + "maxduration=" + this.maxDurationSecs + " secs, "
                + "repmaxduration=" + this.maxDurationSecsPerQueryRep + " secs, "
                + "func=" + this.avgFunc.name()
                + " }";
    }
}
