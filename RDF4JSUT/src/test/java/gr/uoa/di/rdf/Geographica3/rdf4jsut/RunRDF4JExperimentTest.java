/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uoa.di.rdf.Geographica3.rdf4jsut;

import java.io.ByteArrayOutputStream;
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
 *
 * @author tioannid
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Test Experiment Query Filtering with Detailed Benchmark Specification")
public class RunRDF4JExperimentTest {

    // Data Members
    String[] argsNoQueryFilter,
            argsWithQueryInclusionFilter,
            argsWithQueryExclusionFilter;

    private final PrintStream standardOut = System.out;
    private ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeAll
    public void setupAll() {
        System.out.println(RunRDF4JExperimentTest.class.getSimpleName() + " - Before All");
        String argLineNoQueryFilter
                = // No Query Filter is specified - 3 queries (0,1,2) expected in output
                "-rbd RDF4J_3.7.7_Repos/server "
                + "-expdesc 21Aug2024_RDF4JSUT_Run_Scal10K "
                + "-ds /media/sf_VM_Shared/PHD/NetBeansProjects/PhD_2/geordfbench/json_defs/datasets/scalability_10Koriginal.json "
                + "-qs /media/sf_VM_Shared/PHD/NetBeansProjects/PhD_2/geordfbench/json_defs/querysets/scalabilityFuncQSoriginal.json "
                + "-es /media/sf_VM_Shared/scalabilityESoriginal_PRINT.json "
                + "-h /media/sf_VM_Shared/PHD/NetBeansProjects/PhD_2/geordfbench/json_defs/hosts/ubuntu_vma_tioaHOSToriginal.json "
                + "-rs /media/sf_VM_Shared/PHD/NetBeansProjects/PhD_2/geordfbench/json_defs/reportspecs/simplereportspec_original.json "
                + "-rpsr /media/sf_VM_Shared/PHD/NetBeansProjects/PhD_2/geordfbench/json_defs/reportsources/ubuntu_vma_tioaRepSrcoriginal.json ";
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