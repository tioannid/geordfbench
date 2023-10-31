package gr.uoa.di.rdf.Geographica3.runtime.querysets.partofworkload.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gr.uoa.di.rdf.Geographica3.runtime.executionspecs.IExecutionSpec;
import java.util.Map;
import java.util.Random;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.simple.IQuery;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public class MacroReverseGeocodingQS extends MacroQS {

    // --- Static members -----------------------------
    static final double xMin = 20.7861328125;
    static final double xMax = 22.9833984375;
    static final double yMin = 37.705078125;
    static final double yMax = 39.990234375;

    static {
        logger = Logger.getLogger(MacroReverseGeocodingQS.class.getSimpleName());
    }

    // --- Data members ------------------------------
    @JsonIgnore
    Random rnd = new Random(0);
    String templateParamName;

    // --- Constructors ------------------------------
    // 1. Partial constructor:
    //      The query list is deserialized from a JSON file placed
    //      in the resources folder or the classpath in general
    //      Queries are placed on a map property
    public MacroReverseGeocodingQS(IExecutionSpec executionSpec, String name, 
            String relativeBaseDir, boolean hasPredicateQueriesAlso,
            Map<Integer, IQuery> mapQueries,
            Map<String, String> mapUsefulNamespacePrefixes,
            String templateParamName) {
        super(executionSpec, name, relativeBaseDir, hasPredicateQueriesAlso,
                mapQueries, mapUsefulNamespacePrefixes);
        this.templateParamName = templateParamName;
    }

    public MacroReverseGeocodingQS() {
    }

    // --- Data Accessors ----------------------------
    public String getTemplateParamName() {
        return templateParamName;

    }

    public void setTemplateParamName(String templateParamName) {
        this.templateParamName = templateParamName;
    }

    // --- Methods ----------------------------    
    /**
     * Initialize for every iteration of the entire query set. This is used once
     * to initialize/translate the queries of the query set before a complete
     * run of all the queries takes place. It allows for implementing classes to
     * use ad-hoc logic to achieve this. In this class it creates a POINT with
     * random coordinates within a pre-specified range.
     *
     */
    @Override
    public void initializeQuerysetIteration() {
        super.initializeQuerysetIteration();

        double x = xMin + (xMax - xMin) * rnd.nextDouble();
        double y = yMin + (yMax - yMin) * rnd.nextDouble();
        String pointWKT = "\"POINT(" + x + " " + y + ")\"^^geo:wktLiteral";
        IQuery qs = null;
        // replace the template parameter with the pointWKT for all queries
        for (Map.Entry<Integer, IQuery> e : this.mapQueries.entrySet()) {
            qs = e.getValue();
            qs.setText(qs.getText().replace(this.templateParamName, pointWKT));
        }
    }
}
