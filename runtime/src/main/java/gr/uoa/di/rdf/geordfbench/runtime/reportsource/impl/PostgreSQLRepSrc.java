package gr.uoa.di.rdf.geordfbench.runtime.reportsource.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.log4j.Logger;
import java.sql.*;

/**
 * A base abstract class for all non-embedded JDBC report sources implementing
 * interface {@link IReportSource}.
 *
 * @author Theofilos Ioannidis <tioannid@di.uoa.gr>
 * @creationdate 26/11/2020
 * @updatedate 01/09/2024
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY,
        property = "classname")
public class PostgreSQLRepSrc extends JDBCRepSrc {

    // --- Static members -----------------------------
    static {
        logger = Logger.getLogger(PostgreSQLRepSrc.class.getSimpleName());
        // load all DDL commands to the schema initialization script
        schemaInitScript.put(1, "SET statement_timeout = 0;\n"
                + "SET lock_timeout = 0;\n"
                //                + "SET idle_in_transaction_session_timeout = 0;\n"
                + "SET client_encoding = 'UTF8';\n"
                + "SET standard_conforming_strings = on;\n"
                + "SELECT pg_catalog.set_config('search_path', '', false);\n"
                + "SET check_function_bodies = false;\n"
                + "SET xmloption = content;\n"
                + "SET client_min_messages = warning;\n"
                + "SET row_security = off;\n"
                + "\n"
                + "DROP DATABASE IF EXISTS geordfbench;\n"
                + "DROP ROLE IF EXISTS geordfbench;");
        schemaInitScript.put(2, "CREATE ROLE geordfbench LOGIN\n"
                + "  ENCRYPTED PASSWORD 'md5026b364a2c8491da2cca1b5772723d43'\n"
                + "  NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE NOREPLICATION;\n"
                + "\n"
                + "ALTER USER geordfbench WITH PASSWORD 'geordfbench';");
        schemaInitScript.put(3, "CREATE DATABASE geordfbench\n"
                + "    WITH \n"
                + "    TEMPLATE template0\n"
                + "    OWNER = geordfbench\n"
                + "    ENCODING = 'UTF8'\n"
                + "    LC_COLLATE = 'en_US.UTF-8'\n"
                + "    LC_CTYPE = 'en_US.UTF-8'\n"
                + "    TABLESPACE = pg_default\n"
                + "    CONNECTION LIMIT = -1;\n"
                + "\n"
                + "ALTER DATABASE geordfbench OWNER TO geordfbench;\n");
        // For the following commands to work properly a new
        // jdbc connection needs to be made to the newly
        // created <geordfbench> database
        schemaInitScript.put(4, "SET statement_timeout = 0;\n"
                + "SET lock_timeout = 0;\n"
                //                + "SET idle_in_transaction_session_timeout = 0;\n"
                + "SET client_encoding = 'UTF8';\n"
                + "SET standard_conforming_strings = on;\n"
                + "SELECT pg_catalog.set_config('search_path', '', false);\n"
                + "SET check_function_bodies = false;\n"
                + "SET xmloption = content;\n"
                + "SET client_min_messages = warning;\n"
                + "SET row_security = off;");
        schemaInitScript.put(5, "CREATE TABLE public.\"EXPERIMENT\" (\n"
                + "    id integer NOT NULL,\n"
                + "    instime timestamp(3) with time zone DEFAULT ('now'::text)::timestamp(3) with time zone,\n"
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
                + ");\n"
                + "\n"
                + "ALTER TABLE public.\"EXPERIMENT\" OWNER TO geordfbench;\n"
                + "\n"
                + "CREATE SEQUENCE public.\"EXPERIMENT_id_seq\"\n"
                + "    START WITH 1\n"
                + "    INCREMENT BY 1\n"
                + "    NO MINVALUE\n"
                + "    NO MAXVALUE\n"
                + "    CACHE 1;\n"
                + "\n"
                + "ALTER TABLE public.\"EXPERIMENT_id_seq\" OWNER TO geordfbench;\n"
                + "\n"
                + "ALTER SEQUENCE public.\"EXPERIMENT_id_seq\" OWNED BY public.\"EXPERIMENT\".id;");
        schemaInitScript.put(6, "CREATE TABLE public.\"QUERYEXECUTION\" (\n"
                + "    id integer NOT NULL,\n"
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
                + ");\n"
                + "\n"
                + "ALTER TABLE public.\"QUERYEXECUTION\" OWNER TO geordfbench;\n"
                + "\n"
                + "CREATE SEQUENCE public.\"QUERYEXECUTION_experiment_id_seq\"\n"
                + "    START WITH 1\n"
                + "    INCREMENT BY 1\n"
                + "    NO MINVALUE\n"
                + "    NO MAXVALUE\n"
                + "    CACHE 1;\n"
                + "\n"
                + "ALTER TABLE public.\"QUERYEXECUTION_experiment_id_seq\" OWNER TO geordfbench;\n"
                + "\n"
                + "ALTER SEQUENCE public.\"QUERYEXECUTION_experiment_id_seq\" OWNED BY public.\"QUERYEXECUTION\".experiment_id;\n"
                + "\n"
                + "CREATE SEQUENCE public.\"QUERYEXECUTION_id_seq\"\n"
                + "    START WITH 1\n"
                + "    INCREMENT BY 1\n"
                + "    NO MINVALUE\n"
                + "    NO MAXVALUE\n"
                + "    CACHE 1;\n"
                + "\n"
                + "ALTER TABLE public.\"QUERYEXECUTION_id_seq\" OWNER TO geordfbench;\n"
                + "\n"
                + "ALTER SEQUENCE public.\"QUERYEXECUTION_id_seq\" OWNED BY public.\"QUERYEXECUTION\".id;\n"
                + "\n"
                + "");
        schemaInitScript.put(7, "CREATE VIEW public.vqueryexecution AS\n"
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
                + "ALTER TABLE public.vqueryexecution OWNER TO geordfbench;\n"
                + "\n"
                + "CREATE VIEW public.vquery_ordered_aggrs AS\n"
                + " SELECT vqueryexecution.experiment_id,\n"
                + "    vqueryexecution.query_no,\n"
                + "    vqueryexecution.cache_type,\n"
                + "    count(vqueryexecution.iteration) AS no_iterations,\n"
                + "    round(avg(vqueryexecution.total_time_s), 3) AS mean,\n"
                + "    percentile_disc((0.5)::double precision) WITHIN GROUP (ORDER BY vqueryexecution.total_time_s) AS median\n"
                + "   FROM public.vqueryexecution\n"
                + "  GROUP BY vqueryexecution.experiment_id, vqueryexecution.query_no, vqueryexecution.cache_type;\n"
                + "\n"
                + "ALTER TABLE public.vquery_ordered_aggrs OWNER TO geordfbench;");
        schemaInitScript.put(8, "CREATE VIEW public.vqueryexecution2 AS\n"
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
                + "ALTER TABLE public.vqueryexecution2 OWNER TO geordfbench;\n"
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
                + " HAVING (vqueryexecution2.validflag = 'Success'::text);\n"
                + "\n"
                + "ALTER TABLE public.vquery_ordered_aggrs2 OWNER TO geordfbench;");
        schemaInitScript.put(9, "CREATE VIEW public.vqueryexecution3 AS\n"
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
                + "ALTER TABLE public.vqueryexecution3 OWNER TO geordfbench;\n"
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
                + "  GROUP BY v.experiment_id, v.sut, v.queryset, v.dataset, v.query_label, v.query_no, v.validflag, v.cache_type;\n"
                + "\n"
                + "ALTER TABLE public.vquery_ordered_aggrs_3 OWNER TO geordfbench;");
        schemaInitScript.put(10, "CREATE VIEW public.vreport AS\n"
                + " SELECT v.cache_type,\n"
                + "    v.query_no,\n"
                + "    v.query_label,\n"
                + "    v.validflag,\n"
                + "    v.sut,\n"
                + "    v.mean,\n"
                + "    v.median\n"
                + "   FROM public.vquery_ordered_aggrs_3 v;\n"
                + "\n"
                + "ALTER TABLE public.vreport OWNER TO geordfbench;");
        schemaInitScript.put(11, "ALTER TABLE ONLY public.\"EXPERIMENT\" ALTER COLUMN id SET DEFAULT nextval('public.\"EXPERIMENT_id_seq\"'::regclass);\n"
                + "ALTER TABLE ONLY public.\"QUERYEXECUTION\" ALTER COLUMN id SET DEFAULT nextval('public.\"QUERYEXECUTION_id_seq\"'::regclass);\n"
                //                + "ALTER TABLE ONLY public.\"QUERYEXECUTION\" ALTER COLUMN experiment_id SET DEFAULT nextval('public.\"QUERYEXECUTION_experiment_id_seq\"'::regclass);\n"
                + "ALTER TABLE ONLY public.\"EXPERIMENT\"\n"
                + "    ADD CONSTRAINT \"EXPERIMENT_pkey\" PRIMARY KEY (id);\n"
                //                + "CREATE INDEX \"FKI_EXPERIMENT_ID\" ON public.\"QUERYEXECUTION\" USING btree (experiment_id);\n"
                + "ALTER TABLE ONLY public.\"QUERYEXECUTION\"\n"
                + "    ADD CONSTRAINT \"FK_EXPERIMENT_ID\" FOREIGN KEY (experiment_id) REFERENCES public.\"EXPERIMENT\"(id) ON DELETE CASCADE;");
    }

    // --- Data members ------------------------------
    // --- Constructor -----------------------------------
    public PostgreSQLRepSrc() {
    }

    public PostgreSQLRepSrc(String hostname, String althostname, int port,
            String database, String user, String password) throws SQLException {
        super("postgresql", hostname, althostname, port, database, user, password);
    }

    // --- Data Accessors -----------------------------------
    // --- Methods -----------------------------------
    @Override
    protected String getJdbcURL_NoDatabase() {
        StringBuilder sb = new StringBuilder("jdbc:");
        sb.append(this.driver).append("://");
        sb.append(this.hostname).append(":");
        sb.append(this.port).append("/");
        sb.append("postgres").append("?");
        sb.append("user=").append("postgres").append("&");
        sb.append("password=").append("postgres");
        return sb.toString();
    }

    protected String getJdbcAlternateURL_NoDatabase() {
        StringBuilder sb = new StringBuilder("jdbc:");
        sb.append(this.driver).append("://");
        sb.append(this.althostname).append(":");
        sb.append(this.port).append("/");
        sb.append("postgres").append("?");
        sb.append("user=").append("postgres").append("&");
        sb.append("password=").append("postgres");
        return sb.toString();
    }

    @Override
    public boolean isSchemaInitialized() {
        // check if database exists and if it has at least one
        // required table
        boolean dbExists = false, initialized = false, useAlthost = false;
        String sqlDatabaseExists = "SELECT Count(*) AS cntrows\n"
                + "FROM pg_catalog.pg_database\n"
                + "WHERE lower(datname) = lower('geordfbench');";
        String sqlExistsTableExperiment = "SELECT EXISTS (\n"
                + "   SELECT FROM information_schema.tables \n"
                + "   WHERE  table_schema = 'public'\n"
                + "   AND    table_name   = 'EXPERIMENT'\n"
                + "   ) AS \"exists\";";
        Statement stmt;
        ResultSet rs;
        Connection tmpConn = null;
        try {
            // connect to <postgres> database using hostname
            tmpConn = DriverManager.getConnection(getJdbcURL_NoDatabase());
        } catch (SQLException ex) {
            if ("08001".equalsIgnoreCase(ex.getSQLState())) {
                try {
                    useAlthost = true;
                    // connect to <postgres> database using althostname
                    tmpConn = DriverManager.getConnection(getJdbcAlternateURL_NoDatabase());
                } catch (SQLException exx) {
                    logger.error(exx.getMessage());
                }
            } else {
                logger.error(ex.getMessage());
            }
        }
        try {
            // check if <geordfbench> database already exists
            stmt = tmpConn.createStatement();
            rs = stmt.executeQuery(sqlDatabaseExists);
            rs.next();
            dbExists = (rs.getLong("cntrows") == 1);
            rs.close();
            stmt.close();
            tmpConn.close();
            // if <geordfbench> database exists check if it's initialized
            if (dbExists) {
                // connect to <geordfbench> database
                tmpConn = DriverManager.getConnection(useAlthost ? getAltJdbcURL() : getJdbcURL());
                // check if PUBLIC.EXPERIMENT table exists
                stmt = tmpConn.createStatement();
                rs = stmt.executeQuery(sqlExistsTableExperiment);
                rs.next();
                initialized = rs.getBoolean("exists");
            }
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        } finally {
            try {
                tmpConn.close();
            } catch (SQLException ex2) {
                logger.error(ex2.getMessage());
            }
        }
        return initialized;
    }

    @Override
    public boolean initializeSchema() {
        boolean initialized = false, useAlthost = false;
        String sqlCmd;
        Statement stmt;
        Connection tmpConn = null;
        try {
            // create the database and role
            tmpConn = DriverManager.getConnection(getJdbcURL_NoDatabase(), user, password);
        } catch (SQLException ex) {
            if ("08001".equalsIgnoreCase(ex.getSQLState())) {
                try {
                    useAlthost = true;
                    // connect to <postgres> database using althostname
                    tmpConn = DriverManager.getConnection(getJdbcAlternateURL_NoDatabase(), user, password);
                } catch (SQLException exx) {
                    logger.error(exx.getMessage());
                }
            } else {
                logger.error(ex.getMessage());
            }
        }
        try {
            logger.info("Creating " + this.driver + " database: "
                    + this.database + "\nExecuting SQL DDL commands (Phase 1):\n");
            for (int i = 1; i <= 3; i++) {
                sqlCmd = schemaInitScript.get(i);
                logger.info("\t-- No " + i + "\n\t" + sqlCmd);
                try {
                    stmt = tmpConn.createStatement();
                    stmt.execute(sqlCmd);
                } catch (SQLException ex) {
                    logger.error(ex.getMessage());
                }
            }
            tmpConn.close();
            // create all database schema objects (tables, views, etc)
            tmpConn = DriverManager.getConnection(useAlthost ? getAltJdbcURL() : getJdbcURL());
            logger.info("Initializing schema for " + this.driver + " database: "
                    + this.database + "\nExecuting SQL DDL commands  (Phase 2):\n");
            for (int i = 4; i <= schemaInitScript.size(); i++) {
                sqlCmd = schemaInitScript.get(i);
                logger.info("\t-- No " + i + "\n\t" + sqlCmd);
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
