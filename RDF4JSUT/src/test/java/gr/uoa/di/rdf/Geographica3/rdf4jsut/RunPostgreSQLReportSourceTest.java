package gr.uoa.di.rdf.Geographica3.rdf4jsut;

import com.fasterxml.jackson.databind.JsonMappingException;
import gr.uoa.di.rdf.Geographica3.runtime.reportsource.IReportSource;
import gr.uoa.di.rdf.Geographica3.runtime.reportsource.impl.PostgreSQLRepSrc;
import gr.uoa.di.rdf.Geographica3.runtime.reportsource.util.ReportSourceUtil;
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

/**
 * A class that checks whether PostgreSQL report source work properly in several
 * use cases.
 *
 * @author Theofilos Ioannidis <tioannid@di.uoa.gr>
 * @creationdate 03/09/2024
 * @updatedate 04/09/2024
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Test PostgreSQL Report Source Functionality")
public class RunPostgreSQLReportSourceTest {

    // Data Members
    String argLineBase,
            argLinePostgres,
            argLinePostgresRun,
            argLinePostgresPrint;
    String TEST_RESOURCES_DIR,
            JSON_DEFS_DIR,
            NEW_POSTGRESQL_SPEC_FILE,
            POSTGRESQL_SPEC_FILE;

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
        System.out.println(RunPostgreSQLReportSourceTest.class.getSimpleName() + " - Before All");
        // find the absolute path of the test resources folder
        File p = new File("src/test/resources");
        TEST_RESOURCES_DIR = p.getAbsolutePath();
        NEW_POSTGRESQL_SPEC_FILE = TEST_RESOURCES_DIR + "/"
                + "ubuntu_vma_tioaRepSrcoriginal.json";
        // find the absolute path of the JSON Library in the test resources folder
        p = new File("src/test/resources/json_defs");
        JSON_DEFS_DIR = p.getAbsolutePath();
        POSTGRESQL_SPEC_FILE
                = JSON_DEFS_DIR + "/reportsources/ubuntu_vma_tioaRepSrcoriginal.json";
        // create a base argument list for running experiments with RDF4J and
        // the detailed benchmark specifications
        argLineBase
                = // Report Source: PostgreSQL in ubuntu-vma-tioa
                "-rbd RDF4J_3.7.7_Repos/server "
                + "-expdesc RDF4JSUT_RunPostgreSQLReportSourceTest "
                + "-ds " + JSON_DEFS_DIR + "/datasets/scalability_10Koriginal.json "
                + "-qs " + JSON_DEFS_DIR + "/querysets/scalabilityFuncQSoriginal.json "
                + "-h " + JSON_DEFS_DIR + "/hosts/ubuntu_vma_tioaHOSToriginal.json "
                + "-rs " + JSON_DEFS_DIR + "/reportspecs/simplereportspec_original.json ";
        // create the base argument list which use the local PostgreSQL DBMS
        argLinePostgres = argLineBase
                + "-rpsr " + JSON_DEFS_DIR + "/reportsources/ubuntu_vma_tioaRepSrcoriginal.json ";
        // create 2 variants of the PostgreSQL base argument list which either RUN the
        // experiement or PRINT the ground queryset
        argLinePostgresRun = argLinePostgres
                + "-es " + JSON_DEFS_DIR + "/executionspecs/scalabilityESoriginal.json ";
        argLinePostgresPrint = argLinePostgres
                + "-es " + JSON_DEFS_DIR + "/executionspecs/scalabilityESoriginal_PRINT.json ";
    }

    @BeforeEach
    public void setup() {
        System.out.println("Should print before each test");
    }

    @Test
    @Order(1)
    @DisplayName("Should Create A Serialized JSON For PostgreSQL In VM")
    public void shouldCreateASerializedJSONForPostgreSQLInVM() throws IOException {
        // create a new file "src/test/resources/ubuntu_vma_tioaRepSrcoriginal.json"
        File newFile = new File(NEW_POSTGRESQL_SPEC_FILE);
        // delete file if it exists (it probably is for a previous test run)
        if (newFile.exists()) {
            newFile.delete();
        }
        try {
            PostgreSQLRepSrc pgsrc
                    = new PostgreSQLRepSrc(
                            "10.0.2.15", "localhost", 5432,
                            "geordfbench",
                            "geordfbench", "geordfbench");
            pgsrc.serializeToJSON(newFile);
        } catch (SQLException | IOException ex) {
            System.err.println(ex.getMessage());
        }
        // assert that "src/test/resources/ubuntu_vma_tioaRepSrcoriginal.json" is identical 
        // to "src/test/resources/json_defs/reportsources/ubuntu_vma_tioaRepSrcoriginal.json"
        Assertions.assertEquals(
                filesCompareByLine(
                        Paths.get(NEW_POSTGRESQL_SPEC_FILE),
                        Paths.get(POSTGRESQL_SPEC_FILE)),
                -1L);
        // delete newly created file
        if (newFile.exists()) {
            newFile.delete();
        }
    }

    @Test
    @Order(2)
    @DisplayName("Should Deserialize From PostgreSQL In VM JSON Specification")
    public void shouldDeserializeFromPostgreSQLInVMJSONSpecification() throws JsonMappingException, IOException {
        // deserialize a IReportSource object from file
        // "src/test/resources/json_defs/reportsources/ubuntu_vma_tioaRepSrcoriginal.json"
        IReportSource rptSrc
                = ReportSourceUtil.deserializeFromJSON(POSTGRESQL_SPEC_FILE);
        // create a new file "src/test/resources/ubuntu_vma_tioaRepSrcoriginal.json"
        File newFile = null;
        newFile = new File(NEW_POSTGRESQL_SPEC_FILE);
        // delete file if it exists (it probably is for a previous test run)
        if (newFile.exists()) {
            newFile.delete();
        }
        // serialize the IReportSource object to new file
        rptSrc.serializeToJSON(newFile);
        // assert that the file comparison returns -1L for full line equality
        Assertions.assertEquals(
                filesCompareByLine(
                        Paths.get(NEW_POSTGRESQL_SPEC_FILE),
                        Paths.get(POSTGRESQL_SPEC_FILE)),
                -1L);
        // delete newly created file
        if (newFile.exists()) {
            newFile.delete();
        }
    }

    @Test
    @Order(3)
    @DisplayName("Should Create One Experiment Record And Eighteen Queryexecution Records In PostgreSQL VM")
    public void shouldCreateOneExperimentRecordAndEighteenQueryexecutionRecordsInPostgreSQLVM() throws Exception {
        // deserialize a PostgreSQLRepSrc object from file
        // "src/test/resources/json_defs/reportsources/ubuntu_vma_tioaRepSrcoriginal.json"
        PostgreSQLRepSrc rptSrc
                = (PostgreSQLRepSrc) ReportSourceUtil.deserializeFromJSON(POSTGRESQL_SPEC_FILE);
        // Query PostgreSQL for number of rows in database tables:
        // public."EXPERIMENT" and public."QUERYEXECUTION"
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
            System.out.println("Opening connection to PostgreSQL report source");
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
            System.out.println("Closing connection to PostgreSQL report source");
            tmpConn.close();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        // Run the Scalability experiment which should create 1 record in EXPERIMENT table
        // and 18 records in QUERYEXECUTION
        RunRDF4JExperiment.main(argLinePostgresRun.split(" "));
        // Query PostgreSQL for number of rows in database tables:
        // public."EXPERIMENT" and public."QUERYEXECUTION"
        System.out.println("Trying to retrieve number of rows in EXPERIMENT and QUERYEXECUTION tables after Scalability experiment execution");
        try {
            System.out.println("Opening connection to PostgreSQL report source");
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
            System.out.println("Closing connection to PostgreSQL report source");
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
