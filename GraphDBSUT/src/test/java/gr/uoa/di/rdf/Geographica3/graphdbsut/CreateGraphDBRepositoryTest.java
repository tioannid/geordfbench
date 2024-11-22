package gr.uoa.di.rdf.Geographica3.graphdbsut;

import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import static gr.uoa.di.rdf.Geographica3.runtime.hosts.IHost.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;

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
    final String GEN_REPO_SCRIPT_TEST_FULLPATH = GraphDBSUT_BASE_DIR + SEP + GEN_REPO_SCRIPT_TEST;
    final String GEN_REPO_LOG_TEST = "genrepo_test.log";
    final String GEN_REPO_LOG_TEST_FULLPATH = GraphDBSUT_BASE_DIR + SEP + GEN_REPO_LOG_TEST;
    final String GraphDB_PrepareScript_BASE_DIR = GraphDBSUT_BASE_DIR
            + SEP + "../scripts".replace("/", SEP);
    final String DEL_REPO_SCRIPT_TEST = "delrepo_test.sh";
    final String DEL_REPO_SCRIPT_TEST_FULLPATH = GraphDBSUT_BASE_DIR + SEP + DEL_REPO_SCRIPT_TEST;
    final String DEL_REPO_LOG_TEST = "delrepo_test.log";
    final String DEL_REPO_LOG_TEST_FULLPATH = GraphDBSUT_BASE_DIR + SEP + DEL_REPO_LOG_TEST;


    /* create a bash script in GraphDBSUT dir
       which will delete the scalability_10K repo
       if it exists
     */
    void createDeleteScriptInGraphDBSUTBaseDir() {
        PrintWriter os = null;
        try {
            os = new PrintWriter(new FileWriter(DEL_REPO_SCRIPT_TEST_FULLPATH));
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
    void createRepoGenScriptInGraphDBSUTBaseDir() {
        PrintWriter os = null;
        try {
            os = new PrintWriter(new FileWriter(GEN_REPO_SCRIPT_TEST_FULLPATH));
            os.println("#!/bin/bash");
            os.println("set -o allexport");
            os.println("CWD=`pwd`");
            os.println("cd ../scripts");
            os.println("source prepareRunEnvironment.sh VM GraphDBSUT GraphDB_Scal10K_`date -I`");
            os.println("./printRunEnvironment.sh");
            os.println("cd ${CWD}/scripts/CreateRepos");
            os.println("./createAllGraphDBRepos.sh");
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

    @BeforeAll
    public void setupAll() {
        System.out.println("BEFORE ALL EXPERIMENTS");
    }

    @BeforeEach
    public void setup() {
        System.out.println("Should print before each test");
    }

    @Test
    @Order(1)
    @DisplayName("Should Create A Repo Even If It Exists")
    public void shouldCreateARepoEvenIfItExists() {
        int exitCode = -1;

        createDeleteScriptInGraphDBSUTBaseDir();
        // Process process = Runtime.getRuntime().exec(cmd);
        ProcessBuilder pb_del = new ProcessBuilder();
        File del_log = new File(DEL_REPO_LOG_TEST_FULLPATH);
        pb_del.redirectOutput(del_log);
        pb_del.command("/bin/bash", "-c", DEL_REPO_SCRIPT_TEST_FULLPATH);
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
        del_log.delete();
        new File(DEL_REPO_SCRIPT_TEST_FULLPATH).delete();

        createRepoGenScriptInGraphDBSUTBaseDir();
        //String cmd = String.format("/bin/bash -c %s", GEN_REPO_SCRIPT_TEST);
        // Process process = Runtime.getRuntime().exec(cmd);
        ProcessBuilder pb_create = new ProcessBuilder();
        File create_log = new File(GEN_REPO_LOG_TEST_FULLPATH);
        pb_create.redirectOutput(create_log);
        pb_create.command("/bin/bash", "-c", GEN_REPO_SCRIPT_TEST_FULLPATH);
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
        // find the GraphDBDataDir variable value from the GEN_REPO_LOG_TEST_FULLPATH
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
        new File(GEN_REPO_SCRIPT_TEST_FULLPATH).delete();
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

    @AfterEach
    public void tearDown() {
        System.out.println("Should print after each test");
    }

    @AfterAll
    public void tearDownAll() {
        System.out.println("AFTER ALL EXPERIMENTS");
    }
}
