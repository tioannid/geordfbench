package gr.uoa.di.rdf.Geographica3.runtime.reportsource;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import gr.uoa.di.rdf.Geographica3.runtime.experiments.Experiment;
import gr.uoa.di.rdf.Geographica3.runtime.resultscollector.impl.QueryRepResults.QueryRepResult;
import java.io.File;
import java.io.IOException;

/**
 * An interface that represents a report source for Geographica3
 * experiments.
 * It allows recording the experiment results:
 * - experiment configuration
 * - query execution iteration results, timings and accuracy validation
 * 
 * @author GeoRDFBench Creator <GeoRDFBench@Univ>
 * @creationdate 26/11/2020
 * @updatedate 16/02/2023
 */
public interface IReportSource {
    
    // --- Methods -------------------------------------
    /**
     * Serialize the simple report spec to a JSON string
     *
     * @return the serialized JSON string
     */
    String serializeToJSON();

    /**
     * Serialize the simple report spec to a JSON file
     *
     * @param serObjFile File with the serialized object
     */
    void serializeToJSON(File serObjFile) throws JsonGenerationException, JsonMappingException, IOException ;

    /**
     * Record the experiment configuration.
     * 
     * @param exp a {@link Experiment} object
     * @return a <tt>long</tt> value representing the unique Experiment ID
     */
    long insExperiment(Experiment exp);

    /**
     * Record the query execution iteration results and timings.
     * 
     * @param expID unique Experiment ID
     * @param cache_type a String representing an {@link ExecutionType}
     * @param qryLabel query label
     * @param qryNo query number in the queryset
     * @param qryRepNo query repetition number
     * @param queryRepResult a <tt>QueryRepResult</tt> with the results
     */
    void insQueryExecution(long expID, String cache_type, String qryLabel, int qryNo, int qryRepNo, QueryRepResult queryRepResult);
    
    void initializeAfterDeserialization();
    
    /**
     * Writes all deferred statements to the ReportSource
     * 
     */
    void flush();
}
