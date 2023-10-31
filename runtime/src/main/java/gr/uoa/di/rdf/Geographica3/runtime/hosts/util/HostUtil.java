package gr.uoa.di.rdf.Geographica3.runtime.hosts.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import gr.uoa.di.rdf.Geographica3.runtime.hosts.impl.SimpleHost;
import gr.uoa.di.rdf.Geographica3.runtime.os.impl.DebianOS;
import gr.uoa.di.rdf.Geographica3.runtime.os.impl.UbuntuBionicOS;
import java.io.File;
import java.io.IOException;
import gr.uoa.di.rdf.Geographica3.runtime.hosts.IHost;
import gr.uoa.di.rdf.Geographica3.runtime.os.impl.UbuntuJammyOS;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public class HostUtil {

    // --- Static members -----------------------------
    static org.apache.log4j.Logger logger
            = org.apache.log4j.Logger.getLogger(HostUtil.class.getSimpleName());
    static final String HOSTJSONDEFS_DIR = "../json_defs/hosts/";
    static final String UBUNTU_VMA_TIOAJSONDEF_FILE = HOSTJSONDEFS_DIR + "ubuntu_vma_tioaHOSToriginal.json";
    static final String TELEIOS3JSONDEF_FILE = HOSTJSONDEFS_DIR + "teleios3HOSToriginal.json";
    static final String TIOA_PAVILIONDV7JSONDEF_FILE = HOSTJSONDEFS_DIR + "tioa-paviliondv7HOSToriginal.json";
    static final String NUC8i7BEHJSONDEF_FILE = HOSTJSONDEFS_DIR + "nuc8i7behHOSToriginal.json";

    // --- Methods -----------------------------------
    // A) -- Methods that can re-create the JSON definition files
    //       for Geographica3 standard hosts
    /**
     * Creates the JSON definition file for the ubuntu-vma-tioa host in
     * Geographica/runtime/src/main/resources/json_defs/hosts/ubuntu_vma_tioaHOSToriginal.json
     *
     */
    public static void createUBUNTU_VMA_TIOA_Host_OriginalJSONDefFile() {
        SimpleHost ubuntu_vma_tioa
                = new SimpleHost("ubuntu-vma-tioa",
                        "192.168.1.66",
                        11, // GB
                        new UbuntuBionicOS(),
                        "/media/sf_VM_Shared/PHD/Geographica2_Datasets",
                        "/media/sf_VM_Shared/PHD",
                        "/media/sf_VM_Shared/PHD/Results_Store/VM_Results");
        try {
            ubuntu_vma_tioa.serializeToJSON(new File(UBUNTU_VMA_TIOAJSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates the JSON definition file for the teleios3 host in
     * Geographica/runtime/src/main/resources/json_defs/hosts/teleios3HOSToriginal.json
     *
     */
    public static void createTELEIOS3_Host_OriginalJSONDefFile() {
        SimpleHost ubuntu_vma_tioa
                = new SimpleHost("teleios3",
                        "10.0.10.12",
                        32, // GB
                        new UbuntuBionicOS("Ubuntu-precise",
                                "/bin/sh", "sync", "sudo /sbin/sysctl vm.drop_caches=3"),
                        "/home/journal/Geographica_Datasets",
                        "/home/journal",
                        "/home/journal");
        try {
            ubuntu_vma_tioa.serializeToJSON(new File(TELEIOS3JSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates the JSON definition file for the tioa-paviliondv7 host in
     * Geographica/runtime/src/main/resources/json_defs/hosts/tioa-paviliondv7HOSToriginal.json
     *
     */
    public static void createTIOA_PAVILIONDV7_Host_OriginalJSONDefFile() {
        SimpleHost tioa_paviliondv7
                = new SimpleHost("tioa-paviliondv7",
                        "192.168.1.6",
                        16, // GB
                        new DebianOS(),
                        "/dataDisk/Geographica2_Datasets",
                        "/dataDisk",
                        "/dataDisk/Results_Store");
        try {
            tioa_paviliondv7.serializeToJSON(new File(TIOA_PAVILIONDV7JSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates the JSON definition file for the nuc8i7beh host in
     * Geographica/runtime/src/main/resources/json_defs/hosts/nuc8i7behHOSToriginal.json
     *
     */
    public static void createNUC8i7BEH_Host_OriginalJSONDefFile() {
        SimpleHost nuc8i7beh
                = new SimpleHost("NUC8i7BEH",
                        "192.168.1.44",
                        32, // GB
                        new UbuntuJammyOS(),
                        "/data/Geographica2_Datasets",
                        "/data",
                        "/data/Results_Store");
        try {
            nuc8i7beh.serializeToJSON(new File(NUC8i7BEHJSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    // B) -- Methods that can the create Geographica3 IHost from
    /**
     * Creates a geographica host from a customized JSON definition file which
     * contains the serialized host details.
     *
     * @param fileName the file path of the JSON definition file
     *
     * @return
     */
    public static IHost deserializeFromJSON(String fileName) {
        File serObjFile = new File(fileName);
        // create the mapper
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        SimpleHost sh = null;
        try {
            sh = mapper.readValue(serObjFile, new TypeReference<SimpleHost>() {
            });
        } catch (IOException ex) {
            logger.info(ex.getMessage());
        }
        // sh.initializeAfterDeserialization();
        return sh;
    }

    public static void main(String[] args) {
//        // ubuntu-vma-tioa host
//        HostUtil.createUBUNTU_VMA_TIOA_Host_OriginalJSONDefFile();
//        IHost vm = HostUtil.deserializeFromJSON(HostUtil.UBUNTU_VMA_TIOAJSONDEF_FILE);
//        logger.info(vm.serializeToJSON());
//        // teleios3.di.uoa.gr
//        HostUtil.createTELEIOS3_Host_OriginalJSONDefFile();
//        IHost teleios3 = HostUtil.deserializeFromJSON(HostUtil.TELEIOS3JSONDEF_FILE);
//        logger.info(teleios3.serializeToJSON());
//        // tioa-paviliondv7 host
//        HostUtil.createTIOA_PAVILIONDV7_Host_OriginalJSONDefFile();
//        IHost paviliondv7 = HostUtil.deserializeFromJSON(HostUtil.TIOA_PAVILIONDV7JSONDEF_FILE);
//        logger.info(paviliondv7.serializeToJSON());
//        // nuc8i7beh host
        HostUtil.createNUC8i7BEH_Host_OriginalJSONDefFile();
        IHost nuc8i7beh = HostUtil.deserializeFromJSON(HostUtil.NUC8i7BEHJSONDEF_FILE);
        logger.info(nuc8i7beh.serializeToJSON());
    }
}
