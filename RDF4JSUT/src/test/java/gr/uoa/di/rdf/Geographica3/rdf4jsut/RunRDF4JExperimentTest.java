package gr.uoa.di.rdf.Geographica3.rdf4jsut;

import java.io.ByteArrayOutputStream;
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
    private ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeAll
    public void setupAll() {
        System.out.println(RunRDF4JExperimentTest.class.getSimpleName() + " - Before All");
        // find the absolute path of the JSON Library in the test resources folder
        File p = new File("src/test/resources/json_defs");
        JSON_DEFS_DIR = p.getAbsolutePath();
        String argLineNoQueryFilter
                = // No Query Filter is specified - 3 queries (0,1,2) expected in output
                "-rbd RDF4J_3.7.7_Repos/server "
                + "-expdesc RDF4JSUT_RunRDF4JExperimentTest "
                + "-ds " + JSON_DEFS_DIR + "/datasets/scalability_10Koriginal.json "
                + "-qs " + JSON_DEFS_DIR + "/querysets/scalabilityFuncQSoriginal.json "
                + "-es " + JSON_DEFS_DIR + "/executionspecs/scalabilityESoriginal_PRINT.json "
                + "-h " + JSON_DEFS_DIR + "/hosts/ubuntu_vma_tioaHOSToriginal.json "
                + "-rs " + JSON_DEFS_DIR + "/reportspecs/simplereportspec_original.json "
                + "-rpsr " + JSON_DEFS_DIR + "/reportsources/ubuntu_vma_tioaRepSrcoriginal.json ";
        argsNoQueryFilter = argLineNoQueryFilter.split(" ");
        String argLineWithQueryInclusionFilter = argLineNoQueryFilter // Query InclusionFilter specified - 2 queries (0,_,2) expected in output
                + "-qif \"0,2\"";
        argsWithQueryInclusionFilter = argLineWithQueryInclusionFilter.split(" ");
        String argLineWithQueryExclusionFilter = argLineNoQueryFilter // Query ExclusionFilter specified - 1 query (_,1,_) expected in output
                + "-qef \"0,2\"";
        argsWithQueryExclusionFilter = argLineWithQueryExclusionFilter.split(" ");
    }

    @BeforeEach
    public void setup() {
        System.out.println("Should print before each test");
        outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    @Order(1)
    @DisplayName("Should Display the 3 Scalability Ground Queries 0,1,2")
    public void shouldDisplay3ScalabilityGroundQueries() throws Exception {
        RunRDF4JExperiment.main(this.argsNoQueryFilter);
        Assertions.assertTrue(outputStreamCaptor.toString().contains("Query 0 -"));
        Assertions.assertTrue(outputStreamCaptor.toString().contains("Query 1 -"));
        Assertions.assertTrue(outputStreamCaptor.toString().contains("Query 2 -"));
    }

    @Test
    @Order(2)
    @DisplayName("Should Display the 2 Included Scalability Ground Queries 0,2")
    public void shouldDisplayThe2IncludedScalabilityGroundQueries() throws Exception {
        RunRDF4JExperiment.main(this.argsWithQueryInclusionFilter);
        Assertions.assertTrue(outputStreamCaptor.toString().contains("Query 0 -"));
        Assertions.assertFalse(outputStreamCaptor.toString().contains("Query 1 -"));
        Assertions.assertTrue(outputStreamCaptor.toString().contains("Query 2 -"));
    }

    @Test
    @Order(3)
    @DisplayName("Should Not Display the 2 Excluded Scalability Ground Queries 0,2")
    public void shouldNotDisplayThe2ExcludedScalabilityGroundQueries() throws Exception {
        RunRDF4JExperiment.main(this.argsWithQueryExclusionFilter);
        Assertions.assertFalse(outputStreamCaptor.toString().contains("Query 0 -"));
        Assertions.assertTrue(outputStreamCaptor.toString().contains("Query 1 -"));
        Assertions.assertFalse(outputStreamCaptor.toString().contains("Query 2 -"));
    }

    @AfterEach
    public void tearDown() {
        System.out.println("Should print after each test");
        System.setOut(standardOut);
    }

    @AfterAll
    public void tearDownAll() {
        System.out.println("Should print after all tests");
    }
}
