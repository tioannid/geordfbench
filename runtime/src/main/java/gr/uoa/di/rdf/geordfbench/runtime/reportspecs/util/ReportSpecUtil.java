package gr.uoa.di.rdf.geordfbench.runtime.reportspecs.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import gr.uoa.di.rdf.geordfbench.runtime.reportspecs.impl.SimpleReportSpec;
import java.io.File;
import java.io.IOException;
import gr.uoa.di.rdf.geordfbench.runtime.reportspecs.IReportSpec;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public class ReportSpecUtil {

    // --- Static members -----------------------------
    static org.apache.log4j.Logger logger
            = org.apache.log4j.Logger.getLogger(ReportSpecUtil.class.getSimpleName());
    static final String REPORTSPECJSONDEFS_DIR = "../json_defs/reportspecs/";
    static final String SIMPLE_REPORT_SPECJSONDEF_FILE = REPORTSPECJSONDEFS_DIR + "simplereportspec_original.json";

    // --- Methods -----------------------------------
    // A) -- Methods that can re-create the JSON definition files
    //       for GeoRDFBench report specs
    /**
     * Creates the JSON definition file for the simple report spec in
     * Geographica/runtime/src/main/resources/json_defs/reportspecs/simplereportspecToriginal.json
     *
     */
    public static void createSIMPLE_REPORT_SPEC_OriginalJSONDefFile() {
        SimpleReportSpec simplerptspec
                = new SimpleReportSpec(3); // print 3 result 
        simplerptspec.serializeToJSON(new File(SIMPLE_REPORT_SPECJSONDEF_FILE));
    }

    // B) -- Methods that can the create GeoRDFBench Report Spec from
    /**
     * Deserialize from JSON file
     *
     * @param fileName
     * @return IReportSpec object
     */
    public static IReportSpec deserializeFromJSON(String fileName) {
        File serObjFile = new File(fileName);
        // create the mapper
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        SimpleReportSpec rs = null;
        try {
            rs = mapper.readValue(serObjFile, new TypeReference<SimpleReportSpec>() {
            });
        } catch (IOException ex) {
            logger.info(ex.getMessage());
        }
        // rs.initializeAfterDeserialization();
        return rs;
    }    
    public static void main(String[] args) {
        // Simple report spec
        ReportSpecUtil.createSIMPLE_REPORT_SPEC_OriginalJSONDefFile();
        IReportSpec rptSpec = ReportSpecUtil.deserializeFromJSON(SIMPLE_REPORT_SPECJSONDEF_FILE);
        logger.info(rptSpec.serializeToJSON());
    }
}
