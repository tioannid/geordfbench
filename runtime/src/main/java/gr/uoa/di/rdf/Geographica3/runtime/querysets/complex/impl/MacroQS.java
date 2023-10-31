package gr.uoa.di.rdf.Geographica3.runtime.querysets.complex.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.simple.IQuery;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.simple.impl.SimpleQuery.QueryAccuracyValidation;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public class MacroQS extends SimpleQS {

    // --- Static members -----------------------------
    static {
        logger = Logger.getLogger(MacroQS.class.getSimpleName());
    }

    // --- Data members ------------------------------
    /**
     * Will hold the originally instantiated query set with the template
     * parameters and will be used to make ground copies of the queries in the
     * {@link mapQueries} map.
     *
     */
    @JsonIgnore
    Map<Integer, IQuery> mapTemplateQueries;

    // --- Constructors ------------------------------
    // 1. Partial constructor:
    //      The query list is deserialized from a JSON file placed
    //      in the resources folder or the classpath in general
    //      Queries are placed on a map property
    public MacroQS(String name, String relativeBaseDir,
            boolean hasPredicateQueriesAlso,
            Map<Integer, IQuery> mapQueries,
            Map<String, String> mapUsefulNamespacePrefixes) {
        super(name, relativeBaseDir, hasPredicateQueriesAlso,
                mapQueries, mapUsefulNamespacePrefixes);
        this.setExpectedResults(QueryAccuracyValidation.TEMPLATE_DEPENDENT);
        this.mapTemplateQueries = cloneMap(this.mapQueries);
        this.firstBindingSetValues = new HashMap<>();
    }

    public MacroQS() {
    }

    // --- Data Accessors ----------------------------
    // --- Methods ----------------------------   
    protected static Map<Integer, IQuery> cloneMap(Map<Integer, IQuery> source) {
        Map<Integer, IQuery> mapNew = new HashMap<>();
        for (Entry<Integer, IQuery> e : source.entrySet()) {
            mapNew.put(e.getKey(), e.getValue().clone());
        }
        return mapNew;
    }

    /**
     * Macro querysets are not allowed to be filtered, because the queries are
     * very dependent.
     *
     * @param qryPositions
     * @param filterAction
     */
    @Override
    public void filter(int[] qryPositions, FilterAction filterAction) {
        throw new RuntimeException("Cannot filter queries in "
                + this.getClass().getSimpleName());
    }

    /**
     * Initialize for every iteration of the entire query set. This is used once
 to initialize/translate the queries of the query set before a complete
 run of all the queries takes place. It allows for implementing classes to
 use ad-hoc logic to achieve this..

 MacroQS uses static text queries and therefore does not need to do
 anything.
     */
    @Override
    public void initializeQuerysetIteration() {
        // make a fresh copy of the original template param queries
        this.mapQueries = cloneMap(this.mapTemplateQueries);
    }

    @Override
    public void initializeAfterDeserialization() {
        super.initializeAfterDeserialization();
        this.mapTemplateQueries = cloneMap(this.mapQueries);
    }
}
