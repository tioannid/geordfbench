/**
 * @author GeoRDFBench Creator <GeoRDFBench@Univ>
 */
package gr.uoa.di.rdf.Geographica3.runtime.querysets.complex.impl;

import java.util.Map;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.simple.IQuery;

// StaticTempParamQS relates a GeographicaDataSet dataset
// and an AbstractGeographicaSystem<C> _sys. By default the mapUsefulPrefixes
// is initialized with the union of the mapUsefulPrefixes of dataset and _sys.
public class StaticTempParamQS extends SimpleQS {

// --- Static members -----------------------------
    // --- Static block/clause -----------------------
    static {
        logger = Logger.getLogger(StaticTempParamQS.class.getSimpleName());
    }
    // --- Data members ------------------------------
    Map<String, String> mapTemplateParams;
    Map<String, String> mapGraphPrefixes;

    // --- Constructors ------------------------------
    // 1. Partial constructor:
    //      The query list is deserialized from a JSON file placed
    //      in the resources folder or the classpath in general
    //      Queries are placed on a map property
    //      then only one set of queries (spatial predicate or functions) is kept
    //      and static template params are replaced
    public StaticTempParamQS(String name, String relativeBaseDir,
            boolean hasPredicateQueriesAlso,
            Map<Integer, IQuery> mapQueries,
            Map<String, String> mapTemplateParams,
            Map<String, String> mapUsefulNamespacePrefixes,
            Map<String, String> mapGraphPrefixes) {
        super(name, relativeBaseDir, hasPredicateQueriesAlso,
                mapQueries, mapUsefulNamespacePrefixes);
        this.mapTemplateParams = mapTemplateParams;
        this.mapGraphPrefixes = mapGraphPrefixes;
        this.instantiateStaticTemplateParameters();
    }

    public StaticTempParamQS() {
    }

    // --- Data Accessors ----------------------------
    public Map<String, String> getMapTemplateParams() {
        return mapTemplateParams;
    }

    public void setMapTemplateParams(Map<String, String> mapTemplateParams) {
        this.mapTemplateParams = mapTemplateParams;
    }

    public Map<String, String> getMapGraphPrefixes() {
        return mapGraphPrefixes;
    }

    public void setMapGraphPrefixes(Map<String, String> mapGraphPrefixes) {
        this.mapGraphPrefixes = mapGraphPrefixes;
    }

    // --- Methods ----------------------------    
    // Adds dynamic to handling queries. IQuery lists can be generic and with
    // appropriately implementing this function the user can change them to 
    // their desired format.
    // Replace static template literals for actual values, i.e. literal polygon read
    // from a file.
    // NOTE: For dynamic template literals or more generally for iteration 
    //       dependent queries the class needs to override the
    // StaticTempParamQS.getQuery(int queryNo, int repetition)
    // method which by default does not handle the repetition argument
    protected void instantiateStaticTemplateParameters() {
        IQuery qs = null;
        // replace all template parameters with values for all queries
        for (Map.Entry<Integer, IQuery> e : this.mapQueries.entrySet()) {
            qs = e.getValue();
            for (Map.Entry<String, String> f : this.mapTemplateParams.entrySet()) {
                qs.setText(qs.getText().replace(f.getKey(), f.getValue()));
            }
        }
    }
}