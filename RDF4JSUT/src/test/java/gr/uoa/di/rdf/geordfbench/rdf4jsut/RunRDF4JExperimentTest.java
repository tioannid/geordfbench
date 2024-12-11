package gr.uoa.di.rdf.geordfbench.rdf4jsut;

import java.io.File;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import static gr.uoa.di.rdf.geordfbench.runtime.hosts.IHost.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * A class that checks various Query Filtering options during experiment
 * execution with the detailed benchmark specifications.
 *
 * @author Theofilos Ioannidis <tioannid@di.uoa.gr>
 * @creationdate 26/08/2024
 * @updatedate 03/09/2024
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Test Experiment Query Filtering with Detailed Benchmark Specification")
public class RunRDF4JExperimentTest {

    // Data Members
    String[] argsNoQueryFilter,
            argsWithQueryInclusionFilter,
            argsWithQueryExclusionFilter;
    String JSON_DEFS_DIR;

    private final PrintStream standardOut = System.out;
    // Creates a FileOutputStream
    private final String FILEOUTSTREAM = "fileOutStream.txt";
    private FileOutputStream fileOutStream = null;
    private PrintStream outPrintStream = null;
    private File outputFile = null;

    @BeforeAll
    public void setupAll() {
        System.out.println("=> BEFORE ALL TESTS - EXPERIMENT: " + RunRDF4JExperimentTest.class.getSimpleName());
        // find the absolute path of the JSON Library in the test resources folder
        File p = new File("src/test/resources/json_defs".replace("/", SEP));
        JSON_DEFS_DIR = p.getAbsolutePath();
        String argLineNoQueryFilter
                = // No Query Filter is specified - 3 queries (0,1,2) expected in output
                "-rbd RDF4J_4.3.15_Repos/server "
                + "-expdesc RDF4JSUT_RunRDF4JExperimentTest "
                + "-ds " + JSON_DEFS_DIR + "/datasets/scalability_10Koriginal.json".replace("/", SEP) + " "
                + "-qs " + JSON_DEFS_DIR + "/querysets/scalabilityFuncQSoriginal.json".replace("/", SEP) + " "
                + "-es " + JSON_DEFS_DIR + "/executionspecs/scalabilityESoriginal_PRINT.json".replace("/", SEP) + " "
                + "-rs " + JSON_DEFS_DIR + "/reportspecs/simplereportspec_original.json".replace("/", SEP) + " "
                + "-rpsr " + JSON_DEFS_DIR + "/reportsources/ubuntu_vma_tioaRepSrcoriginal.json".replace("/", SEP) + " ";
        // based on OS choose an appropriate host spec
        argLineNoQueryFilter += "-h " + JSON_DEFS_DIR
                + ((isWindows())
                        ? "/hosts/win10_workHOSToriginal.json".replace("/", SEP)
                        : "/hosts/ubuntu_vma_tioaHOSToriginal.json".replace("/", SEP));
        argsNoQueryFilter = argLineNoQueryFilter.split(" ");
        String argLineWithQueryInclusionFilter = argLineNoQueryFilter // Query InclusionFilter specified - 2 queries (0,_,2) expected in output
                + " -qif \"0,2\"";
        argsWithQueryInclusionFilter = argLineWithQueryInclusionFilter.split(" ");
        String argLineWithQueryExclusionFilter = argLineNoQueryFilter // Query ExclusionFilter specified - 1 query (_,1,_) expected in output
                + " -qef \"0,2\"";
        argsWithQueryExclusionFilter = argLineWithQueryExclusionFilter.split(" ");
    }

    @BeforeEach
    public void setup() {
        System.out.println("===> BEFORE EACH TEST");
        outputFile = new File(("src/test/resources/" + FILEOUTSTREAM).replace("/", SEP));
        outputFile.delete();
        try {
            fileOutStream = new FileOutputStream(outputFile.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
        outPrintStream = new PrintStream(fileOutStream, true);

    }

    @Test
    @Order(1)
    @DisplayName("Should Display the 3 Scalability Ground Queries 0,1,2")
    public void shouldDisplay3ScalabilityGroundQueries() throws Exception {
        System.out.println("TEST: Should Display 3 Scalability Ground Queries 0, 1, 2");
        System.setOut(outPrintStream);
        RunRDF4JExperiment.main(this.argsNoQueryFilter);
        fileOutStream.close();
        BufferedReader br = new BufferedReader(new FileReader(outputFile));
        String line = "";
        boolean bl1 = false, bl2 = false, bl3 = false;
        while ((line = br.readLine()) != null) {
            if (line.contains("Query 0 -")) {
                bl1 = true;
            }
            if (line.contains("Query 1 -")) {
                bl2 = true;
            }
            if (line.contains("Query 2 -")) {
                bl3 = true;
            }
        }
        br.close();
        Assertions.assertTrue(bl1);
        Assertions.assertTrue(bl2);
        Assertions.assertTrue(bl3);
    }

    @Test
    @Order(2)
    @DisplayName("Should Display the 2 Included Scalability Ground Queries 0,2")
    public void shouldDisplayThe2IncludedScalabilityGroundQueries() throws Exception {
        System.out.println("TEST: Should Display The 2 Included Scalability Ground Queries 0, 2");
        System.setOut(outPrintStream);
        RunRDF4JExperiment.main(this.argsWithQueryInclusionFilter);
        fileOutStream.close();
        BufferedReader br = new BufferedReader(new FileReader(outputFile));
        String line = "";
        boolean bl1 = false, bl2 = false, bl3 = false;
        while ((line = br.readLine()) != null) {
            if (line.contains("Query 0 -")) {
                bl1 = true;
            }
            if (line.contains("Query 1 -")) {
                bl2 = true;
            }
            if (line.contains("Query 2 -")) {
                bl3 = true;
            }
        }
        br.close();
        Assertions.assertTrue(bl1);
        Assertions.assertFalse(bl2);
        Assertions.assertTrue(bl3);
    }

    @Test
    @Order(3)
    @DisplayName("Should Not Display the 2 Excluded Scalability Ground Queries 0,2")
    public void shouldNotDisplayThe2ExcludedScalabilityGroundQueries() throws Exception {
        System.out.println("TEST: Should Not Display The 2 Excluded Scalability Ground Queries 0, 2");
        System.setOut(outPrintStream);
        RunRDF4JExperiment.main(this.argsWithQueryExclusionFilter);
        fileOutStream.close();
        BufferedReader br = new BufferedReader(new FileReader(outputFile));
        String line = "";
        boolean bl1 = false, bl2 = false, bl3 = false;
        while ((line = br.readLine()) != null) {
            if (line.contains("Query 0 -")) {
                bl1 = true;
            }
            if (line.contains("Query 1 -")) {
                bl2 = true;
            }
            if (line.contains("Query 2 -")) {
                bl3 = true;
            }
        }
        br.close();
        Assertions.assertFalse(bl1);
        Assertions.assertTrue(bl2);
        Assertions.assertFalse(bl3);
    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
        outPrintStream.close();
        outPrintStream = null;
        try {
            fileOutStream.close();
            fileOutStream = null;
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(outputFile));
            br.lines().forEach(System.out::println);
            br.close();
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        System.out.println("<=== AFTER EACH TEST");
    }

    @AfterAll
    public void tearDownAll() {
        System.out.println("<= AFTER ALL EXPERIMENTS");
        outputFile.delete();
    }
}
