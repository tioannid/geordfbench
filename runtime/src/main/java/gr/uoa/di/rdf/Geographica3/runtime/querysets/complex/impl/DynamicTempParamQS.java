/**
 * @author GeoRDFBench Creator <GeoRDFBench@Univ>
 */
package gr.uoa.di.rdf.Geographica3.runtime.querysets.complex.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.simple.IQuery;

/**
 * A Geographica query set implementation that allows dynamically creating the
 * final query set by specifying: 
 * - list of query templates to use
 * - list of template parameters to use 
 * - the declarative matrix of template parameter values to use for each query
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public class DynamicTempParamQS extends SimpleQS {

    // --- Static block/clause -----------------------
    static {
        logger = Logger.getLogger(DynamicTempParamQS.class.getSimpleName());
    }

    // --- Data members ------------------------------
    Map<String, String> mapQueryTemplates; // template name -> template query 
    Map<String, String> mapLiteralValues; // named literal values
    List<String> templateDynamicParamNameList;  // list of template param names
    // "qryNo"##"templateParamName" ==> "templateParamValue"
    DynamicQryTempParamMatrix templateParamValueMatrix;
    boolean isTranslated;   // translation should be done once!

    // --- Constructors ------------------------------
    // 1. Partial constructor:
    //      The query list is deserialized from a JSON file placed
    //      in the resources folder or the classpath in general
    //      Queries are placed on a map property
    //      then only one set of queries (spatial predicate or functions) is kept
    //      and static template params are replaced
    public DynamicTempParamQS(String name, String relativeBaseDir,
            boolean hasPredicateQueriesAlso,
            Map<Integer, IQuery> mapQueries,
            Map<String, String> mapUsefulNamespacePrefixes,
            Map<String, String> mapQueryTemplates,
            Map<String, String> mapLiteralValues,
            List<String> templateDynamicParamNameList) {
        super(name, relativeBaseDir, hasPredicateQueriesAlso,
                mapQueries, mapUsefulNamespacePrefixes);
        this.mapQueryTemplates = mapQueryTemplates;
        this.mapLiteralValues = mapLiteralValues;
        this.templateDynamicParamNameList = templateDynamicParamNameList;
        // sanity checks
        boolean isSane = (mapQueryTemplates != null)
                && (templateDynamicParamNameList != null);
        if (!isSane) {
            throw new RuntimeException(this.getClass().getSimpleName()
                    + " does not have sane values for initialization");
        }
        this.templateParamValueMatrix = new DynamicQryTempParamMatrix(this);
        this.isTranslated = false;
    }

    // 1. Partial constructor:
    //      The query list is deserialized from a JSON file placed
    //      in the resources folder or the classpath in general
    //      Queries are placed on a map property
    //      then only one set of queries (spatial predicate or functions) is kept
    //      and static template params are replaced
    /**
     * Used mainly to deserializeFromJSON a
     * GeographicaDynamicTemplateParamQuerySet from a JSON file, where queries
     * can be translated or not yet. This allows the end-user to modify the JSON
     * file and produce variations of the dynamic queryset.
     *
     * @param name
     * @param relativeBaseDir
     * @param hasPredicateQueriesAlso
     * @param mapQueries
     * @param mapUsefulNamespacePrefixes
     * @param mapQueryTemplates
     * @param mapLiteralValues
     * @param templateDynamicParamNameList
     * @param templateParamValueMatrix
     * @param isTranslated
     */
    public DynamicTempParamQS(String name, String relativeBaseDir,
            boolean hasPredicateQueriesAlso,
            Map<Integer, IQuery> mapQueries,
            Map<String, String> mapUsefulNamespacePrefixes,
            Map<String, String> mapQueryTemplates,
            Map<String, String> mapLiteralValues,
            List<String> templateDynamicParamNameList,
            DynamicQryTempParamMatrix templateParamValueMatrix,
            boolean isTranslated) {
        super(name, relativeBaseDir, hasPredicateQueriesAlso, mapQueries,
                mapUsefulNamespacePrefixes);
        this.mapQueryTemplates = mapQueryTemplates;
        this.mapLiteralValues = mapLiteralValues;
        this.templateDynamicParamNameList = templateDynamicParamNameList;
        templateParamValueMatrix.setParent(this);
        this.templateParamValueMatrix = templateParamValueMatrix;
        this.isTranslated = isTranslated;
        this.translateAllQueries();
    }

    public DynamicTempParamQS() {
    }

    // --- Data Accessors ----------------------------
    public Map<String, String> getMapQueryTemplates() {
        return mapQueryTemplates;
    }

    public void setMapQueryTemplates(Map<String, String> mapQueryTemplates) {
        this.mapQueryTemplates = mapQueryTemplates;
    }

    public Map<String, String> getMapLiteralValues() {
        return mapLiteralValues;
    }

    public void setMapLiteralValues(Map<String, String> mapLiteralValues) {
        this.mapLiteralValues = mapLiteralValues;
    }

    public List<String> getTemplateDynamicParamNameList() {
        return templateDynamicParamNameList;
    }

    public void setTemplateDynamicParamNameList(List<String> templateDynamicParamNameList) {
        this.templateDynamicParamNameList = templateDynamicParamNameList;
    }

    public DynamicQryTempParamMatrix getTemplateParamValueMatrix() {
        return templateParamValueMatrix;
    }

    public void setTemplateParamValueMatrix(DynamicQryTempParamMatrix templateParamValueMatrix) {
        templateParamValueMatrix.setParent(this);
        this.templateParamValueMatrix = templateParamValueMatrix;
    }

    public boolean isIsTranslated() {
        return isTranslated;
    }

    public void setIsTranslated(boolean isTranslated) {
        this.isTranslated = isTranslated;
    }

    // --- Methods -----------------------------------
    /**
     * Uses the {@link templateParamValueMatrix} to translate the query text of
     * all queries by replacing template parameters with corresponding values
     *
     */
    @JsonIgnore
    public void translateAllQueries() {
        if (!isTranslated) {
            IQuery q;
            int qryNo;
            for (Entry<Integer, IQuery> e : this.mapQueries.entrySet()) {
                qryNo = e.getKey();
                q = this.templateParamValueMatrix.getTranslatedQuery(qryNo);
                this.mapQueries.put(qryNo, q);
            }
            isTranslated = true;
        }
    }

    @Override
    public IQuery getQuery(int position) {
        translateAllQueries();
        return super.getQuery(position);
    }

    @Override
    public Map<Integer, IQuery> getMapQueries() {
        translateAllQueries();
        return super.getMapQueries();
    }
}