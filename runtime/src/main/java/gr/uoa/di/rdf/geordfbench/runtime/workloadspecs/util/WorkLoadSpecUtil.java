package gr.uoa.di.rdf.geordfbench.runtime.workloadspecs.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import gr.uoa.di.rdf.geordfbench.runtime.datasets.simple.ScalabilityEnum;
import gr.uoa.di.rdf.geordfbench.runtime.workloadspecs.IGeospatialWorkLoadSpec;
import gr.uoa.di.rdf.geordfbench.runtime.workloadspecs.impl.SimpleGeospatialWL;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public class WorkLoadSpecUtil {

    // --- Static members -----------------------------
    static Logger logger
            = Logger.getLogger(WorkLoadSpecUtil.class.getSimpleName());
    static final String WORKLOADSPECJSONDEFS_DIR = "../json_defs/workloads/";
    static final String CENSUSJSONDEF_FILE = WORKLOADSPECJSONDEFS_DIR + "censusmacrogeocodingWLoriginal.json";
    static final String REALWORLD_JSONDEF_FILE = WORKLOADSPECJSONDEFS_DIR + "rwmicroWLoriginal.json";
    static final String SYNTHETIC_JSONDEF_FILE = WORKLOADSPECJSONDEFS_DIR + "syntheticWLoriginal.json";
    static final String SYNTHETICPOIS_JSONDEF_FILE = WORKLOADSPECJSONDEFS_DIR + "syntheticPOIsWLoriginal.json";
    static final String RWMACROCOMPUTESTATISTICS_JSONDEF_FILE = WORKLOADSPECJSONDEFS_DIR + "rwmacrocomputestatisticsWLoriginal.json";
    static final String RWMACROREVERSEGEOCODING_JSONDEF_FILE = WORKLOADSPECJSONDEFS_DIR + "rwmacroreversegeocodingWLoriginal.json";
    static final String RWMACROMAPSEARCH_JSONDEF_FILE = WORKLOADSPECJSONDEFS_DIR + "rwmacromapsearchWLoriginal.json";
    static final String RWMACRORAPIDMAPPING_JSONDEF_FILE = WORKLOADSPECJSONDEFS_DIR + "rwmacrorapidmappingWLoriginal.json";

    static final String SCALABILITY_FUNC_JSONDEF_FILE(ScalabilityEnum enumScal) {
        return WORKLOADSPECJSONDEFS_DIR + "scalabilityFunc" + enumScal.toString() + "_WLoriginal.json";
    }

    static final String SCALABILITY_PRED_JSONDEF_FILE(ScalabilityEnum enumScal) {
        return WORKLOADSPECJSONDEFS_DIR + "scalabilityPred" + enumScal.toString() + "_WLoriginal.json";
    }

    // --- Methods -----------------------------------
    // A) -- Methods that can re-create the JSON definition files
    //       for GeoRDFBench workloads
    /**
     * Creates the JSON definition file for the RealWorld Micro geographica
     * workload in
     * Geographica/runtime/src/main/resources/json_defs/workloads/rwmicroWLoriginal.json
     *
     */
    public static void createRWMicroWL_OriginalJSONDefFile() {
        try {
            SimpleGeospatialWL.newRWMicroWL().serializeToJSON(new File(REALWORLD_JSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates the JSON definition file for the Geocoding Census geographica
     * workload in
     * Geographica/runtime/src/main/resources/json_defs/workloads/censusmacrogeocodingWLoriginal.json
     *
     */
    public static void createCensusGeographicaWL_OriginalJSONDefFile() {
        try {
            SimpleGeospatialWL.newCensusMacroGeoWL().serializeToJSON(new File(CENSUSJSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates the JSON definition file for the RealWorld Macro Compute
     * Statistics geographica workload in
     * Geographica/runtime/src/main/resources/json_defs/workloads/rwmacrocomputestatisticsWLoriginal.json
     *
     */
    public static void createMacroComputeStatisticsWL_OriginalJSONDefFile() {
        try {
            SimpleGeospatialWL.newRWMacroCompStatsWL().serializeToJSON(new File(RWMACROCOMPUTESTATISTICS_JSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates the JSON definition file for the RealWorld Macro Reverse
     * Geocoding geographica workload in
     * Geographica/runtime/src/main/resources/json_defs/workloads/rwmacroreversegeocodingWLoriginal.json
     *
     */
    public static void createMacroReverseGeocodingWL_OriginalJSONDefFile() {
        try {
            SimpleGeospatialWL.newRWMacroRevGeoWL().serializeToJSON(new File(RWMACROREVERSEGEOCODING_JSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates the JSON definition file for the RealWorld Macro Map Search
     * geographica workload in
     * Geographica/runtime/src/main/resources/json_defs/workloads/rwmacromapsearchWLoriginal.json
     *
     */
    public static void createMacroMapSearchWL_OriginalJSONDefFile() {
        try {
            SimpleGeospatialWL.newRWMacroMapSearchWL().serializeToJSON(new File(RWMACROMAPSEARCH_JSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates the JSON definition file for the RealWorld Macro Rapid Mapping
     * geographica workload in
     * Geographica/runtime/src/main/resources/json_defs/workloads/rwmacrorapidmappingWLoriginal.json
     *
     */
    public static void createMacroRapidMappingWL_OriginalJSONDefFile() {
        try {
            SimpleGeospatialWL.newRWMacroRapidMappingWL().serializeToJSON(new File(RWMACRORAPIDMAPPING_JSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates the JSON definition file for the Scalability N {function and
     * predicate} geographica workload in
     * Geographica/runtime/src/main/resources/json_defs/workloads/scalability{Func|Pred}{N}_WLSoriginal.json
     *
     * @param enumScal the scale or subset of the Scalability complex dataset
     */
    public static void createScalabilityWL_OriginalJSONDefFile(ScalabilityEnum enumScal) {
        try {
            SimpleGeospatialWL.newScalabilityFuncWL(enumScal).serializeToJSON(new File(SCALABILITY_FUNC_JSONDEF_FILE(enumScal)));
            SimpleGeospatialWL.newScalabilityPredWL(enumScal).serializeToJSON(new File(SCALABILITY_PRED_JSONDEF_FILE(enumScal)));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates the JSON definition file for the Synthetic N=512 geographica
     * workload in
     * Geographica/runtime/src/main/resources/json_defs/workloads/syntheticWLoriginal.json
     *
     */
    public static void createSyntheticGeographicaWL_OriginalJSONDefFile() {
        try {
            SimpleGeospatialWL.newSyntheticWL().serializeToJSON(new File(SYNTHETIC_JSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    // B) -- Methods that can the create GeoRDFBench datasets from
    //       custom JSON definition files    
    /**
     * Deserialize from JSON file
     *
     * @param fileName
     * @return IGeospatialWorkLoadSpec object
     */
    public static IGeospatialWorkLoadSpec deserializeFromJSON(String fileName) {
        File serObjFile = new File(fileName);
        // create the mapper
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        SimpleGeospatialWL wls = null;
        try {
            wls = mapper
                    //.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .readValue(serObjFile, new TypeReference<SimpleGeospatialWL>() {
                    });
        } catch (IOException ex) {
            logger.info(ex.getMessage());
        }
        if (wls.getGeospatialQueryset() != null) {
            wls.getGeospatialQueryset().initializeAfterDeserialization();
        }
        return wls;
    }

    /**
     * Deserialize from JSON file
     *
     * @param jsonSpec
     * @return IGeospatialWorkLoadSpec object
     */
    public static IGeospatialWorkLoadSpec deserializeFromJSONString(String jsonSpec) {
        // create the mapper
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        SimpleGeospatialWL wls = null;
        try {
            wls = mapper
                    //.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .readValue(jsonSpec, new TypeReference<SimpleGeospatialWL>() {
                    });
        } catch (IOException ex) {
            logger.info(ex.getMessage());
        }
        if (wls.getGeospatialQueryset() != null) {
            wls.getGeospatialQueryset().initializeAfterDeserialization();
        }
        return wls;
    }

    public static void main(String[] args) {
        // RealWorld Workload
        WorkLoadSpecUtil.createCensusGeographicaWL_OriginalJSONDefFile();
        IGeospatialWorkLoadSpec censusWLS
                = WorkLoadSpecUtil.deserializeFromJSON(CENSUSJSONDEF_FILE);
        logger.info(censusWLS.serializeToJSON());
        WorkLoadSpecUtil.createRWMicroWL_OriginalJSONDefFile();
        IGeospatialWorkLoadSpec rwWLS
                = WorkLoadSpecUtil.deserializeFromJSON(REALWORLD_JSONDEF_FILE);
        logger.info(rwWLS.serializeToJSON());
        WorkLoadSpecUtil.createMacroComputeStatisticsWL_OriginalJSONDefFile();
        IGeospatialWorkLoadSpec maccsWLS
                = WorkLoadSpecUtil.deserializeFromJSON(RWMACROCOMPUTESTATISTICS_JSONDEF_FILE);
        logger.info(maccsWLS.serializeToJSON());
        WorkLoadSpecUtil.createMacroReverseGeocodingWL_OriginalJSONDefFile();
        IGeospatialWorkLoadSpec macrevgeoWLS
                = WorkLoadSpecUtil.deserializeFromJSON(RWMACROREVERSEGEOCODING_JSONDEF_FILE);
        logger.info(macrevgeoWLS.serializeToJSON());
        WorkLoadSpecUtil.createMacroMapSearchWL_OriginalJSONDefFile();
        IGeospatialWorkLoadSpec macmapsrcWLS
                = WorkLoadSpecUtil.deserializeFromJSON(RWMACROMAPSEARCH_JSONDEF_FILE);
        logger.info(macmapsrcWLS.serializeToJSON());
        WorkLoadSpecUtil.createMacroRapidMappingWL_OriginalJSONDefFile();
        IGeospatialWorkLoadSpec macrapmapWLS
                = WorkLoadSpecUtil.deserializeFromJSON(RWMACRORAPIDMAPPING_JSONDEF_FILE);
        logger.info(macrapmapWLS.serializeToJSON());

        // Scalability Workload
        IGeospatialWorkLoadSpec scalWLS;
        for (ScalabilityEnum e : ScalabilityEnum.values()) {
            WorkLoadSpecUtil.createScalabilityWL_OriginalJSONDefFile(e);
            scalWLS = WorkLoadSpecUtil.deserializeFromJSON(SCALABILITY_FUNC_JSONDEF_FILE(e));
            logger.info(scalWLS.serializeToJSON());
            scalWLS = WorkLoadSpecUtil.deserializeFromJSON(SCALABILITY_PRED_JSONDEF_FILE(e));
            logger.info(scalWLS.serializeToJSON());
        }

        // Synthetic Workload
        WorkLoadSpecUtil.createSyntheticGeographicaWL_OriginalJSONDefFile();
        IGeospatialWorkLoadSpec synth512WLS
                = WorkLoadSpecUtil.deserializeFromJSON(SYNTHETIC_JSONDEF_FILE);
        logger.info(synth512WLS.serializeToJSON());
    }
}
