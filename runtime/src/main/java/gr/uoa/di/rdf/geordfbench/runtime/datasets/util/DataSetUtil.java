package gr.uoa.di.rdf.geordfbench.runtime.datasets.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import gr.uoa.di.rdf.geordfbench.runtime.datasets.complex.IGeospatialDataSet;
import gr.uoa.di.rdf.geordfbench.runtime.datasets.complex.impl.GeographicaDS;
import gr.uoa.di.rdf.geordfbench.runtime.datasets.simple.ScalabilityEnum;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public class DataSetUtil {

    // --- Static members -----------------------------
    static org.apache.log4j.Logger logger
            = org.apache.log4j.Logger.getLogger(DataSetUtil.class.getSimpleName());
    static final String DATASETJSONDEFS_DIR = "../json_defs/datasets/";
    static final String CENSUSJSONDEF_FILE = DATASETJSONDEFS_DIR + "censusDSoriginal.json";
    static final String REALWORLD_JSONDEF_FILE = DATASETJSONDEFS_DIR + "realworldDSoriginal.json";
    static final String SYNTHETIC_JSONDEF_FILE = DATASETJSONDEFS_DIR + "syntheticDSoriginal.json";
    static final String SYNTHETICPOIS_JSONDEF_FILE = DATASETJSONDEFS_DIR + "syntheticPOIsDSoriginal.json";

    static final String SCALABILITY_JSONDEF_FILE(ScalabilityEnum enumScal) {
        return DATASETJSONDEFS_DIR + "scalability_" + enumScal.toString() + "original.json";
    }

    // --- Methods -----------------------------------
    // A) -- Methods that can re-create the JSON definition files
    //       GeoRDFBench datasets
    /**
     * Creates the JSON definition file for the Census geographica dataset in
     * Geographica/runtime/src/main/resources/json_defs/datasets/censusDSoriginal.json
     *
     */
    public static void createCensusGeographicaDS_OriginalJSONDefFile() {
        try {
            GeographicaDS.newCensusGeographicaDS().serializeToJSON(new File(CENSUSJSONDEF_FILE));
        } catch (JsonMappingException ex) {
            Logger.getLogger(DataSetUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataSetUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Creates the JSON definition file for the RealWorld geographica dataset in
     * Geographica/runtime/src/main/resources/json_defs/datasets/realworldDSoriginal.json
     *
     */
    public static void createRealWorldGeographicaDS_OriginalJSONDefFile() {
        try {
            GeographicaDS.newRealWorldGeographicaDS().serializeToJSON(new File(REALWORLD_JSONDEF_FILE));
        } catch (JsonMappingException ex) {
            Logger.getLogger(DataSetUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataSetUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Creates the JSON definition file for the Scalability N geographica
     * dataset in
     * Geographica/runtime/src/main/resources/json_defs/datasets/scalability_NDSoriginal.json
     *
     * @param enumScal the scale or subset of the Scalability complex dataset
     */
    public static void createScalabilityDS_OriginalJSONDefFile(ScalabilityEnum enumScal) {
        try {
            GeographicaDS.newScalabilityDS(enumScal).serializeToJSON(new File(SCALABILITY_JSONDEF_FILE(enumScal)));
        } catch (JsonMappingException ex) {
            Logger.getLogger(DataSetUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataSetUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Creates the JSON definition file for the Synthetic geographica dataset in
     * Geographica/runtime/src/main/resources/json_defs/datasets/syntheticDSoriginal.json
     *
     */
    public static void createSyntheticGeographicaDS_OriginalJSONDefFile() {
        try {
            GeographicaDS.newSyntheticGeographicaDS().serializeToJSON(new File(SYNTHETIC_JSONDEF_FILE));
        } catch (JsonMappingException ex) {
            Logger.getLogger(DataSetUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataSetUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Creates the JSON definition file for the Synthetic geographica dataset in
     * Geographica/runtime/src/main/resources/json_defs/datasets/syntheticDSoriginal.json
     *
     */
    public static void createSyntheticPOIsGeographicaDS_OriginalJSONDefFile() {
        try {
            GeographicaDS.newSyntheticPOIsGeographicaDS().serializeToJSON(new File(SYNTHETICPOIS_JSONDEF_FILE));
        } catch (JsonMappingException ex) {
            Logger.getLogger(DataSetUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataSetUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // B) -- Methods that can the create GeoRDFBench datasets from
    //       custom JSON definition files    
    /**
     * Deserialize from JSON file
     *
     * @param fileName
     * @return GeographicaDataSet object
     */
    public static IGeospatialDataSet deserializeFromJSON(String fileName) {
        File serObjFile = new File(fileName);
        // create the mapper
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        GeographicaDS ds = null;
        try {
            ds = mapper.readValue(serObjFile, new TypeReference<GeographicaDS>() {
            });
        } catch (IOException ex) {
            logger.info(ex.getMessage());
        }
        // ds.initializeAfterDeserialization();
        return ds;
    }

    public static void main(String[] args) {
        // RealWorld Workload
        DataSetUtil.createCensusGeographicaDS_OriginalJSONDefFile();
        IGeospatialDataSet censusDS
                = DataSetUtil.deserializeFromJSON(CENSUSJSONDEF_FILE);
        logger.info(censusDS.serializeToJSON());
        DataSetUtil.createRealWorldGeographicaDS_OriginalJSONDefFile();
        IGeospatialDataSet rwDS
                = DataSetUtil.deserializeFromJSON(REALWORLD_JSONDEF_FILE);
        logger.info(rwDS.serializeToJSON());

        // Scalability Workload
        IGeospatialDataSet scalDS;
        for (ScalabilityEnum e : ScalabilityEnum.values()) {
            DataSetUtil.createScalabilityDS_OriginalJSONDefFile(e);
            scalDS
                    = DataSetUtil.deserializeFromJSON(SCALABILITY_JSONDEF_FILE(e));
            logger.info(scalDS.serializeToJSON());
        }

        // Synthetic Workload
        DataSetUtil.createSyntheticGeographicaDS_OriginalJSONDefFile();
        IGeospatialDataSet synthDS
                = DataSetUtil.deserializeFromJSON(SYNTHETIC_JSONDEF_FILE);
        logger.info(synthDS.serializeToJSON());

        // SyntheticPOIs Workload
        DataSetUtil.createSyntheticPOIsGeographicaDS_OriginalJSONDefFile();
        IGeospatialDataSet synthPOIsDS
                = DataSetUtil.deserializeFromJSON(SYNTHETICPOIS_JSONDEF_FILE);
        logger.info(synthPOIsDS.serializeToJSON());
    }
}
