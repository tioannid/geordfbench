package gr.uoa.di.rdf.geordfbench.runtime.querysets.simple;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import gr.uoa.di.rdf.geordfbench.runtime.querysets.simple.impl.SimpleQuery;
import gr.uoa.di.rdf.geordfbench.runtime.resultscollector.impl.QueryRepResults.QueryRepResult;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
@JsonDeserialize(as = SimpleQuery.class)
public interface IQuery {

    /**
     * Gets the query label or name
     *
     * @return
     */
    String getLabel();

    /**
     * Sets the query label or name
     *
     * @param label the query label or name
     */
    void setLabel(String label);

//    /**
//     * Gets the composite query label that combines position or index number in
//     * the {@link IQuerySet} instance, whether it is a spatial predicate version
//     * of the query and the query label or name
//     * @param indexNo the query position or index number in the {@link IQuerySet} instance
//     * @return the composite query label
//     */
//    String getQnnLabel(int indexNo);
    /**
     * Gets the query text without namespace prefixes
     *
     * @return the query text
     */
    String getText();

    /**
     * Sets the query text without namespace prefixes
     *
     * @param query the query text without namespace prefixes
     */
    void setText(String query);

    /**
     * Does the query have spatial predicate instead of spatial function?
     *
     * @return <tt>true</tt> if the query has spatial predicate
     */
    boolean isUsePredicate();

    /**
     * Sets the query flag to denote that the query uses spatial predicate
     * instead of spatial function
     *
     * @param usePredicate <tt>true</tt> if the query has spatial predicate
     */
    void setUsePredicate(boolean usePredicate);

    /**
     * Return a cloned query which has a namespace prefix header added to it.
     *
     * @param namespacePrefixHeader the namespace prefix header to add
     * @return a clone of the query which has a namespace prefix header
     */
    IQuery getNamespaceHeaderedQuery(String namespacePrefixHeader);

    /**
     * Returns a deep copy clone of some query instance
     *
     * @return a {@link IQuery} instance
     */
    IQuery clone();

    /**
     * Returns the number of results the query is expected to return to be
     * considered accurate
     *
     * @return expected number of results
     */
    public long getExpectedResults();

    /**
     * Sets the number of results the query should return
     *
     * @param expectedResults the number of results the query should return
     */
    void setExpectedResults(long expectedResults);

    /**
     * if accuracy validation is possible, it checks the provided number against
     * the query's expected results number
     *
     * @param results the number of results returned by a query run
     * @return <tt>true</tt> if the provided number equals the query's expected
     * results number
     */
    public boolean isResultAccurate(long results);

    /**
     * if accuracy validation is possible, and result has no exception it checks 
     * the provided number against the query's expected results number.
     *
     * @param queryRepResult Query repetition result
     * @return <tt>true</tt> if the query repetition result has number of results 
     * equals the query's expected results number and the result has no exception.
     */
    public boolean isResultAccurate(QueryRepResult queryRepResult);

}
