package gr.uoa.di.rdf.geordfbench.runtime.datasets.complex;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import gr.uoa.di.rdf.geordfbench.runtime.datasets.complex.impl.GeographicaDS;
import java.util.List;
import java.util.Map;
import gr.uoa.di.rdf.geordfbench.runtime.datasets.simple.IGeospatialSimpleDataSet;
import java.io.File;
import java.io.IOException;

/**
 * An interface that represents a complex Geospatial Dataset.
 * Complex means that it may comprise more than one
 * simple geospatial datasets.
 * 
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
@JsonDeserialize(as = GeographicaDS.class)
public interface IGeospatialDataSet {

    /**
     * Gets the dataset’s name.
     *
     * @return A string representing the dataset name
     */
    String getName();

    /**
     * Gets the dataset’s relative base directory.
     *
     * @return A string representing the relative directory
     */
    String getRelativeBaseDir();
    
    /**
     * Gets the list of simple geospatial datasets that
     * make up this geospatial dataset.
     * 
     * @return the list of simple geospatial datasets
     */
    List<IGeospatialSimpleDataSet> getSimpleGeospatialDataSetList();
    
    /**
     * Is this geospatial dataset made of only 1 
     * simple geospatial dataset?
     *
     * @return is this a simple geospatial dataset?
     */    
    boolean isSimple();

    /**
     * Gets a simple geospatial dataset, searching by name in
     * the list of simple geospatial datasets.
     *
     * @param name simple geospatial dataset name
     * @return a simple geospatial dataset with the provided name
     */    
    IGeospatialSimpleDataSet getSimpleGeospatialDataset(String name);

    /**
     * Adds a simple geospatial dataset to the list of
     * simple geospatial datasets that make up this 
     * geospatial dataset.
     * 
     * @param simpleGeoDS the simple geospatial dataset to add to the list
     */
    void addSimpleGeospatialDataSet(IGeospatialSimpleDataSet simpleGeoDS);

    /**
     * Gets the context/graph IRI, searching by simple geospatial 
     * dataset name in the map of simple geospatial dataset 
     * <name, context> pairs.
     * 
     * @param name simple geospatial dataset name
     * @return a String representing the context or graph IRI
     */
    String getSimpleGeospatialDataSetContext(String name);

    /**
     * Adds a pair of <name, context> in the map of simple 
     * geospatial dataset <name, context> pairs.
     * 
     * @param name the name of a simple geospatial dataset
     * @param context the context/graph IRI of the simple dataset
     */
    void addSimpleGeospatialDataSetContext(String name, String context);

    /**
     * Gets the merged union of the maps of useful namespace prefixes
     * of this geospatial dataset.
     * 
     * @return the map of namespace prefixes and IRIs
     */
    Map<String, String> getMapUsefulNamespacePrefixes();

    /**
     * Gets the prefix header from the map of useful
     * namespace prefixes to be used in queries against 
     * this geospatial dataset.
     * 
     * @return a string representing the prefix header 
     */
    String getNamespacePrefixHeader();

    /** Get the dataset description
     * 
     * @return A String containing a description of the dataset 
     */
    String prettyPrint();

    /**
     * Serialize the geographica dataset to a JSON string
     *
     * @return the serialized JSON string
     */
    String serializeToJSON();

    /**
     * Serialize the geographica dataset to a JSON file
     *
     * @param serObjFile File with the serialized object
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    void serializeToJSON(File serObjFile) throws JsonGenerationException, JsonMappingException, IOException;
}
