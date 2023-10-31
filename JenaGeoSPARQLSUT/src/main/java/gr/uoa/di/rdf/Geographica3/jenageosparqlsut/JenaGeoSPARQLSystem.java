/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 * @since 20/03/2023
 */
package gr.uoa.di.rdf.Geographica3.jenageosparqlsut;

import gr.uoa.di.rdf.Geographica3.runtime.sys.interfaces.impl.JenaBasedGeographicaSystem;
import java.util.Map;
import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.log4j.Logger;

public class JenaGeoSPARQLSystem extends JenaBasedGeographicaSystem {

    // --- Static members -----------------------------

    // --- Static block/clause -----------------------
    static {
        // initialize the map of useful JenaBasedGeographicaSystem related RDF prefixes
        // Empty by intention. It is a reminder that subclasses should use the
        // static initialization block!
        logger = Logger.getLogger(JenaGeoSPARQLSystem.class.getSimpleName());
    }

    // --- Data members ------------------------------

    // --- Constructors ------------------------------    
    public JenaGeoSPARQLSystem() {
    }

    public JenaGeoSPARQLSystem(Map<String, String> sysProps) throws Exception {
        super(sysProps);
    }

    // --- Methods -----------------------------------

    /**
     * Initializes the JenaGeoSPARQLSystem using the system properties,
     */
    @Override
    public void init() throws Exception {
        // The setup of GeoSPARQL Jena only needs to be performed once in an 
        // application. After it is setup, querying is performed using Jena’s 
        // standard query methods.
        GeoSPARQLConfig.setupMemoryIndex();
        // Call the super class initialization procedure             
        super.init();
    }
}
