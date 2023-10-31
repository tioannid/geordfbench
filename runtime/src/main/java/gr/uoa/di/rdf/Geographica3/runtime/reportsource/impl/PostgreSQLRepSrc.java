package gr.uoa.di.rdf.Geographica3.runtime.reportsource.impl;

import org.apache.log4j.Logger;
import java.sql.*;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public class PostgreSQLRepSrc extends JDBCRepSrc {

    // --- Static members -----------------------------
    static {
        logger = Logger.getLogger(PostgreSQLRepSrc.class.getSimpleName());
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
}
