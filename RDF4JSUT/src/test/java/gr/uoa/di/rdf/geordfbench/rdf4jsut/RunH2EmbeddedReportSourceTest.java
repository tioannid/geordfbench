package gr.uoa.di.rdf.geordfbench.rdf4jsut;

import com.fasterxml.jackson.databind.JsonMappingException;
import gr.uoa.di.rdf.geordfbench.runtime.reportsource.IReportSource;
import gr.uoa.di.rdf.geordfbench.runtime.reportsource.impl.H2EmbeddedRepSrc;
import gr.uoa.di.rdf.geordfbench.runtime.reportsource.util.ReportSourceUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

/**
 * A class that checks whether H2 embedded report source work properly in
 * several use cases.
 *
 * @author Theofilos Ioannidis <tioannid@di.uoa.gr>
 * @creationdate 03/09/2024
 * @updatedate 27/09/2024
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Test H2 Embedded Report Source Functionality")
public class RunH2EmbeddedReportSourceTest {

    // Static Members
    // Data Members
    String argLineBase,
            argLineH2Embedded,
            argLineH2EmbeddedRun,
            argLineH2EmbeddedPrint;
    String TEST_RESOURCES_DIR,
            JSON_DEFS_DIR,
            NEW_H2_EMBEDDED_SPEC_FILE,
            H2_EMBEDDED_SPEC_FILE;

    // Static Methods
    public static long filesCompareByLine(Path path1, Path path2) throws IOException {
        try (BufferedReader bf1 = Files.newBufferedReader(path1);
                BufferedReader bf2 = Files.newBufferedReader(path2)) {

            long lineNumber = 1;
            String line1 = "", line2 = "";
            while ((line1 = bf1.readLine()) != null) {
                line2 = bf2.readLine();
                if (line2 == null || !line1.equals(line2)) {
                    return lineNumber;
                }
                lineNumber++;
            }
            if (bf2.readLine() == null) {
                return -1;
            } else {
                return lineNumber;
            }
        }
    }

    @BeforeAll
    public void setupAll() {
        System.out.println(RunH2EmbeddedReportSourceTest.class.getSimpleName() + " - Before All");
        // find the absolute path of the test resources folder
        File p = new File("src/test/resources".replace("/", SEP));
        TEST_RESOURCES_DIR = p.getAbsolutePath();
        NEW_H2_EMBEDDED_SPEC_FILE = TEST_RESOURCES_DIR + SEP
                + "h2EmbeddedRepSrcoriginal.json";
        System.out.println("NEW_H2_EMBEDDED_SPEC_FILE = " + NEW_H2_EMBEDDED_SPEC_FILE);
        // find the absolute path of the JSON Library in the test resources folder
        p = new File("src/test/resources/json_defs".replace("/", SEP));
        JSON_DEFS_DIR = p.getAbsolutePath();
        H2_EMBEDDED_SPEC_FILE
                = JSON_DEFS_DIR + "/reportsources/h2EmbeddedRepSrcoriginal.json".replace("/", SEP);
        System.out.println("H2_EMBEDDED_SPEC_FILE = " + H2_EMBEDDED_SPEC_FILE);
        // create a base argument list for running experiments with RDF4J and
        // the detailed benchmark specifications
        argLineBase
                = // Report Source: PostgreSQL in ubuntu-vma-tioa
                "-rbd " + "RDF4J_4.3.15_Repos/server".replace("/", SEP) + " "
                + "-expdesc RDF4JSUT_RunH2EmbeddedReportSourceTest "
                + "-ds " + JSON_DEFS_DIR + "/datasets/scalability_10Koriginal.json".replace("/", SEP) + " "
                + "-qs " + JSON_DEFS_DIR + "/querysets/scalabilityFuncQSoriginal.json".replace("/", SEP) + " "
                + "-rs " + JSON_DEFS_DIR + "/reportspecs/simplereportspec_original.json".replace("/", SEP) + " ";
        // based on OS choose an appropriate host spec
        argLineBase += "-h " + JSON_DEFS_DIR
                + ((isWindows())
                        ? "/hosts/win10_workHOSToriginal.json".replace("/", SEP)
                        : "/hosts/ubuntu_vma_tioaHOSToriginal.json".replace("/", SEP)) + " ";
        // create the base argument list which use the embedded H2 DBMS
        argLineH2Embedded = argLineBase
                + "-rpsr " + JSON_DEFS_DIR + "/reportsources/h2EmbeddedRepSrcoriginal.json".replace("/", SEP) + " ";
        // create 2 variants of the embedded H2 base argument list which either RUN the
        // experiement or PRINT the ground queryset
        argLineH2EmbeddedRun = argLineH2Embedded
                + "-es " + JSON_DEFS_DIR + "/executionspecs/scalabilityESoriginal.json".replace("/", SEP) + " ";
        argLineH2EmbeddedPrint = argLineH2Embedded
                + "-es " + JSON_DEFS_DIR + "/executionspecs/scalabilityESoriginal_PRINT.json".replace("/", SEP) + " ";
    }

    @BeforeEach
    public void setup() {
        System.out.println("Should print before each test");
    }

    @Test
    @Order(1)
    @DisplayName("Should Create A Serialized JSON For H2 Embedded")
    public void shouldCreateASerializedJSONForH2Embedded() throws IOException {
        // create a new file "src/test/resources/h2EmbeddedRepSrcoriginal.json"
        File newFile = null;
        newFile = new File(NEW_H2_EMBEDDED_SPEC_FILE);
        // delete file if it exists (it probably is for a previous test run)
        if (newFile.exists()) {
            newFile.delete();
        }
        try {
            H2EmbeddedRepSrc h2src
                    = new H2EmbeddedRepSrc(
                            "../scripts/h2embeddedreportsource",
                            "geordfbench",
                            "sa", "");

            h2src.serializeToJSON(newFile);
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        // assert that "src/test/resources/h2EmbeddedRepSrcoriginal.json" is identical 
        // to "src/test/resources/json_defs/reportsources/h2EmbeddedRepSrcoriginal.json"

        // assert that the file comparison returns -1L for full line equality
        Assertions.assertEquals(
                filesCompareByLine(
                        Paths.get(NEW_H2_EMBEDDED_SPEC_FILE),
                        Paths.get(H2_EMBEDDED_SPEC_FILE)),
                -1L);
        // delete newly created file
        if (newFile.exists()) {
            newFile.delete();
        }
    }

    @Test
    @Order(2)
    @DisplayName("Should Deserialize From H2 Embedded JSON Specification")
    public void shouldDeserializeFromH2EmbeddedJSONSpecification() throws JsonMappingException, IOException {
        // deserialize a IReportSource object from file
        // "src/test/resources/json_defs/reportsources/h2EmbeddedRepSrcoriginal.json"
        IReportSource rptSrc
                = ReportSourceUtil.deserializeFromJSON(H2_EMBEDDED_SPEC_FILE);
        // create a new file "src/test/resources/h2EmbeddedRepSrcoriginal.json"
        File newFile = null;
        newFile = new File(NEW_H2_EMBEDDED_SPEC_FILE);
        // delete file if it exists (it probably is for a previous test run)
        if (newFile.exists()) {
            newFile.delete();
        }
        // serialize the IReportSource object to new file
        rptSrc.serializeToJSON(newFile);
        // assert that the file comparison returns -1L for full line equality
        Assertions.assertEquals(
                filesCompareByLine(
                        Paths.get(NEW_H2_EMBEDDED_SPEC_FILE),
                        Paths.get(H2_EMBEDDED_SPEC_FILE)),
                -1L);
        // delete newly created file
        if (newFile.exists()) {
            newFile.delete();
        }
    }

    @Test
    @Order(3)
    @DisplayName("Should Create One Experiment Record And Eighteen Queryexecution Records In H2 Embedded")
    public void shouldCreateOneExperimentRecordAndEighteenQueryexecutionRecordsInH2Embedded() throws Exception {
        // deserialize an H2EmbeddedRepSrc object from file
        // "src/test/resources/json_defs/reportsources/h2EmbeddedRepSrcoriginal.json"
        H2EmbeddedRepSrc rptSrc
                = (H2EmbeddedRepSrc) ReportSourceUtil.deserializeFromJSON(H2_EMBEDDED_SPEC_FILE);
        // Query H2 for number of rows in database tables:
        // "PUBLIC"."EXPERIMENT" and "PUBLIC"."QUERYEXECUTION"
        String sqlGetExperimentRows = "SELECT Count(*) AS countrows\n"
                + "FROM public.\"EXPERIMENT\";";
        String sqlGetQueryExecutionRows = "SELECT Count(*) AS countrows\n"
                + "FROM public.\"QUERYEXECUTION\";";
        Statement stmt;
        ResultSet rs;
        Connection tmpConn = null;
        int rowsExperimentBefore = -1, rowsExperimentAfter = -1;
        int rowsQueryExecutionBefore = -1, rowsQueryExecutionAfter = -1;
        System.out.println("Trying to retrieve number of rows in EXPERIMENT and QUERYEXECUTION tables before Scalability experiment execution");
        try {
            System.out.println("Opening connection to H2 Embedded report source");
            tmpConn = rptSrc.getConn();
            // retrieve number of rows in EXPERIMENT
            stmt = tmpConn.createStatement();
            rs = stmt.executeQuery(sqlGetExperimentRows);
            rs.next();
            rowsExperimentBefore = rs.getInt("countrows");
            System.out.println("Number of rows in EXPERIMENT table before Scalability experiment execution = " + rowsExperimentBefore);
            // retrieve number of rows in QUERYEXECUTION
            stmt = tmpConn.createStatement();
            rs = stmt.executeQuery(sqlGetQueryExecutionRows);
            rs.next();
            rowsQueryExecutionBefore = rs.getInt("countrows");
            System.out.println("Number of rows in QUERYEXECUTION table before Scalability experiment execution = " + rowsQueryExecutionBefore);
            System.out.println("Closing connection to H2 Embedded report source");
            tmpConn.close();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        // Run the Scalability experiment which should create 1 record in EXPERIMENT table
        // and 18 records in QUERYEXECUTION
        RunRDF4JExperiment.main(argLineH2EmbeddedRun.split(" "));
        // Query H2 Embedded for number of rows in database tables:
        // "PUBLIC"."EXPERIMENT" and "PUBLIC"."QUERYEXECUTION"
        System.out.println("Trying to retrieve number of rows in EXPERIMENT and QUERYEXECUTION tables after Scalability experiment execution");
        try {
            System.out.println("Opening connection to H2 Embedded report source");
            tmpConn = rptSrc.getConn();
            // retrieve number of rows in EXPERIMENT
            stmt = tmpConn.createStatement();
            rs = stmt.executeQuery(sqlGetExperimentRows);
            rs.next();
            rowsExperimentAfter = rs.getInt("countrows");
            System.out.println("Number of rows in EXPERIMENT table after Scalability experiment execution = " + rowsExperimentAfter);
            // retrieve number of rows in QUERYEXECUTION
            stmt = tmpConn.createStatement();
            rs = stmt.executeQuery(sqlGetQueryExecutionRows);
            rs.next();
            rowsQueryExecutionAfter = rs.getInt("countrows");
            System.out.println("Number of rows in QUERYEXECUTION table after Scalability experiment execution = " + rowsQueryExecutionAfter);
            System.out.println("Closing connection to H2 Embedded report source");
            tmpConn.close();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        // Assert that:
        //  a) rowsExperimentAfter = rowsExperimentBefore + 1
        //  b) rowsQueryExecutionAfter = rowsQueryExecutionBefore + 18 (3*[3+3])
        // 18 = 3 queries each executed 
        //          3 times with COLD caches and 
        //          3 times with WARM caches
        Assertions.assertEquals(rowsExperimentAfter, rowsExperimentBefore + 1);
        Assertions.assertEquals(rowsQueryExecutionAfter, rowsQueryExecutionBefore + 3 * (3 + 3));
    }

    @AfterEach
    public void tearDown() {
        System.out.println("Should print after each test");
    }

    @AfterAll
    public void tearDownAll() {
        System.out.println("Should print after all tests");
    }
}
