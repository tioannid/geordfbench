package gr.uoa.di.rdf.Geographica3.runtime.reportsource.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import gr.uoa.di.rdf.Geographica3.runtime.reportsource.IReportSource;
import gr.uoa.di.rdf.Geographica3.runtime.reportsource.impl.JDBCRepSrc;
import gr.uoa.di.rdf.Geographica3.runtime.reportsource.impl.PostgreSQLRepSrc;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public class ReportSourceUtil {

    // --- Static members -----------------------------
    static org.apache.log4j.Logger logger
            = org.apache.log4j.Logger.getLogger(ReportSourceUtil.class.getSimpleName());
    static final String REPORTSOURCEJSONDEFS_DIR = "../json_defs/reportsources/";
    static final String UBUNTU_VMA_TIOA_REP_SRCJSONDEF_FILE = REPORTSOURCEJSONDEFS_DIR + "ubuntu_vma_tioaRepSrcoriginal.json";

    // --- Methods -----------------------------------
    // A) -- Methods that can re-create the JSON definition files
    //       for Geographica3 report sources
    /**
     * Creates the JSON definition file for the PostgreSQL report source in
     * Geographica/runtime/src/main/resources/json_defs/reportsources/ubuntu_vma_tioaRepSrcoriginal.json
     *
     */
    public static void createUBUNTU_VMA_TIOA_REP_SRC_OriginalJSONDefFile() {
        try {
            PostgreSQLRepSrc pgsrc
                    = new PostgreSQLRepSrc(
                            "192.168.1.66", "localhost", 5432,
                            "geographica3",
                            "geographica3", "geographica3");
            pgsrc.serializeToJSON(new File(UBUNTU_VMA_TIOA_REP_SRCJSONDEF_FILE));
        } catch (SQLException | IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    // B) -- Methods that can the create Geographica3 Report Source from JSON
    /**
     * Deserialize from JSON file
     *
     * @param fileName
     * @return IReportSource object
     */
    public static IReportSource deserializeFromJSON(String fileName) {
        File serObjFile = new File(fileName);
        // create the mapper
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        JDBCRepSrc rs = null;
        try {
            rs = mapper.readValue(serObjFile, new TypeReference<JDBCRepSrc>() {
            });
        } catch (IOException ex) {
            logger.info(ex.getMessage());
        }
        rs.initializeAfterDeserialization();
        return rs;
    }

    public static void main(String[] args) {
        // UBUNTU_VMA_TIOA PostgreSQL report source
        ReportSourceUtil.createUBUNTU_VMA_TIOA_REP_SRC_OriginalJSONDefFile();
        IReportSource rptSrc = ReportSourceUtil.deserializeFromJSON(UBUNTU_VMA_TIOA_REP_SRCJSONDEF_FILE);
        logger.info(rptSrc.serializeToJSON());
    }
}
