package gr.uoa.di.rdf.geordfbench.runtime.reportsource;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import gr.uoa.di.rdf.geordfbench.runtime.experiments.Experiment;
import gr.uoa.di.rdf.geordfbench.runtime.resultscollector.impl.QueryRepResults.QueryRepResult;
import java.io.File;
import java.io.IOException;

/**
 * An interface that represents a report source for GeoRDFBench experiments. It
 * allows recording the experiment results: - experiment configuration - query
 * execution iteration results, timings and accuracy validation
 *
 * @author Theofilos Ioannidis <tioannid@di.uoa.gr>
 * @creationdate 26/11/2020
 * @updatedate 01/09/2024
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
    void serializeToJSON(File serObjFile) throws JsonGenerationException, JsonMappingException, IOException;

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

    /**
     * Checks if report source structures have been initialized e.g,, for database
     * report sources if a database with the appropriate name already exist.
     * @return boolean
     */
    boolean isSchemaInitialized();

    /**
     * Initializes the structures of the report source. e.g,, for database
     * report sources it should create the database with the appropriate schema
     * objects and code.
     */
    boolean initializeSchema();
}
