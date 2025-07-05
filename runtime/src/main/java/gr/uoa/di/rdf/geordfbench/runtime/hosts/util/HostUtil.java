package gr.uoa.di.rdf.geordfbench.runtime.hosts.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import gr.uoa.di.rdf.geordfbench.runtime.hosts.impl.SimpleHost;
import gr.uoa.di.rdf.geordfbench.runtime.os.impl.DebianOS;
import gr.uoa.di.rdf.geordfbench.runtime.os.impl.UbuntuBionicOS;
import java.io.File;
import java.io.IOException;
import gr.uoa.di.rdf.geordfbench.runtime.hosts.IHost;
import gr.uoa.di.rdf.geordfbench.runtime.os.impl.UbuntuJammyOS;
import gr.uoa.di.rdf.geordfbench.runtime.os.impl.Windows10OS;
import static gr.uoa.di.rdf.geordfbench.runtime.hosts.IHost.*;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import gr.uoa.di.rdf.geordfbench.runtime.os.IOS;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public class HostUtil {

    // --- Static members -----------------------------
    static org.apache.log4j.Logger logger
            = org.apache.log4j.Logger.getLogger(HostUtil.class.getSimpleName());
    static final String HOSTJSONDEFS_DIR = "../json_defs/hosts/".replace("/", SEP);
    static final String UBUNTU_VMA_TIOAJSONDEF_FILE = HOSTJSONDEFS_DIR + "ubuntu_vma_tioaHOSToriginal.json";
    static final String TELEIOS3JSONDEF_FILE = HOSTJSONDEFS_DIR + "teleios3HOSToriginal.json";
    static final String TIOA_PAVILIONDV7JSONDEF_FILE = HOSTJSONDEFS_DIR + "tioa-paviliondv7HOSToriginal.json";
    static final String NUC8i7BEHJSONDEF_FILE = HOSTJSONDEFS_DIR + "nuc8i7behHOSToriginal.json";
    static final String WIN10_WORKJSONDEF_FILE = HOSTJSONDEFS_DIR + "win10_workHOSToriginal.json";

    // --- Methods -----------------------------------
    // A) -- Methods that can re-create the JSON definition files
    //       for GeoRDFBench standard hosts
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

    /**
     * Creates the JSON definition file for the win10_work host in
     * Geographica/runtime/src/main/resources/json_defs/hosts/win10_workHOSToriginal.json
     *
     */
    public static void createWIN10_WORK_Host_OriginalJSONDefFile() {
        SimpleHost win10_work
                = new SimpleHost("win10_work",
                        "localhost",
                        11, // GB
                        new Windows10OS(),
                        "F:\\VM_Shared\\PHD\\Geographica2_Datasets",
                        "F:\\VM_Shared\\PHD",
                        "F:\\VM_Shared\\PHD\\Results_Store\\VM_Results");
        try {
            win10_work.serializeToJSON(new File(WIN10_WORKJSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates a transient JSON definition file for the current host in
     * current_host.json
     *
     * @param testHostFile
     */
    public static void createCurrent_TestHost_JSONDefFile(File testHostFile) {
        String hostName = "";
        String IP = "";
        int RAM;
        IOS os;

        try {
            hostName = InetAddress.getLocalHost().getHostName();
            IP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            logger.error(ex.getMessage());
        }
        long osrambytes = ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean())
                .getTotalPhysicalMemorySize();
        RAM = (int) (osrambytes / (1024 * 1024 * 1024));
        if (isLinux()) {
            os = new UbuntuJammyOS();
        } else if (isWindows()) {
            os = new Windows10OS();
        } else {
            throw new RuntimeException("Could not identify host's operating system!");
        }
        // find the absolute path of the test resources folder
        File p = new File("src/test/resources".replace("/", SEP));
        String TEST_RESOURCES_DIR = p.getAbsolutePath();
        SimpleHost currentHost
                = new SimpleHost(hostName,
                        IP,
                        RAM, // GB
                        os,
                        TEST_RESOURCES_DIR + SEP + "RDFDataFiles",
                        TEST_RESOURCES_DIR,
                        TEST_RESOURCES_DIR + SEP + "Results_Store");
        try {
            currentHost.serializeToJSON(testHostFile);
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    // B) -- Methods that can the create GeoRDFBench IHost from
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

    /**
     * Creates a geographica host from a customized JSON definition file which
     * contains the serialized host details.
     *
     * @param fileName the file path of the JSON definition file
     *
     * @return
     */
    public static IHost deserializeFromJSONString(String jsonSpec) {
        // create the mapper
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        SimpleHost sh = null;
        try {
            sh = mapper.readValue(jsonSpec, new TypeReference<SimpleHost>() {
            });
        } catch (IOException ex) {
            logger.info(ex.getMessage());
        }
        // sh.initializeAfterDeserialization();
        return sh;
    }

    public static void main(String[] args) {
        // ubuntu-vma-tioa host
        HostUtil.createUBUNTU_VMA_TIOA_Host_OriginalJSONDefFile();
        IHost vm = HostUtil.deserializeFromJSON(HostUtil.UBUNTU_VMA_TIOAJSONDEF_FILE);
        logger.info(vm.serializeToJSON());
        // teleios3.di.uoa.gr
        HostUtil.createTELEIOS3_Host_OriginalJSONDefFile();
        IHost teleios3 = HostUtil.deserializeFromJSON(HostUtil.TELEIOS3JSONDEF_FILE);
        logger.info(teleios3.serializeToJSON());
        // tioa-paviliondv7 host
        HostUtil.createTIOA_PAVILIONDV7_Host_OriginalJSONDefFile();
        IHost paviliondv7 = HostUtil.deserializeFromJSON(HostUtil.TIOA_PAVILIONDV7JSONDEF_FILE);
        logger.info(paviliondv7.serializeToJSON());
        // nuc8i7beh host
        HostUtil.createNUC8i7BEH_Host_OriginalJSONDefFile();
        IHost nuc8i7beh = HostUtil.deserializeFromJSON(HostUtil.NUC8i7BEHJSONDEF_FILE);
        logger.info(nuc8i7beh.serializeToJSON());
        // win10 host
        HostUtil.createWIN10_WORK_Host_OriginalJSONDefFile();
        IHost win10_work = HostUtil.deserializeFromJSON(HostUtil.WIN10_WORKJSONDEF_FILE);
        logger.info(win10_work.serializeToJSON());
    }
}
