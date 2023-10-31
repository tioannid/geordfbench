package gr.uoa.di.rdf.Geographica3.runtime.sys.interfaces.impl;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.Geographica3.runtime.sys.interfaces.IGeographicaSystem;

/**
 * A generic abstract base implementation of the IGeographicaSystem interface.
 * This class should be subclassed to generate SesameBasedSystem,
 * RDF4JBasedSystem and JenaBasedSystem.
 * 
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 * @param <C> represents the connection class for each library (Sesame, RDF4J, Jena)
 */
public abstract class AbstractGeographicaSystem<C> implements IGeographicaSystem {

    // --- Static members -----------------------------
    protected static Logger logger = Logger.getLogger(AbstractGeographicaSystem.class.getSimpleName());
    public static final Map<String, String> USEFULL_PREFIXES_MAP = new HashMap<>();
    public static long lastMeasuredOperation = 0;

    // --- Static block/clause -----------------------
    static {
        // initialize the map of useful AbstractGeographicaSystem related RDF 
        // namespace prefixes.
        // Empty by intention. It is a reminder that subclasses should use the
        // static initialization block!
    }

    // --- Data members ------------------------------
    protected Map<String, String> sysProps = null;
    protected boolean initialized = false;
    protected C connection = null;
    
    // --- Data Accessors ----------------------------
    /**
     * Initializes an AbstractGeographicaSystem, by setting the map of 
     * system properties after a validation check {@link validateSysProps} and 
     * invoking the {@link init} method. This is the constructor!
     * 
     * @param sysProps A Map with the properties for the IGeographicaSystem
      * initialization.
     * @throws java.lang.Exception
     */
    @Override
    public void setSysProps(Map<String, String> sysProps) throws Exception {
        if (initialized) { // has already been initialized
            throw new RuntimeException("System of class " + this.getName() + " has already been initialized");
        } else if (!validateSysProps(sysProps)) { // sys pros are invalid
            throw new RuntimeException("System of class " + this.getName() + " has been assigned an invalid property set");
        } else { // not initialized yet and sys pros are valid
            this.sysProps = sysProps;
            init();
        }
    }

    /**
     * Gets the AbstractGeographicaSystem connection object of type {@code C}
     * 
     * @return A {@code C} connection object to the AbstractGeographicaSystem.
     */
    public C getConnection() {
        if (this.connection == null) {
            throw new RuntimeException("System of class " + this.getName() + " does not have a connection initialized");
        } else {
            return connection;
        }
    }

    /**
     * Gets the name of the AbstractGeographicaSystem.
     * 
     * @return A String with the AbstractGeographicaSystem's name.
     */
    @Override
    public String getName() {
        return this.getClass().getName();
    }

    /**
     * Gets the Map of useful namespace prefixes for the 
     * AbstractGeographicaSystem.
     * 
     * @return A Map of useful namespace prefixes.
     */
    @Override
    public Map<String, String> getMapUsefulPrefixes() {
        return USEFULL_PREFIXES_MAP;
    }

    // --- Methods -----------------------------------
    /**
     * Validates map values before they can be copied to object properties.
     * 
     * @param sysProps A Map with system properties
     * @return a {@code true} value means that the provided properties are
     * accepted by the system, a {@code false} value means that some 
     * required property by the system is missing or does not have an
     * acceptable value.
     */
    protected abstract boolean validateSysProps(Map<String, String> sysProps);

    /**
     * Initializes the IGeographicaSystem using the system properties, the
     * last step of which is to set the {@link initialized} property to 
     * {@code true}.
     * Each subclass should define in more detail the steps to properly
     * initialize the specific type of system, the main work being 
     * creating and setting the connection property based on the 
     * appropriately initialized/set sysProps map structure.
     */
    @Override
    public void init() throws Exception {
        this.initialized = true;
    }

    /**
     * Closes the IGeographicaSystem, the last step of which is to set the 
     * {@link initialized} property to <tt>false</tt>.
     * Each subclass should define in more detail the steps to properly
     * shutdown the specific type of system, the main work being 
     * gracefully closing the connection and related file descriptors. 
     */
    @Override
    public void close() {
        this.connection = null;
        this.initialized = false;
    }
}
