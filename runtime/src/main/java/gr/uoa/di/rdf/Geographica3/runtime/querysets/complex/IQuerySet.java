package gr.uoa.di.rdf.Geographica3.runtime.querysets.complex;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import gr.uoa.di.rdf.Geographica3.runtime.executionspecs.IExecutionSpec;
import java.util.Map;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.simple.IQuery;
import java.io.File;
import java.io.IOException;

/**
 * An interface that represents an independent Query Set.
 * It's use can be determined by specifying an {@link IExecutionSpec.Action}.
 * Operations can be limited on a subpart of the queryset by using the 
 * {@link FilterAction} which allows either to include or exclude a list of
 * query numbers.
 * It is independent because it is not binded to a {@link IExecutionSpec}.
 * 
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public interface IQuerySet {

    /**
     * Serialize the geographica queryset to a JSON string
     *
     * @return the serialized JSON string
     */
    String serializeToJSON();

    /**
     * Serialize the geographica queryset to a JSON file
     *
     * @param serObjFile File with the serialized object
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    void serializeToJSON(File serObjFile) throws JsonGenerationException, JsonMappingException, IOException;

    // --- Data Accessors ----------------------------
    IExecutionSpec.Action getAction();

    void setAction(IExecutionSpec.Action action);

    /**
     * Enumeration for filtering the map queries. Used with an int[] to filter
     * out (EXCLUDE) or preserve (INCLUDE) the map queries
     */
    public enum FilterAction {
        INCLUDE,
        EXCLUDE
    }

    /**
     * Gets the queryset’s name.
     *
     * @return A string representing the queryset name
     */
    String getName();

    /**
     * Gets the queryset’s relative base directory.
     *
     * @return A string representing the relative directory
     */
    String getRelativeBaseDir();

    /**
     * Reports if there are predicate based versions of the queries.
     *
     * @return
     */
    boolean isHasPredicateQueriesAlso();

    /**
     * Gets the header with all prefixes for the queryset.
     *
     * @return A String representing the prefix header
     */
    String getNamespacePrefixHeader();

    /**
     * Gets the map of queries with their position in the queryset.
     *
     * @return the map of queries
     */
    Map<Integer, IQuery> getMapQueries();

    int getQueriesNum();

    /**
     * Gets the map of useful namespace prefixes for the queryset.
     *
     * @return the map of useful namespace prefixes
     */
    Map<String, String> getMapUsefulNamespacePrefixes();

    /**
     * Gets a query, searching by position in the query map.
     *
     * @param position the query position in the query map
     * @return the query in the provided position
     */
    IQuery getQuery(int position);

    /**
     * Get the queryset description
     *
     * @return A String containing a description of the queryset
     */
    String prettyPrint();

    /**
     * Filters the queries map either by preserving or removing the queries with
     * position listed in the int[].
     *
     * @param qryPositions <tt>int[]</tt> with query positions
     * @param filterAction IQuerySet.FilterAction.<tt>EXCLUDE</tt> to exclude the
 query positions provided or IQuerySet.FilterAction.<tt>INCLUDE</tt> to
     * preserve the query positions provided and exclude all others.
     */
    void filter(int[] qryPositions, FilterAction filterAction);

    /**
     * Adds the entries of the provided map namespace prefixes to the underlying
     * map of useful namespace prefixes.
     *
     * @param mapUsefulNamespacePrefixes A {@link Map} of useful namespace
     * prefixes to add
     */
    void addMapUsefulNamespacePrefixes(Map<String, String> mapUsefulNamespacePrefixes);

    /**
     * Initialize for every iteration of the entire query set. This is used once
     * to initialize/translate the queries of the query set before a complete
     * run of all the queries takes place. It allows for implementing classes to
     * use ad-hoc logic to achieve this..
     *
     */
    void initializeQuerysetIteration();

    void initializeAfterDeserialization();

    Map<String, String> getFirstBindingSetValues();

    void setFirstBindingSetValues(Map<String, String> firstBindingSetValues);

}
