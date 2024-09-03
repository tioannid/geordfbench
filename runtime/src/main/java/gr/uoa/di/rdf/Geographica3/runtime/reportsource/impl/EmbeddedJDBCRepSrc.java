package gr.uoa.di.rdf.Geographica3.runtime.reportsource.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import gr.uoa.di.rdf.Geographica3.runtime.experiments.Experiment;
import gr.uoa.di.rdf.Geographica3.runtime.reportsource.IReportSource;
import gr.uoa.di.rdf.Geographica3.runtime.resultscollector.impl.QueryRepResults;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.apache.log4j.Logger;

/**
 * A base abstract class for all embedded JDBC report sources implementing
 * interface {@link IReportSource}.
 * @author Theofilos Ioannidis <tioannid@di.uoa.gr>
 * @creationdate 01/09/2024
 * @updatedate 03/09/2024
 */
public abstract class EmbeddedJDBCRepSrc implements IReportSource {

    // --- Static members -----------------------------
    static Logger logger = Logger.getLogger(EmbeddedJDBCRepSrc.class.getSimpleName());
    /**
     * The SQL script commands that create the report source database schema.
     */
    static Map<Integer, String> schemaInitScript = new TreeMap<>();

    // --- Data members ------------------------------
    String driver;
    String relativeBaseDir;
    String database;
    String user;
    String password;
    @JsonIgnore
    Connection conn;
    /* deferred query execution result insertions are necessary because
     * a SUT might be using the same DBMS as the ReportSource, in which case
     * the repetetive restarts of the DBMS during experiment query iterations
     * will disable the direct insertions to the ReportSource.
     */
    @JsonIgnore
    transient boolean deferInsQryExec = false;
    @JsonIgnore
    transient Map<Integer, String> deferredInsQryExecs;
    @JsonIgnore
    transient int noDeferredInsQryExecs;
    @JsonIgnore
    transient boolean schemaInitialized = false; // gets updated by getConn()

    // --- Constructor -----------------------------------
    public EmbeddedJDBCRepSrc() {
    }

    public EmbeddedJDBCRepSrc(String driver, String relativeBaseDir,
            String database, String user, String password) {
        // initialize ground data members that are automatically ser/deser
        this.driver = driver;
        this.relativeBaseDir = relativeBaseDir;
        this.database = database;
        this.user = user;
        this.password = password;
        // basically initialize transient data members
        initializeAfterDeserialization();
    }

    // --- Data Accessors -----------------------------------
    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        JDBCRepSrc.logger = logger;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getRelativeBaseDir() {
        return relativeBaseDir;
    }

    public void setRelativeBaseDir(String relativeBaseDir) {
        this.relativeBaseDir = relativeBaseDir;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // --- Methods -----------------------------------
        @JsonIgnore
    /**
     * This extended URL disables automatic database creation for embedded mode.
     */
    public String getJdbcURL_NoCreateDB() {
        return getJdbcURL() + ";IFEXISTS=TRUE";
    }
    
    @JsonIgnore
    public String getJdbcURL() {
        StringBuilder sb = new StringBuilder("jdbc:");
        // use file protocol be default (could be mem|tcp)
        // http://h2database.com/html/features.html?highlight=url&search=URL#database_url
        sb.append(this.driver).append(":file:");
        sb.append(this.relativeBaseDir).append("/");
        sb.append(this.database);
        return sb.toString();
    }

    public Connection getConn() {
        boolean makeNewConnection = false;
        // is there a valid connection?
        if (conn == null) {
            makeNewConnection = true;
        } else {
            try {
                if (!conn.isValid(0)) {
                    makeNewConnection = true;
                }
            } catch (SQLException ex) { // connection invalid
                logger.error(ex.getMessage());
            }
        }
        // make a connection if you have to
        if (makeNewConnection) {
            try {
                // get a connection to an existing database
                conn = DriverManager.getConnection(getJdbcURL_NoCreateDB(), user, password);
            } catch (SQLException ex) {
                logger.error("Could not connect to the source:\n"
                        + ex.getMessage());
            }
        }
        return conn;
    }

    @Override
    public long insExperiment(Experiment exp) {
        Calendar calendar = Calendar.getInstance();
        java.util.Date now = calendar.getTime();
        Timestamp currentTimestamp = new Timestamp(now.getTime());
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement("INSERT INTO public.\"EXPERIMENT\"(\n"
                    + " exectime, type, description, host, os, sut, \n"
                    + " queryset, dataset, executionspec, reportspec)\n"
                    + " VALUES (?, ?, ?, ?, ?, ?, \n"
                    + " ?, ?, ?, ?);");
            int col = 0;
            st.setTimestamp(++col, currentTimestamp);
            st.setString(++col, exp.getType());
            st.setString(++col, exp.getDescription());
            st.setString(++col, exp.getSut().getHost().toString());
            st.setString(++col, exp.getSut().getHost().getOs().toString());
            st.setString(++col, exp.getSut().getClass().getSimpleName());
            st.setString(++col, exp.getQrySet().getName());
            st.setString(++col, exp.getGeoDS().getName());
            st.setString(++col, exp.getExecSpec().toString());
            st.setString(++col, exp.getRptSpec().getClass().getSimpleName());

            int rowsInserted = st.executeUpdate();
            logger.debug(rowsInserted + " rows inserted in EXPERIMENT table");
            st.close();
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        }

        Statement st1 = null;
        ResultSet rs = null;
        long ret = 0;
        try {
            st1 = conn.createStatement();
            rs = st1.executeQuery("select lastval();");
            rs.next();
            ret = rs.getLong(1);
            rs.close();
            st1.close();
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        }

        return ret;
    }

    @Override
    public void insQueryExecution(long expID, String cache_type,
            String qryLabel, int qryNo, int qryRepNo,
            QueryRepResults.QueryRepResult queryRepResult) {

        if (!this.deferInsQryExec) { // real time insertion to database
            PreparedStatement st = null;
            try {
                st = conn.prepareStatement("INSERT INTO public.\"QUERYEXECUTION\"(\n"
                        + " experiment_id, query_no, query_label, cache_type, iteration, eval_time, \n"
                        + " scan_time, no_results, no_scan_errors, eval_flag, res_exception)\n"
                        + "    VALUES (?, ?, ?, ?, ?, ?, \n"
                        + "            ?, ?, ?, ?, ?);");
                int col = 0;
                st.setLong(++col, expID);
                st.setInt(++col, qryNo);
                st.setString(++col, qryLabel);
                st.setString(++col, cache_type);
                st.setInt(++col, qryRepNo);
                st.setLong(++col, queryRepResult.getEvalTime());
                st.setLong(++col, queryRepResult.getScanTime());
                st.setLong(++col, queryRepResult.getNoResults());
                st.setLong(++col, queryRepResult.getNoScanErrors());
                st.setString(++col, queryRepResult.getEvalFlag().getName());
                st.setString(++col, queryRepResult.getResException().getName());

                int rowsInserted = st.executeUpdate();
                logger.debug(rowsInserted + " rows inserted in QUERYEXECUTION table");
                st.close();
            } catch (SQLException ex) {
                logger.error(ex.getMessage());
            }
        } else { // deferred insertion
            String insSql = "INSERT INTO public.\"QUERYEXECUTION\"(\n"
                    + " experiment_id, query_no, query_label, cache_type, iteration, eval_time, \n"
                    + " scan_time, no_results, no_scan_errors, eval_flag, res_exception)\n"
                    + "    VALUES (" + expID + ", " + qryNo + ", '" + qryLabel + "', \n"
                    + "    '" + cache_type + "', " + qryRepNo + ", " + queryRepResult.getEvalTime() + ", \n"
                    + queryRepResult.getScanTime() + ", " + queryRepResult.getNoResults() + ", \n"
                    + queryRepResult.getNoScanErrors() + ", '" + queryRepResult.getEvalFlag().getName() + "', \n"
                    + "    '" + queryRepResult.getResException().getName() + "');";
            this.deferredInsQryExecs.put(++noDeferredInsQryExecs, insSql);
        }
    }

    /**
     * Serialize the GeoRDFBench report source to a JSON string
     *
     * @return the serialized JSON string
     */
    @Override
    public String serializeToJSON() {
        // create the mapper
        ObjectMapper mapper = new ObjectMapper();

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // enable pretty printing
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // serialize the object
        StringWriter strwrt = new StringWriter();
        try {
            mapper.writeValue(strwrt, this);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
        return strwrt.toString();
    }

    /**
     * Serialize the GeoRDFBench report source to a JSON file
     *
     * @param serObjFile File with the serialized object
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @Override
    public void serializeToJSON(File serObjFile) throws JsonGenerationException, JsonMappingException, IOException {
        // create the mapper
        ObjectMapper mapper = new ObjectMapper();
        // do not serialize null fields
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // enable pretty printing
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // serialize the object
        mapper.writeValue(serObjFile, this);
    }

    @Override
    public void initializeAfterDeserialization() {
        schemaInitialized = isSchemaInitialized();
        if (!schemaInitialized) {
            schemaInitialized = initializeSchema();
        }
        if (schemaInitialized) {
            conn = this.getConn();
            deferInsQryExec = true;
            deferredInsQryExecs = new HashMap<>();
            noDeferredInsQryExecs = 0;
        }
    }

    @Override
    public void flush() {
        logger.info("Deferred mode for " + this.getClass().getSimpleName() + " is disabled because it is an embedded DBMS. No records to flush!");
    }

    @Override
    public abstract boolean isSchemaInitialized();

    @Override
    public abstract boolean initializeSchema();
}
