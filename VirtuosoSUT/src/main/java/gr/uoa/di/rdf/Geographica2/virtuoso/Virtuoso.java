/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uoa.di.rdf.Geographica2.virtuoso;

import java.io.File;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import virtuoso.rdf4j.driver.VirtuosoRepository;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 * @since 19/11/2018
 */
public class Virtuoso {
    
    // ---------------- Static Mmebers & Methods ---------------------------
    // --------------------- Data Members ----------------------------------
    private String baseDir; // base directory for repository manager
    private Repository repository; // repository
    private RepositoryConnection connection; // repository connection
    private String database = null;
    private String user = null;
    private String passwd = null;
    private Integer port = null;
    private String host = null;

    // --------------------- Constructors ----------------------------------
    // Constructor 1: Connects to a virtuoso database hosted by vso listening 
    // on host:port with credentials <user>/<passwd>
    public Virtuoso(String baseDir, String database, String user, String passwd, Integer port, String host) {
        // check if <baseDir + "/" + database> exists, otherwise throw exception
        File dir = new File(baseDir + "/" + database);
        if (!dir.exists()) {
            throw new RuntimeException("Directory " + dir.getAbsolutePath() + " does not exist.");
        }
        this.baseDir = baseDir;
        this.database = database;
        this.host = host;
        this.port = port;
        this.user = user;
        this.passwd = passwd;
        // retrieve the repository and initialize it
        try {
            this.repository = new VirtuosoRepository("jdbc:virtuoso://" + host + ":" + port, user, passwd);
            this.repository.initialize();
        } catch (RepositoryException e) {
            VirtuosoSUT.logger.error("Virtuoso repository exception " + e.toString());
            throw new RuntimeException("Error retrieving database " + database);
        }
        // create a repository connection, otherwise throw exception
        connection = repository.getConnection();
        if (connection == null) {
            VirtuosoSUT.logger.error("Could not establish connection to database " + database);
            throw new RuntimeException("Could not establish connection to database " + database);
        }
    }

    // --------------------- Data Accessors --------------------------------
    public RepositoryConnection getConnection() {
        return connection;
    }

    // --------------------- Methods --------------------------------
    public void close() {
        VirtuosoSUT.logger.info("Closing connection...");
        try {
            connection.commit();
        } catch (RepositoryException e) {
            VirtuosoSUT.logger.error(e);
        } finally {
            try {
                connection.close();
                this.repository.shutDown();
            } catch (RepositoryException e) {
                VirtuosoSUT.logger.error(e);
            }
            VirtuosoSUT.logger.info("Connection closed.");
        }
    }
    
}
