/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
package gr.uoa.di.rdf.Geographica3.strabonsut;

import java.util.Map;
import org.apache.log4j.Logger;
import eu.earthobservatory.runtime.postgis.Strabon;
import gr.uoa.di.rdf.Geographica3.runtime.sys.interfaces.impl.SesamePostGISBasedGeographicaSystem;
import java.sql.SQLException;

public class StrabonSystem extends SesamePostGISBasedGeographicaSystem {

    // --- Static members -----------------------------
    // --- Static block/clause -----------------------
    static {
        // initialize the map of useful StrabonSystem related RDF prefixes
        // Empty by intention. It is a reminder that subclasses should use the
        // static initialization block!
        logger = Logger.getLogger(StrabonSystem.class.getSimpleName());
    }

    // --- Data members ------------------------------
    private Strabon strabon;

    // --- Constructors ------------------------------    
    public StrabonSystem() {
    }

    public StrabonSystem(Map<String, String> sysProps) throws Exception {
        super(sysProps);
    }

    // --- Data Accessors
    public Strabon getStrabon() {
        return strabon;
    }

    public void setStrabon(Strabon strabon) {
        this.strabon = strabon;
        this.connection = strabon.getSailRepoConnection();
    }

    // --- Methods -----------------------------------
    /**
     * Initializes the StrabonSystem using the system properties, the last step
     * of which is to set the {@link initialized} property to {@code true}. It
     * uses the mandatory fields to create a connection
     */
    @Override
    public void init() throws Exception {
        // STEP 1: create a eu.earthobservatory.runtime.postgis.Strabon instance
        //          and implicitly retrieve its internal SailRepositoryConnection
        boolean successfullStrabonCreation = false;
        try {
            this.setStrabon(new Strabon(dbname, dbuser, dbpasswd, Integer.parseInt(this.dbport), dbhost, true));
            successfullStrabonCreation = true;
        } catch (SQLException e) { // SQL exception, probably DB does not exist or connection string related issues
            logger.error("Cannot create new instance of " + this.getClass().getName());
            logger.error(e.getMessage());
        } catch (ClassNotFoundException e) { // could not load PostgreSQL JDBC driver
            logger.error("Cannot create new instance of " + this.getClass().getName());
            logger.error(e.getMessage());
        } catch (Exception e) { // database already locked by another process
            logger.error("Cannot create new instance of " + this.getClass().getName());
            logger.error(e.getMessage());
        }
        // STEP 5: Call the super class initialization procedure             
        if (successfullStrabonCreation) {
            super.init();
        } else {
            logger.error("Skipping remaining initialization!");
            throw new Exception("Cannot create new instance of " + this.getClass().getName());
        }
    }

}
