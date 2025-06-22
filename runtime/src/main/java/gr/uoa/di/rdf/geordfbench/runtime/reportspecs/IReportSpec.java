package gr.uoa.di.rdf.geordfbench.runtime.reportspecs;

import java.io.File;

/**
 * An interface that represents a report specification for GeoRDFBench
 * experiments.
 * It allows working with information such as: 
 * WHERE TO STORE : report collector (CSV file, server based e.g. JavaDB, PostgreSQL)
 * WHAT TO STORE : queries
 *                 query results (full | sample (how many?))
 *                  experiment configuration
 * WHEN TO STORE IT: 
 * @author GeoRDFBench Creator <GeoRDFBench@Univ>
 * @creationdate 26/11/2020
 */
public interface IReportSpec {
    
    int getNoQueryResultToReport();

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
    void serializeToJSON(File serObjFile);
}
