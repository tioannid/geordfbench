package gr.uoa.di.rdf.Geographica3.runtime.executionspecs.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import gr.uoa.di.rdf.Geographica3.runtime.executionspecs.impl.SimpleES;
import java.io.File;
import java.io.IOException;
import gr.uoa.di.rdf.Geographica3.runtime.executionspecs.IExecutionSpec;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public class ExecutionSpecUtil {

    // --- Static members -----------------------------
    static org.apache.log4j.Logger logger
            = org.apache.log4j.Logger.getLogger(ExecutionSpecUtil.class.getSimpleName());
    public static final String EXECUTIONSPECJSONDEFS_DIR = "../json_defs/executionspecs/";
    public static final String MICROEXECUTIONSPECJSONDEF_FILE = EXECUTIONSPECJSONDEFS_DIR + "microESoriginal.json";
    public static final String MACROEXECUTIONSPECJSONDEF_FILE = EXECUTIONSPECJSONDEFS_DIR + "macroESoriginal.json";
    public static final String SCALABILITYEXECUTIONSPECJSONDEF_FILE = EXECUTIONSPECJSONDEFS_DIR + "scalabilityESoriginal.json";

    // --- Methods -----------------------------------
    // A) -- Methods that can re-create the JSON definition files
    //       Geographica3 experiment specifications
    /**
     * Creates the JSON definition file for the Micro execution specification in
     * Geographica/runtime/src/main/resources/json_defs/executionspecs/microESoriginal.json
     *
     */
    public static void createMicroES_OriginalJSONDefFile() {
        SimpleES mases = SimpleES.newMicroES();
        try {
            mases.serializeToJSON(new File(MICROEXECUTIONSPECJSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates the JSON definition file for the Macro execution specification in
     * Geographica/runtime/src/main/resources/json_defs/executionspecs/macroESoriginal.json
     *
     */
    public static void createMacroES_OriginalJSONDefFile() {
    SimpleES mases = SimpleES.newMacroES();
        try {
            mases.serializeToJSON(new File(MACROEXECUTIONSPECJSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates the JSON definition file for the Scalability execution
     * specification in
     * Geographica/runtime/src/main/resources/json_defs/executionspecs/scalabilityESoriginal.json
     *
     */
    public static void createScalabilityES_OriginalJSONDefFile() {
        SimpleES sses = SimpleES.newScalabilityES();
        try {
            sses.serializeToJSON(new File(SCALABILITYEXECUTIONSPECJSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    // B) -- Methods that can the create Geographica3 execution specification from
    //       custom JSON definition files    
    /**
     * Deserialize from JSON file
     *
     * @param fileName
     * @return GeographicaDataSet object
     */
    public static IExecutionSpec deserializeFromJSON(String fileName) {
        File serObjFile = new File(fileName);
        // create the mapper
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        SimpleES es = null;
        try {
            es = mapper.readValue(serObjFile, new TypeReference<SimpleES>() {
            });
        } catch (IOException ex) {
            logger.info(ex.getMessage());
        }
        return es;
    }

    public static void main(String[] args) {
        // Create all execution specifications
        ExecutionSpecUtil.createMicroES_OriginalJSONDefFile();
        ExecutionSpecUtil.createMacroES_OriginalJSONDefFile();
        ExecutionSpecUtil.createScalabilityES_OriginalJSONDefFile();
        // Load all execution specifications from the original JSON spec files
        IExecutionSpec mies = ExecutionSpecUtil.deserializeFromJSON(MICROEXECUTIONSPECJSONDEF_FILE);
        IExecutionSpec maes = ExecutionSpecUtil.deserializeFromJSON(MACROEXECUTIONSPECJSONDEF_FILE);
        IExecutionSpec sces = ExecutionSpecUtil.deserializeFromJSON(SCALABILITYEXECUTIONSPECJSONDEF_FILE);
    }
}
