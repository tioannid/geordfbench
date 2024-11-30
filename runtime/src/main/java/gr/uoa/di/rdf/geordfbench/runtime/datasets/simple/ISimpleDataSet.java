package gr.uoa.di.rdf.geordfbench.runtime.datasets.simple;

import java.util.Map;

/**
 * An interface for a simple one file RDF dataset with a map of
 * useful namespace prefixes.
 * 
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public interface ISimpleDataSet {
    
    /** 
     * Gets the dataset’s name.
     * 
     * @return A string representing the dataset name
    */
    String getName();
    
    /** 
     * Gets the dataset’s relative base directory where
     * the RDF file is located.
     * 
     * @return A string representing the dataset 
     * relative base directory
    */
    String getRelativeBaseDir();
    
    /** 
     * Gets the dataset’s file name which when prefixed by the
     * {@link getRelativeBaseDir()} provides the full path to the file.
     * 
     * @return A string representing the dataset file name 
    */
    String getDataFile();
    
    /** 
     * Gets the dataset’s RDF file format.
     * 
     * @return A string representing the dataset RDF file format 
    */
    String getRdfFormat();
    
    /** 
     * Adds a useful namespace prefix for the dataset.
     * 
     * @param nsPrefix A String containing the namespace prefix
     * @param nsIRI A String containing the namespace IRI
     * file name 
    */    
    void addUsefulNamespacePrefix(String nsPrefix, String nsIRI);

    /** 
     * Gets the map of useful namespace prefixes for the dataset.
     * 
     * @return a map with the dataset useful namespace prefixes.
    */
    Map<String, String> getMapUsefulNamespacePrefixes();

    /** 
     * Gets the prefix header for the dataset useful namespace prefixes.
     * 
     * @return A String containing the header with the namespace prefixes
     */
    String getNamespacePrefixHeader();

    /** 
     * Get the dataset description
     * 
     * @return A String containing a description of the dataset 
     */
    String prettyPrint();
}
