package gr.uoa.di.rdf.geordfbench.graphdbsut;

import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import static gr.uoa.di.rdf.geordfbench.runtime.hosts.IHost.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

/**
 * A class that checks the following use cases: Create new repository only if
 * repository does not exist Overwrite existing repository Query an empty repo
 * (no of triples) Load N-Triple file in existing repo Query a loaded repo (no
 * of triples)
 *
 * @author Theofilos Ioannidis <tioannid@di.uoa.gr>
 * @creationdate 19/11/2024
 * @updatedate 19/11/2024
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Create GraphDB Repository Test")
public class CreateGraphDBRepositoryTest {

    // Static Members
    // Data Members
    // assume that workingdir/"user.dir" is GraphDBSUT
    final String REPO_ID = "scalability_10K";
    final String GraphDBSUT_BASE_DIR = System.getProperty("user.dir");
    final String GEN_REPO_SCRIPT_TEST = "genrepo_test.sh";
    final String DEL_REPO_SCRIPT_TEST = "delrepo_test.sh";

    public void createARepoEvenIfItExists(int testIndex,
            boolean enableGeoSPARQLPlugin,
            String indexingAlgorithm, int indexingPrecision) {
        String gen_repo_script = GEN_REPO_SCRIPT_TEST.replace(".", "_" + testIndex + ".");
        String gen_repo_script_fullpath = GraphDBSUT_BASE_DIR + SEP + gen_repo_script;
        String gen_repo_log = GEN_REPO_SCRIPT_TEST.replace(".sh", "_" + testIndex + ".log");
        String gen_repo_log_fullpath = GraphDBSUT_BASE_DIR + SEP + gen_repo_log;
        String del_repo_script = DEL_REPO_SCRIPT_TEST.replace(".", "_" + testIndex + ".");
        String del_repo_script_fullpath = GraphDBSUT_BASE_DIR + SEP + del_repo_script;
        String del_repo_log = DEL_REPO_SCRIPT_TEST.replace(".sh", "_" + testIndex + ".log");
        String del_repo_log_fullpath = GraphDBSUT_BASE_DIR + SEP + del_repo_log;
        int exitCode = -1;

        System.out.println("|==> TEST Description: Create "
                + REPO_ID
                + " repo with GeoSPARQL plugin " + ((enableGeoSPARQLPlugin)
                        ? "enabled (algo, prec) = (" + indexingAlgorithm + ", " + indexingPrecision + ")"
                        : "disabled"));
        createDeleteScriptInGraphDBSUTBaseDir(del_repo_script_fullpath);
        // Process process = Runtime.getRuntime().exec(cmd);
        ProcessBuilder pb_del = new ProcessBuilder();
        File del_log = new File(del_repo_log_fullpath);
        pb_del.redirectOutput(del_log);
        pb_del.command("/bin/bash", "-c", del_repo_script_fullpath);
        Process process_del = null;
        try {
            process_del = pb_del.start();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        // consume the input stream connected to the normal output of the subprocess
        new BufferedReader(new InputStreamReader(process_del.getInputStream())).lines()
                .forEach(System.out::println);
        try {
            exitCode = process_del.waitFor();
        } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
        process_del.destroy();
        // display the log of the generate repo script
        BufferedReader bf = null;
        try {
            bf = new BufferedReader(new InputStreamReader(new FileInputStream(del_log)));
            bf.lines().forEach(System.out::println);
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        } finally {
            try {
                bf.close();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
        // remove script and log
//        del_log.delete();
        new File(del_repo_script_fullpath).delete();

        createRepoGenScriptInGraphDBSUTBaseDir(gen_repo_script_fullpath,
                enableGeoSPARQLPlugin, indexingAlgorithm, indexingPrecision);
        //String cmd = String.format("/bin/bash -c %s", GEN_REPO_SCRIPT_TEST);
        // Process process = Runtime.getRuntime().exec(cmd);
        ProcessBuilder pb_create = new ProcessBuilder();
        File create_log = new File(gen_repo_log_fullpath);
        pb_create.redirectOutput(create_log);
        pb_create.command("/bin/bash", "-c", gen_repo_script_fullpath);
        Process process_create = null;
        try {
            process_create = pb_create.start();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        // consume the input stream connected to the normal output of the subprocess
        new BufferedReader(new InputStreamReader(process_create.getInputStream())).lines()
                .forEach(System.out::println);
        try {
            exitCode = process_create.waitFor();
        } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
        process_create.destroy();
        // find the GraphDBDataDir variable value from the GEN_REPO_LOG_TEST_1_FULLPATH
        String GraphDBDataDir = getEnvVarValueInFile("GraphDBDataDir", create_log);
        // display the log of the generate repo script      
        try {
            bf = new BufferedReader(new InputStreamReader(new FileInputStream(create_log)));
            bf.lines().forEach(System.out::println);
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        } finally {
            try {
                bf.close();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
        // remove script and log
        create_log.delete();
        new File(gen_repo_script_fullpath).delete();
        del_log.delete();
        // check if scalability_10K folder has been generated
        File repDir = new File(GraphDBDataDir + SEP + ("repositories/" + REPO_ID).replace("/", SEP));
        boolean exists = repDir.exists();
        if (exists) {
            System.out.println("Directory " + repDir.getAbsolutePath() + " exists!");
        } else {
            System.out.println("Directory " + repDir.getAbsolutePath() + " does not exist.");
        }
        Assertions.assertTrue(exists);
        Assertions.assertTrue(exitCode == 0);
    }

    /* create a bash script in GraphDBSUT dir
       which will delete the scalability_10K repo
       if it exists
     */
    void createDeleteScriptInGraphDBSUTBaseDir(String del_repo_script_fullpath) {
        PrintWriter os = null;
        try {
            System.out.println("Creating " + del_repo_script_fullpath + " script to remove repo if it exists.");
            os = new PrintWriter(new FileWriter(del_repo_script_fullpath));
            os.println("#!/bin/bash");
            os.println("set -o allexport");
            os.println("CWD=`pwd`");
            os.println("cd ../scripts");
            os.println("source prepareRunEnvironment.sh VM GraphDBSUT GraphDB_Scal10K_`date -I`");
            // check if repo directory is already present
            os.println("RepoID=" + REPO_ID);
            os.println("RepoDir=\"${GraphDBDataDir}/repositories/${RepoID}\"");
            os.println("dirExists=FALSE");
            os.println("if [ -d \"$RepoDir\" ]; then\n"
                    + "    dirExists=TRUE\n"
                    + "fi");
            // check if GraphDB server is running
            os.println("GraphDBIsRunning=`pgrep -f graphdb`");
            // if repo exists delete it by using GraphDB REST API
            os.println("if [ \"$dirExists\" == \"TRUE\" ]; then\n"
                    + "    echo \"${RepoID} already exists. Removing it...\"\n"
                    + "    if [ -z ${GraphDBIsRunning} ]; then # GraphDB is not running\n"
                    + "       ${GraphDBBaseDir}/bin/graphdb -d # start GraphDB\n"
                    + "       echo \"Starting GraphDB in order to remove existing repository...\"\n"
                    + "       read -t 10 -p \"\"\n"
                    + "       GraphDBIsRunning=`pgrep -f graphdb`\n"
                    + "    fi");

            os.println("   curl -X DELETE \"http://localhost:7200/rest/repositories/${RepoID}\"");
            os.println("   # STEP 3: Terminate the GraphDB server process, if it's running\n"
                    + "    if [ -n ${GraphDBIsRunning} ]; then # GraphDB is running\n"
                    + "       echo \"Stopping GraphDB...\"\n"
                    + "       kill -9 ${GraphDBIsRunning} > /dev/null 2>&1\n"
                    + "    fi");
            os.println("fi");
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            os.close();
        }
    }

    /* create a bash script in GraphDBSUT dir
       which will generate the scalability_10K repo 
     */
    void createRepoGenScriptInGraphDBSUTBaseDir(String filePath,
            boolean enableGeoSPARQLPlugin,
            String indexingAlgorithm, int indexingPrecision) {
        PrintWriter os = null;
        try {
            System.out.println("Creating " + filePath + " script to generate repo.");
            os = new PrintWriter(new FileWriter(filePath));
            os.println("#!/bin/bash");
            os.println("CWD=`pwd`");
            os.println("cd ../scripts");
            os.println("set -o allexport # or we could use: set -a");
            os.println("source prepareRunEnvironment.sh VM GraphDBSUT GraphDB_Scal10K_`date -I`");
            os.println("# change some environment variables");
            if (enableGeoSPARQLPlugin) {
                os.println("export EnableGeoSPARQLPlugin=true");
                if (indexingAlgorithm.equalsIgnoreCase("QUAD")
                        || indexingAlgorithm.equalsIgnoreCase("GEOHASH")) {
                    os.println("export IndexingAlgorithm=" + indexingAlgorithm.toLowerCase());
                }
                os.println("export IndexingPrecision=" + indexingPrecision);
            } else {
                os.println("# export EnableGeoSPARQLPlugin=true");
                os.println("# export IndexingAlgorithm = quad");
                os.println("# export IndexingPrecision = 11");
            }
            os.println("./printRunEnvironment.sh");
            os.println("cd ${CWD}/scripts/CreateRepos");
            os.println("./createAllGraphDBRepos.sh");
            os.println("set +o allexport # or we could use: set +a");
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            os.close();
        }
    }

    /* retrieve the value of an environment variable by scanning
       a listing in a file
     */
    String getEnvVarValueInFile(String envVar, File f) {
        String line = "", value = "";
        BufferedReader reader = null;
        boolean found = false;
        try {
            // create a buffered reader
            reader = new BufferedReader(new FileReader(f));
            // find the line where the environment variable value is printed
            // ENV_VAR = xxxxyyy
            while (!found && (line = reader.readLine()) != null) {
                found = line.contains(envVar + " =");
            }
            if (found) {
                value = line.split("=")[1].trim();
            } else {
                System.out.println("The value of environment variable " + envVar
                        + " has not been found in file " + f.getAbsolutePath());
            }
            reader.close();
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return value;
    }

    @Test
    @EnabledOnOs({OS.LINUX})
    @Order(1)
    @DisplayName("Should Create A Repo Even If It Exists")
    // GeoSPARQL plugin is disabled
    public void shouldCreateARepoEvenIfItExists() {
        createARepoEvenIfItExists(1, false, "quad", 11);
    }

    @Test
    @EnabledOnOs({OS.LINUX})
    @Order(2)
    @DisplayName("Should Create A Quad 20 GeoSPARQL Plugin Enabled Repo Even If It Exists")
    // The default 11 precision value of the quad prefix is about ±2.5km on the equator. 
    // When increased to 20 the precision goes down to ±6m accuracy.
    public void shouldCreateAQuad20GeoSPARQLPluginEnabledRepoEvenIfItExists() {
        createARepoEvenIfItExists(2, true, "quad", 20);
    }

    @Test
    @EnabledOnOs({OS.LINUX})
    @Order(3)
    @DisplayName("Should Create A Geohash 11 GeoSPARQL Plugin Enabled Repo Even If It Exists")
    // The geohash prefix tree with precision 11 results ±1m on the equator.
    public void shouldCreateAGeohash11GeoSPARQLPluginEnabledRepoEvenIfItExists() {
        createARepoEvenIfItExists(3, true, "geohash", 11);
    }

    @BeforeAll
    public void setupAll() {
        System.out.println("BEFORE ALL TESTS");
    }

    @BeforeEach
    public void setup() {
        System.out.println("|==> TEST Start");
    }

    @AfterEach
    public void tearDown() {
        System.out.println("|<== TEST End");
    }

    @AfterAll
    public void tearDownAll() {
        System.out.println("AFTER ALL TESTS");
    }
}
