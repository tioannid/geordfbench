package gr.uoa.di.rdf.geordfbench.runtime.sys.interfaces;

import java.util.Map;

/**
 * An interface representing a IGeographicaSystem with basic capabilities of
 * setting system properties and using them to initialize the system.
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public interface IGeographicaSystem {

    /**
     * Initializes the IGeographicaSystem using the system properties.
     */
    void init() throws Exception;

    /**
     * Closes the IGeographicaSystem.
     */
    void close();

    /**
     * Sets the map of IGeographicaSystem properties.
     *
     * These properties will be used for construction and initialization of the
     * IGeographicaSystem.
     *
     * @param sysProps A Map with the properties for the IGeographicaSystem
     * initialization.
     */
    void setSysProps(Map<String, String> sysProps) throws Exception;

    /**
     * Gets the map of useful pairs <tt>(nsprefix, nsIRI)</tt>.
     *
     * @return a Map representing the useful pairs <tt>(nsprefix, nsIRI)</tt>
     */
    Map<String, String> getMapUsefulPrefixes();

    /**
     * Gets the name of the AbstractGeographicaSystem.
     *
     * @return A String with the AbstractGeographicaSystem's name.
     */
    String getName();

}
