package gr.uoa.di.rdf.Geographica3.runtime.reportsource.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import gr.uoa.di.rdf.Geographica3.runtime.reportsource.IReportSource;
import gr.uoa.di.rdf.Geographica3.runtime.reportsource.impl.H2EmbeddedRepSrc;
import gr.uoa.di.rdf.Geographica3.runtime.reportsource.impl.PostgreSQLRepSrc;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.log4j.Logger;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public class ReportSourceUtil {

    // --- Static members -----------------------------
    static Logger logger
            = Logger.getLogger(ReportSourceUtil.class.getSimpleName());
    static final String REPORTSOURCEJSONDEFS_DIR = "../json_defs/reportsources/";
    static final String UBUNTU_VMA_TIOA_REP_SRCJSONDEF_FILE = REPORTSOURCEJSONDEFS_DIR + "ubuntu_vma_tioaRepSrcoriginal.json";
    static final String H2_EMBEDDED_REP_SRCJSONDEF_FILE = REPORTSOURCEJSONDEFS_DIR + "h2EmbeddedRepSrcoriginal.json";

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
                            "10.0.2.15", "localhost", 5432,
                            "geordfbench",
                            "geordfbench", "geordfbench");
            pgsrc.serializeToJSON(new File(UBUNTU_VMA_TIOA_REP_SRCJSONDEF_FILE));
        } catch (SQLException | IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates the JSON definition file for the H2 eembedded report source in
     * Geographica/runtime/src/main/resources/json_defs/reportsources/h2EmbeddedRepSrcoriginal.json
     *
     */
    public static void createH2_Embedded_REP_SRC_OriginalJSONDefFile() {
        try {
            H2EmbeddedRepSrc h2src
                    = new H2EmbeddedRepSrc(
                            "../scripts/h2embeddedreportsource",
                            "geordfbench",
                            "sa", "");
            h2src.serializeToJSON(new File(H2_EMBEDDED_REP_SRCJSONDEF_FILE));
        } catch (IOException ex) {
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
        IReportSource rs = null;
        try {
            rs = mapper.readValue(serObjFile, new TypeReference<PostgreSQLRepSrc>() {
            });
        } catch (IOException ex) {
            try {
                rs = mapper.readValue(serObjFile, new TypeReference<H2EmbeddedRepSrc>() {
                });
            } catch (IOException e) {
                logger.info(ex.getMessage() + "\n" + e.getMessage());
            }
        }
        rs.initializeAfterDeserialization();
        return rs;
    }

    public static void main(String[] args) {
        IReportSource rptSrc;
        // UBUNTU_VMA_TIOA PostgreSQL report source
        ReportSourceUtil.createUBUNTU_VMA_TIOA_REP_SRC_OriginalJSONDefFile();
        rptSrc = ReportSourceUtil.deserializeFromJSON(UBUNTU_VMA_TIOA_REP_SRCJSONDEF_FILE);
        logger.info(rptSrc.serializeToJSON());
        // H2 Embedded report source
        ReportSourceUtil.createH2_Embedded_REP_SRC_OriginalJSONDefFile();
        rptSrc = ReportSourceUtil.deserializeFromJSON(H2_EMBEDDED_REP_SRCJSONDEF_FILE);
        logger.info(rptSrc.serializeToJSON());
    }
}
