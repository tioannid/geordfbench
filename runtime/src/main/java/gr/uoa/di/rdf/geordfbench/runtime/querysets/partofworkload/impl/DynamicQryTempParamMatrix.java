package gr.uoa.di.rdf.geordfbench.runtime.querysets.partofworkload.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gr.uoa.di.rdf.geordfbench.runtime.querysets.simple.impl.SimpleQuery;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import gr.uoa.di.rdf.geordfbench.runtime.querysets.simple.IQuery;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public class DynamicQryTempParamMatrix {

    // --- Data members ------------------------------
    transient DynamicTempParamQS parent;
    // "qryNo"##"templateParamName" ==> "templateParamValue"
    Map<String, String> mapTemplateParamValues;

    // --- Constructors ------------------------------
    // 1. Partial constructor:
    //    The remaining structure is created by setter operator
    public DynamicQryTempParamMatrix(DynamicTempParamQS parent) {
        this.parent = parent;
        mapTemplateParamValues = new HashMap<>();
    }

    public DynamicQryTempParamMatrix() {
    }

    // --- Data Accessors ----------------------------
    @JsonIgnore
    public DynamicTempParamQS getParent() {
        return parent;
    }

    public void setParent(DynamicTempParamQS parent) {
        this.parent = parent;
    }

    public Map<String, String> getMapTemplateParamValues() {
        return mapTemplateParamValues;
    }

    public void setMapTemplateParamValues(Map<String, String> mapTemplateParamValues) {
        this.mapTemplateParamValues = mapTemplateParamValues;
    }

    // --- Methods -----------------------------------
    /**
     * Adds a mapping for a template param value replacement for a specific
     * query position.
     *
     * @param qryNo position of the query this mapping is related to
     * @param templateParamName a String representing the name of the template
     * param in the query text
     * @param literalName a String representing the name of the literal to use
     * in order to replace the template param in the query text
     */
    @JsonIgnore
    public void put(int qryNo, String templateParamName, String literalName) {
        // check if qryNo belongs in [qryNoMin, qryNoMax]
        if ((qryNo < 0) || (qryNo > parent.getQueriesNum() - 1)) {
            throw new RuntimeException("Query number is not in the "
                    + "[" + 0 + ", " + (parent.getQueriesNum() - 1) + "] range!");
        }
        // check if templateParamName exists in the templateParamNameList
        if (!parent.templateDynamicParamNameList.contains(templateParamName)) {
            throw new RuntimeException("Template param name " + templateParamName
                    + " is not in the template dynamic param list!");
        }
        // check if literalName exists in the mapLiteralValues
        if (!parent.mapLiteralValues.containsKey(literalName)) {
            throw new RuntimeException("Literal name " + literalName
                    + " is not in the literal values map!");
        }
        // add the new map entry
        // "qryNo"##"templateParamName" ==> "templateParamValue"
        mapTemplateParamValues.put(qryNo + "##" + templateParamName, literalName);
    }

    /**
     * Get a translated copy of the query in the given position.
     *
     * @param qryNo position of the query that will be used for the translation
     * @return a {@link IQuery} representing the translated copy of the query in
     * the given position
     */
    @JsonIgnore
    public IQuery getTranslatedQuery(int qryNo) {
        // retrieve the template name for the query
        String queryTemplateName = parent.mapQueries.get(qryNo).getText();
        // retrieve the template text
        String queryTemplateText = this.parent.mapQueryTemplates.get(queryTemplateName);
        String templateParamName, templateParamValue;
        int eQryNo;
        // use all matching map records to replace template text params with 
        // literal values
        for (Entry<String, String> e : mapTemplateParamValues.entrySet()) {
            eQryNo = Integer.parseInt(e.getKey().split("##")[0]);
            if (eQryNo == qryNo) { // do the string replacement
                templateParamName = e.getKey().split("##")[1];
                templateParamValue = e.getValue();
                queryTemplateText = queryTemplateText.replaceAll(templateParamName, this.parent.mapLiteralValues.get(templateParamValue));
            }
        }
        SimpleQuery newQry = new SimpleQuery((SimpleQuery) parent.mapQueries.get(qryNo));
        newQry.setText(queryTemplateText);
        return newQry;
    }
}
