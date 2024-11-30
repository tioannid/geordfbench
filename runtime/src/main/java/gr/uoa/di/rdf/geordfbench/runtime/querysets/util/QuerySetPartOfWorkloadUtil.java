package gr.uoa.di.rdf.geordfbench.runtime.querysets.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import gr.uoa.di.rdf.geordfbench.runtime.querysets.partofworkload.IQuerySetPartOfWorkload;
import gr.uoa.di.rdf.geordfbench.runtime.querysets.partofworkload.impl.DynamicTempParamQS;
import gr.uoa.di.rdf.geordfbench.runtime.querysets.partofworkload.impl.MacroQS;
import gr.uoa.di.rdf.geordfbench.runtime.querysets.partofworkload.impl.SimpleQSPartOfWorkload;
import gr.uoa.di.rdf.geordfbench.runtime.querysets.partofworkload.impl.StaticTempParamQS;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public class QuerySetPartOfWorkloadUtil {

    // --- Static members -----------------------------
    static org.apache.log4j.Logger logger
            = org.apache.log4j.Logger.getLogger(QuerySetPartOfWorkloadUtil.class.getSimpleName());
    public static final String QUERYSETJSONDEFS_DIR = "../json_defs/workloads/querysets/";
    public static final String SCALABILITY_FUNC_JSONDEF_FILE = QUERYSETJSONDEFS_DIR + "scalabilityFuncQSoriginal.json";
    public static final String SCALABILITY_PRED_JSONDEF_FILE = QUERYSETJSONDEFS_DIR + "scalabilityPredQSoriginal.json";
    public static final String SYNTHETICJSONDEF_FILE = QUERYSETJSONDEFS_DIR + "syntheticQSoriginal.json";
    public static final String RWMICROJSONDEF_FILE = QUERYSETJSONDEFS_DIR + "rwmicroQSoriginal.json";
    public static final String RWMACROREVERSEGEOCODING_JSONDEF_FILE = QUERYSETJSONDEFS_DIR + "rwmacroreversegeocodingQSoriginal.json";
    public static final String RWMACROMAPSEARCH_JSONDEF_FILE = QUERYSETJSONDEFS_DIR + "rwmacromapsearchQSoriginal.json";
    public static final String RWMACRORAPIDMAPPING_JSONDEF_FILE = QUERYSETJSONDEFS_DIR + "rwmacrorapidmappingQSoriginal.json";
    public static final String RWMACROCOMPUTESTATISTICS_JSONDEF_FILE = QUERYSETJSONDEFS_DIR + "rwmacrocomputestatisticsQSoriginal.json";
    public static final String RWMACROGEOCODING_JSONDEF_FILE = QUERYSETJSONDEFS_DIR + "censusmacrogeocodingQSoriginal.json";

    // --- Methods -----------------------------------
    // A) -- Methods that can re-create the JSON definition files
    //       GeoRDFBench querysets
    /**
     * Creates the JSON definition files for the Scalability (spatial function
     * and spatial predicate) geographica querysets
     * in
     * Geographica/runtime/src/main/resources/json_defs/workloads/querysets/scalability{Func|Pred}QSoriginal.json
     *
     */
    public static void createScalabilityQS_OriginalJSONDefFile() {
        try {
            StaticTempParamQS.newScalabilityFuncQS().serializeToJSON(new File(SCALABILITY_FUNC_JSONDEF_FILE));
            StaticTempParamQS.newScalabilityPredQS().serializeToJSON(new File(SCALABILITY_PRED_JSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates the JSON definition file for the Macro Geocoding-Census
     * geographica queryset in
     * Geographica/runtime/src/main/resources/json_defs/workloads/querysets/rwmacrogeocodingQSoriginal.json
     *
     */
    public static void createMacroGeocodingQS_OriginalJSONDefFile() {
        try {
            MacroQS.newMacroGeocodingQS().serializeToJSON(new File(RWMACROGEOCODING_JSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates the JSON definition file for the RealWorld Micro geographica
     * queryset in
     * Geographica/runtime/src/main/resources/json_defs/workloads/querysets/rwmicroQSoriginal.json
     *
     */
    public static void createRWMicroQS_OriginalJSONDefFile() {
        try {
            DynamicTempParamQS.newRWMicroQS().serializeToJSON(new File(RWMICROJSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates the JSON definition file for the Macro Reverse Geocoding
     * geographica queryset in
     * Geographica/runtime/src/main/resources/json_defs/workloads/querysets/rwmacroreversegeocodingQSoriginal.json
     *
     */
    public static void createMacroReverseGeocodingQS_OriginalJSONDefFile() {
        try {
            MacroQS.newMacroReverseGeocodingQS().serializeToJSON(new File(RWMACROREVERSEGEOCODING_JSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates the JSON definition file for the Macro MapSearch geographica
     * queryset in
     * Geographica/runtime/src/main/resources/json_defs/querysets/rwmacromapsearchQSoriginal.json
     *
     */
    public static void createMacroMapSearchQS_OriginalJSONDefFile() {
        try {
            MacroQS.newMacroMapSearchQS().serializeToJSON(new File(RWMACROMAPSEARCH_JSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates the JSON definition file for the Macro Rapid Mapping geographica
     * queryset in
     * Geographica/runtime/src/main/resources/json_defs/querysets/scalabilityQSoriginal.json
     *
     */
    public static void createMacroRapidMappingQS_OriginalJSONDefFile() {
        try {
            MacroQS.newMacroRapidMappingQS().serializeToJSON(new File(RWMACRORAPIDMAPPING_JSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates the JSON definition file for the Macro Compute Statistics
     * geographica queryset in
     * Geographica/runtime/src/main/resources/json_defs/querysets/scalabilityQSoriginal.json
     *
     */
    public static void createMacroComputeStatisticsQS_OriginalJSONDefFile() {
        try {
            MacroQS.newMacroComputeStatisticsQS().serializeToJSON(new File(RWMACROCOMPUTESTATISTICS_JSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates the JSON definition file for the Synthetic geographica queryset
     * in
     * Geographica/runtime/src/main/resources/json_defs/querysets/syntheticQSoriginal.json
     *
     */
    public static void createSyntheticQS_OriginalJSONDefFile(int N) {
        try {
            StaticTempParamQS.newSyntheticQS(N).serializeToJSON(new File(SYNTHETICJSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }
    /**
     * Deserialize from JSON file
     *
     * @param fileName
     * @return GeographicaDataSet object
     */
    public static IQuerySetPartOfWorkload deserializeFromJSON(String fileName) {
        File serObjFile = new File(fileName);
        // create the mapper
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        SimpleQSPartOfWorkload qs = null;
        try {
            qs = mapper.readValue(serObjFile, new TypeReference<SimpleQSPartOfWorkload>() {
            });
        } catch (IOException ex) {
            logger.info(ex.getMessage());
        }
        qs.initializeAfterDeserialization();
        return qs;
    }

    public static void main(String[] args) throws JsonProcessingException, IOException {
        IQuerySetPartOfWorkload qs;
        // Realworld Micro Queryset - dynamic template
        QuerySetPartOfWorkloadUtil.createRWMicroQS_OriginalJSONDefFile();
        qs = QuerySetPartOfWorkloadUtil.deserializeFromJSON(QuerySetPartOfWorkloadUtil.RWMICROJSONDEF_FILE);
        logger.info(qs.serializeToJSON());
        // Realworld Macro Reverse Geocoding Queryset
        QuerySetPartOfWorkloadUtil.createMacroReverseGeocodingQS_OriginalJSONDefFile();
        qs = QuerySetPartOfWorkloadUtil.deserializeFromJSON(QuerySetPartOfWorkloadUtil.RWMACROREVERSEGEOCODING_JSONDEF_FILE);
        logger.info(qs.serializeToJSON());
        // Realworld Macro Map Search Queryset
        QuerySetPartOfWorkloadUtil.createMacroMapSearchQS_OriginalJSONDefFile();
        qs = QuerySetPartOfWorkloadUtil.deserializeFromJSON(QuerySetPartOfWorkloadUtil.RWMACROMAPSEARCH_JSONDEF_FILE);
        logger.info(qs.serializeToJSON());
        // Realworld Macro Rapid Mapping Queryset
        QuerySetPartOfWorkloadUtil.createMacroRapidMappingQS_OriginalJSONDefFile();
        qs = QuerySetPartOfWorkloadUtil.deserializeFromJSON(QuerySetPartOfWorkloadUtil.RWMACRORAPIDMAPPING_JSONDEF_FILE);
        logger.info(qs.serializeToJSON());
        // Realworld Macro Compute Statistics Queryset
        QuerySetPartOfWorkloadUtil.createMacroComputeStatisticsQS_OriginalJSONDefFile();
        qs = QuerySetPartOfWorkloadUtil.deserializeFromJSON(QuerySetPartOfWorkloadUtil.RWMACROCOMPUTESTATISTICS_JSONDEF_FILE);
        logger.info(qs.serializeToJSON());
        // Realworld Macro Geocoding Queryset
        QuerySetPartOfWorkloadUtil.createMacroGeocodingQS_OriginalJSONDefFile();
        qs = QuerySetPartOfWorkloadUtil.deserializeFromJSON(QuerySetPartOfWorkloadUtil.RWMACROGEOCODING_JSONDEF_FILE);
        logger.info(qs.serializeToJSON());
        // Synthetic N=512 Queryset
        QuerySetPartOfWorkloadUtil.createSyntheticQS_OriginalJSONDefFile(512);
        qs = QuerySetPartOfWorkloadUtil.deserializeFromJSON(QuerySetPartOfWorkloadUtil.SYNTHETICJSONDEF_FILE);
        logger.info(qs.serializeToJSON());
        // Scalability Queryset
        QuerySetPartOfWorkloadUtil.createScalabilityQS_OriginalJSONDefFile();
        qs = QuerySetPartOfWorkloadUtil.deserializeFromJSON(QuerySetPartOfWorkloadUtil.SCALABILITY_FUNC_JSONDEF_FILE);
        logger.info(qs.serializeToJSON());
        qs = QuerySetPartOfWorkloadUtil.deserializeFromJSON(QuerySetPartOfWorkloadUtil.SCALABILITY_PRED_JSONDEF_FILE);
        logger.info(qs.serializeToJSON());
    }
}
