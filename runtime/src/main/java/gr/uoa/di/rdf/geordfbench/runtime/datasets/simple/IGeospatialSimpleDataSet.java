package gr.uoa.di.rdf.geordfbench.runtime.datasets.simple;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import gr.uoa.di.rdf.geordfbench.runtime.datasets.simple.impl.GenericGeospatialSimpleDS;
import java.util.Map;

/**
 * An interface which extends the {@link ISimpleDataSet} interface with maps of
 * geospatial RDF properties for #asWKT and #hasGeometry.
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
@JsonDeserialize(as = GenericGeospatialSimpleDS.class)
public interface IGeospatialSimpleDataSet extends ISimpleDataSet {

    /**
     * Adds an asWKT property to the map of asWKT properties.
     * 
     * @param name identifier name for the asWKT property in the map
     * @param asWKT a String representing the asWKT property
     */
    void addAsWKT(String name, String asWKT);

    /**
     * Adds an hasGeometry property to the map of hasGeometry properties.
     * 
     * @param name identifier name for the hasGeometry property in the map
     * @param hasGeometry a String representing the hasGeometry property
     */
    void addHasGeometry(String name, String hasGeometry);

    /**
     * Gets the map of asWKT properties of the dataset.
     *
     * @return a map with the dataset asWKT properties
     */
    Map<String, String> getMapAsWKT();

    /**
     * Gets the map of hasGeometry properties of the dataset.
     *
     * @return a map with the dataset hasGeometry properties
     */
    Map<String, String> getMapHasGeometry();

    /**
     * Gets a printout of the map of asWKT properties.
     * 
     * @return A String with the printout
     */
    String printAsWKTs();

    /**
     * Gets a printout of the map of hasGeometry properties.
     * 
     * @return A String with the printout
     */
    String printHasGeometries();
}
