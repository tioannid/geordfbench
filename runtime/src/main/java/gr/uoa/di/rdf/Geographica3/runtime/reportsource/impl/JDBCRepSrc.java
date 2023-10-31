package gr.uoa.di.rdf.Geographica3.runtime.reportsource.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import gr.uoa.di.rdf.Geographica3.runtime.experiments.Experiment;
import gr.uoa.di.rdf.Geographica3.runtime.reportsource.IReportSource;
import gr.uoa.di.rdf.Geographica3.runtime.resultscollector.impl.QueryRepResults.QueryRepResult;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import org.apache.log4j.Logger;
import java.sql.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public class JDBCRepSrc implements IReportSource {

    // --- Static members -----------------------------
    static Logger logger = Logger.getLogger(JDBCRepSrc.class.getSimpleName());

    // --- Data members ------------------------------
    String driver;
    String hostname;
    String althostname;
    int port;
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
    transient boolean deferInsQryExec;
    @JsonIgnore
    transient Map<Integer, String> deferredInsQryExecs;
    @JsonIgnore
    transient int noDeferredInsQryExecs;

    // --- Constructor -----------------------------------
    public JDBCRepSrc() {
    }

    public JDBCRepSrc(String driver, String hostname, String althostname, int port,
            String database, String user, String password) throws SQLException {
        // ground data members
        this.driver = driver;
        this.hostname = hostname;
        this.althostname = althostname;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
        // transient data members
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

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getAlthostname() {
        return althostname;
    }

    public void setAlthostname(String althostname) {
        this.althostname = althostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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
    public String getJdbcURL() {
        StringBuilder sb = new StringBuilder("jdbc:");
        sb.append(this.driver).append("://");
        sb.append(this.hostname).append(":");
        sb.append(this.port).append("/");
        sb.append(this.database).append("?");
        sb.append("user=").append(this.user).append("&");
        sb.append("password=").append(this.password);
        return sb.toString();
    }

    @JsonIgnore
    public String getAltJdbcURL() {
        StringBuilder sb = new StringBuilder("jdbc:");
        sb.append(this.driver).append("://");
        sb.append(this.althostname).append(":");
        sb.append(this.port).append("/");
        sb.append(this.database).append("?");
        sb.append("user=").append(this.user).append("&");
        sb.append("password=").append(this.password);
        return sb.toString();
    }

    public Connection getConn() {
        boolean makeNewConnection = false;
        if (conn == null) {
            makeNewConnection = true;
        } else {
            try {
                if (!conn.isValid(0)) {
                    makeNewConnection = true;
                }
            } catch (SQLException ex) { // connection invalid
            }
        }

        if (makeNewConnection) {
            try { // try to connect to the main hostname
                conn = DriverManager.getConnection(getJdbcURL());
            } catch (SQLException ex) {
                try {
                    conn = DriverManager.getConnection(getAltJdbcURL());
                } catch (SQLException ex1) {
                    logger.error("Could not connect to neither hostnames of the source\n"
                            + ex.getMessage() + "\n"
                            + ex1.getMessage());
                }
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
            QueryRepResult queryRepResult) {

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
     * Serialize the geographica report source to a JSON string
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
     * Serialize the geographica report source to a JSON file
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
        conn = this.getConn();
        deferInsQryExec = true;
        deferredInsQryExecs = new HashMap<>();
        noDeferredInsQryExecs = 0;
    }

    @Override
    public void flush() {
        if (deferInsQryExec) {
            // refresh connection, because it was probably broken by
            // SUT experiment restarts
            conn = this.getConn();
            String insSql = "";
            int noInserted = 0;
            for (Integer insSqlNo : deferredInsQryExecs.keySet()) {
                insSql = deferredInsQryExecs.get(insSqlNo);
                if (insQueryExecution(insSql)) {
                    ++noInserted;
                }
            }
            logger.info("Deferred mode for " + this.getClass().getSimpleName() + " was enabled. " + noInserted + " records were flushed");
        } else {
            logger.info("Deferred mode for " + this.getClass().getSimpleName() + " was disabled. No records to flush!");
        }

    }

    private boolean insQueryExecution(String insSql) {
        PreparedStatement st = null;
        boolean inserted = false;
        try {
            st = conn.prepareStatement(insSql);
            int rowsInserted = st.executeUpdate();
            inserted = (rowsInserted == 1);
            st.close();
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        }
        return inserted;
    }
}
