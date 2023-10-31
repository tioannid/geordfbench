package gr.uoa.di.rdf.Geographica3.runtime.workloadspecs;

import gr.uoa.di.rdf.Geographica3.runtime.workloadspecs.impl.SimpleGeospatialWL;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import gr.uoa.di.rdf.Geographica3.runtime.datasets.complex.IGeospatialDataSet;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.partofworkload.IQuerySetPartOfWorkload;
import java.util.Map;
import java.io.File;
import java.io.IOException;

/**
 * An interface that represents a regular Workload Specification. Regular means
 * that it comprise one {@link IGeospatialDataSet} and one
 * {@link IQuerySetPartOfWorkload}
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
@JsonDeserialize(as = SimpleGeospatialWL.class)
public interface IGeospatialWorkLoadSpec {

    /**
     * Gets the workload’s name.
     *
     * @return A string representing the workload name
     */
    String getName();

    /**
     * Gets the workload’s relative base directory. The dataset and queryset
     * specification component base directories are relative to this directory.
     *
     * @return A string representing the relative directory
     */
    String getRelativeBaseDir();

    /**
     * Gets the merged union of the maps of useful namespace prefixes of the
     * geospatial dataset and queryset.
     *
     * @return the map of namespace prefixes and IRIs
     */
    Map<String, String> getMapUsefulNamespacePrefixes();

    /**
     * Gets the prefix header from the map of useful namespace prefixes to be
     * used in queries against this geospatial dataset.
     *
     * @return a string representing the prefix header
     */
    String getNamespacePrefixHeader();

    /**
     * Get the workload description
     *
     * @return A String containing a description of the workload
     */
    String prettyPrint();

    /**
     * Serialize the geographica workload to a JSON string
     *
     * @return the serialized JSON string
     */
    String serializeToJSON();

    /**
     * Serialize the geographica workload to a JSON file
     *
     * @param serObjFile File with the serialized object
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    void serializeToJSON(File serObjFile) throws JsonGenerationException, JsonMappingException, IOException;

    /**
     * Gets the geospatial dataset.
     *
     * @return a {@link IGeospatialDataSet} dataset with the provided name
     */
    IGeospatialDataSet getGeospatialDataset();

    /**
     * Gets the geospatial queryset.
     *
     * @return a {@link IQuerySetPartOfWorkload} queryset with the provided name
     */
    IQuerySetPartOfWorkload getGeospatialQueryset();

}
