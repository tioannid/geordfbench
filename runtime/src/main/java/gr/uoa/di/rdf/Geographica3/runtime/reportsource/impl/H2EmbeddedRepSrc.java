package gr.uoa.di.rdf.Geographica3.runtime.reportsource.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * A concrete implementation class for {@link EmbeddedJDBCRepSrc}.
 *
 * @author Theofilos Ioannidis <tioannid@di.uoa.gr>
 * @creationdate 01/09/2024
 * @updatedate 03/09/2024
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY,
        property = "classname")
public class H2EmbeddedRepSrc extends EmbeddedJDBCRepSrc {

    // --- Static members -----------------------------
    static {
        logger = Logger.getLogger(H2EmbeddedRepSrc.class.getSimpleName());
        // load and register JDBC driver for H2 DBMS
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException ex) {
            logger.error(ex.getMessage());
        }
        // load all DDL commands to the schema initialization script
        schemaInitScript.put(1, "CREATE TABLE public.\"EXPERIMENT\" (\n"
                + "    id int GENERATED ALWAYS AS IDENTITY PRIMARY KEY,\n"
                + "    instime timestamp(3) with time zone DEFAULT CURRENT_TIMESTAMP(3),\n"
                + "    exectime timestamp(3) with time zone,\n"
                + "    description character varying(100),\n"
                + "    host character varying(200),\n"
                + "    os character varying(100),\n"
                + "    sut character varying(200),\n"
                + "    queryset character varying(200),\n"
                + "    dataset character varying(150),\n"
                + "    executionspec character varying(200),\n"
                + "    reportspec character varying(150),\n"
                + "    type character varying(50)\n"
                + ");\n");
        schemaInitScript.put(2, "CREATE TABLE public.\"QUERYEXECUTION\" (\n"
                + "    id int GENERATED ALWAYS AS IDENTITY PRIMARY KEY,\n"
                + "    experiment_id integer NOT NULL,\n"
                + "    query_no integer NOT NULL,\n"
                + "    query_label character varying(100),\n"
                + "    cache_type character varying(15),\n"
                + "    iteration smallint,\n"
                + "    eval_time bigint,\n"
                + "    scan_time bigint,\n"
                + "    no_results bigint,\n"
                + "    no_scan_errors bigint,\n"
                + "    eval_flag character varying(35),\n"
                + "    res_exception character varying(35)\n"
                + ");\n");
        schemaInitScript.put(3, "CREATE VIEW public.vqueryexecution AS\n"
                + " SELECT \"QUERYEXECUTION\".id,\n"
                + "    \"QUERYEXECUTION\".experiment_id,\n"
                + "    \"EXPERIMENT\".type,\n"
                + "    \"QUERYEXECUTION\".query_label,\n"
                + "    \"QUERYEXECUTION\".query_no,\n"
                + "    \"QUERYEXECUTION\".cache_type,\n"
                + "    \"QUERYEXECUTION\".iteration,\n"
                + "    \"QUERYEXECUTION\".eval_time,\n"
                + "    \"QUERYEXECUTION\".scan_time,\n"
                + "    \"QUERYEXECUTION\".no_results,\n"
                + "    \"QUERYEXECUTION\".no_scan_errors,\n"
                + "    \"QUERYEXECUTION\".eval_flag,\n"
                + "    \"QUERYEXECUTION\".res_exception,\n"
                + "    (\"QUERYEXECUTION\".eval_time + \"QUERYEXECUTION\".scan_time) AS total_time,\n"
                + "    round((((\"QUERYEXECUTION\".eval_time + \"QUERYEXECUTION\".scan_time))::numeric / 1000000000.0), 3) AS total_time_s\n"
                + "   FROM public.\"QUERYEXECUTION\",\n"
                + "    public.\"EXPERIMENT\"\n"
                + "  WHERE (\"QUERYEXECUTION\".experiment_id = \"EXPERIMENT\".id);\n"
                + "\n"
                + "CREATE VIEW public.vquery_ordered_aggrs AS\n"
                + " SELECT vqueryexecution.experiment_id,\n"
                + "    vqueryexecution.query_no,\n"
                + "    vqueryexecution.cache_type,\n"
                + "    count(vqueryexecution.iteration) AS no_iterations,\n"
                + "    round(avg(vqueryexecution.total_time_s), 3) AS mean,\n"
                + "    percentile_disc((0.5)::double precision) WITHIN GROUP (ORDER BY vqueryexecution.total_time_s) AS median\n"
                + "   FROM public.vqueryexecution\n"
                + "  GROUP BY vqueryexecution.experiment_id, vqueryexecution.query_no, vqueryexecution.cache_type;\n");
        schemaInitScript.put(4, "CREATE VIEW public.vqueryexecution2 AS\n"
                + " SELECT \"QUERYEXECUTION\".id,\n"
                + "    \"QUERYEXECUTION\".experiment_id,\n"
                + "    \"EXPERIMENT\".type,\n"
                + "    \"QUERYEXECUTION\".query_label,\n"
                + "    \"QUERYEXECUTION\".query_no,\n"
                + "    \"QUERYEXECUTION\".cache_type,\n"
                + "    \"QUERYEXECUTION\".iteration,\n"
                + "    \"QUERYEXECUTION\".eval_time,\n"
                + "    \"QUERYEXECUTION\".scan_time,\n"
                + "    \"QUERYEXECUTION\".no_results,\n"
                + "    \"QUERYEXECUTION\".no_scan_errors,\n"
                + "    \"QUERYEXECUTION\".eval_flag,\n"
                + "    \"QUERYEXECUTION\".res_exception,\n"
                + "    (\"QUERYEXECUTION\".eval_time + \"QUERYEXECUTION\".scan_time) AS total_time,\n"
                + "    round((((\"QUERYEXECUTION\".eval_time + \"QUERYEXECUTION\".scan_time))::numeric / 1000000000.0), 3) AS total_time_s,\n"
                + "        CASE\n"
                + "            WHEN ((\"QUERYEXECUTION\".res_exception)::text <> 'NONE'::text) THEN 'Failed'::text\n"
                + "            ELSE 'Success'::text\n"
                + "        END AS validflag\n"
                + "   FROM public.\"QUERYEXECUTION\",\n"
                + "    public.\"EXPERIMENT\"\n"
                + "  WHERE (\"QUERYEXECUTION\".experiment_id = \"EXPERIMENT\".id);\n"
                + "\n"
                + "CREATE VIEW public.vquery_ordered_aggrs2 AS\n"
                + " SELECT vqueryexecution2.experiment_id,\n"
                + "    vqueryexecution2.query_no,\n"
                + "    vqueryexecution2.cache_type,\n"
                + "    count(vqueryexecution2.iteration) AS no_iterations,\n"
                + "    round(avg(vqueryexecution2.total_time_s), 3) AS mean,\n"
                + "    percentile_disc((0.5)::double precision) WITHIN GROUP (ORDER BY vqueryexecution2.total_time_s) AS median\n"
                + "   FROM public.vqueryexecution2\n"
                + "  GROUP BY vqueryexecution2.validflag, vqueryexecution2.experiment_id, vqueryexecution2.query_no, vqueryexecution2.cache_type\n"
                + " HAVING (vqueryexecution2.validflag = 'Success'::text);\n");
        schemaInitScript.put(5, "CREATE VIEW public.vqueryexecution3 AS\n"
                + " SELECT q.id,\n"
                + "    q.experiment_id,\n"
                + "    e.description,\n"
                + "    e.host,\n"
                + "    e.sut,\n"
                + "    e.queryset,\n"
                + "    e.dataset,\n"
                + "    e.type,\n"
                + "    q.query_label,\n"
                + "    q.query_no,\n"
                + "    q.cache_type,\n"
                + "    q.iteration,\n"
                + "    q.eval_time,\n"
                + "    q.scan_time,\n"
                + "    q.no_results,\n"
                + "    q.no_scan_errors,\n"
                + "    q.eval_flag,\n"
                + "    q.res_exception,\n"
                + "    (q.eval_time + q.scan_time) AS total_time,\n"
                + "    round((((q.eval_time + q.scan_time))::numeric / 1000000000.0), 3) AS total_time_s,\n"
                + "        CASE\n"
                + "            WHEN ((q.res_exception)::text <> 'NONE'::text) THEN 'Failed'::text\n"
                + "            ELSE 'Success'::text\n"
                + "        END AS validflag\n"
                + "   FROM public.\"QUERYEXECUTION\" q,\n"
                + "    public.\"EXPERIMENT\" e\n"
                + "  WHERE (q.experiment_id = e.id);\n"
                + "\n"
                + "CREATE VIEW public.vquery_ordered_aggrs_3 AS\n"
                + " SELECT v.experiment_id,\n"
                + "    v.sut,\n"
                + "    v.queryset,\n"
                + "    v.dataset,\n"
                + "    v.query_label,\n"
                + "    v.query_no,\n"
                + "    v.validflag,\n"
                + "    v.cache_type,\n"
                + "    count(v.iteration) AS no_iterations,\n"
                + "    round(avg(v.total_time_s), 3) AS mean,\n"
                + "    percentile_disc((0.5)::double precision) WITHIN GROUP (ORDER BY v.total_time_s) AS median\n"
                + "   FROM public.vqueryexecution3 v\n"
                + "  GROUP BY v.experiment_id, v.sut, v.queryset, v.dataset, v.query_label, v.query_no, v.validflag, v.cache_type;\n");
        schemaInitScript.put(6, "CREATE VIEW public.vreport AS\n"
                + " SELECT v.cache_type,\n"
                + "    v.query_no,\n"
                + "    v.query_label,\n"
                + "    v.validflag,\n"
                + "    v.sut,\n"
                + "    v.mean,\n"
                + "    v.median\n"
                + "   FROM public.vquery_ordered_aggrs_3 v;\n");
        schemaInitScript.put(7, "ALTER TABLE public.\"QUERYEXECUTION\"\n"
                + "    ADD FOREIGN KEY (experiment_id) \n"
                + "    REFERENCES public.\"EXPERIMENT\"(id) ON DELETE CASCADE;");
    }

    // --- Data members ------------------------------
    // --- Constructor -----------------------------------
    public H2EmbeddedRepSrc() {
    }

    public H2EmbeddedRepSrc(String relativeBaseDir,
            String database, String user, String password) {
        super("h2", relativeBaseDir, database, user, password);
    }

    // --- Data Accessors -----------------------------------
    // --- Methods -----------------------------------
    @JsonIgnore
    /**
     * This extended URL disables automatic database creation for embedded mode.
     */
    public String getJdbcURL_NoCreateDB() {
        return getJdbcURL() + ";IFEXISTS=TRUE";
    }

    @Override
    public boolean isSchemaInitialized() {
        /* check if database exists and if it has at least one
           required table */
        boolean initialized = false;
        String sqlExistsTableExperiment = "SELECT EXISTS (\n"
                + "   SELECT FROM information_schema.tables \n"
                + "   WHERE  table_schema = 'PUBLIC'\n"
                + "   AND    table_name   = 'EXPERIMENT'\n"
                + "   ) AS \"exists\";";
        Statement stmt;
        ResultSet rs;
        Connection tmpConn = null;
        try {
            // disable automatic database creation for embedded mode
            tmpConn = DriverManager.getConnection(getJdbcURL_NoCreateDB(), user, password);
            // check if PUBLIC.EXPERIMENT table exists
            stmt = tmpConn.createStatement();
            rs = stmt.executeQuery(sqlExistsTableExperiment);
            rs.next();
            initialized = rs.getBoolean("exists");
            logger.info("Database " + database + " already exist in "
                    + relativeBaseDir + " and it is initialized");
        } catch (SQLException ex1) {
            if (ex1.getSQLState().equalsIgnoreCase("90146")) { // 90146 => DATABASE_NOT_FOUND_WITH_IF_EXISTS_1
                logger.info("Database " + database + " does not exist in " + relativeBaseDir);
            } else {
                logger.error(ex1.getMessage());
            }
        } finally {
            try {
                if (tmpConn != null) {
                    tmpConn.close();
                }
            } catch (SQLException ex3) {
                logger.error(ex3.getMessage());
            }
        }
        return initialized;
    }

    @Override
    public boolean initializeSchema() {
        boolean initialized = false;
        String sqlCmd;
        Statement stmt;
        Connection tmpConn;
        try {
            // create embedded database
            tmpConn = DriverManager.getConnection(getJdbcURL(), user, password);
            logger.info("Initializing schema for " + this.driver + " database: "
                    + this.database + "\nExecuting SQL DDL commands:\n");
            // create all database schema objects (tables, views, etc)
            for (Map.Entry<Integer, String> e : schemaInitScript.entrySet()) {
                sqlCmd = e.getValue();
                logger.info("\t No " + e.getKey() + "\n\t" + sqlCmd);
                try {
                    stmt = tmpConn.createStatement();
                    stmt.execute(sqlCmd);
                } catch (SQLException ex) {
                    logger.error(ex.getMessage());
                }
            }
            // consider database and schema as initialized
            initialized = true;
            // preserve this connection
            conn = tmpConn;
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        }
        return initialized;
    }
}
