/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
package gr.uoa.di.rdf.geordfbench.runtime.querysets.simple.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import gr.uoa.di.rdf.geordfbench.runtime.querysets.simple.IQuery;
import gr.uoa.di.rdf.geordfbench.runtime.resultscollector.impl.QueryRepResults;

public class SimpleQuery implements IQuery {

    public enum QueryAccuracyValidation {
        TEMPLATE_DEPENDENT("TEMPLATE_DEPENDENT", -2),
        DATASET_DEPENDENT("DATASET_DEPENDENT", -1),
        INDEPENDENT("INDEPENDENT", 0);

        // --- Data members ------------------------------
        private final String label;
        private final int value;

        // --- Constructor -----------------------------------
        QueryAccuracyValidation(String label, int value) {
            this.label = label;
            this.value = value;
        }

        // --- Data Accessors -----------------------------------
        public String getLabel() {
            return label;
        }

        public int getValue() {
            return value;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    // --- Data members ------------------------------
    protected String label;
    protected String text;
    protected boolean usePredicate; // text uses geospatial predicates
    protected long expectedResults; // expected no of results, for validation
    protected transient QueryAccuracyValidation accuracyValidation; // denotes if query's accuracy can be validated

    // --- Constructors ------------------------------
    // label should not have a prefix with Q<number>
    // This will be added automatically by the AbstractGeographicaQuerySet
    public SimpleQuery(String label, String query, boolean usePredicate) {
        this.text = query;
        this.label = label;
        this.usePredicate = usePredicate;
        this.setExpectedResults(QueryAccuracyValidation.DATASET_DEPENDENT.getValue());
    }

    public SimpleQuery(String label, String query, boolean usePredicate,
            long expectedResults) {
        this.text = query;
        this.label = label;
        this.usePredicate = usePredicate;
        this.setExpectedResults(expectedResults);
    }

    public SimpleQuery(SimpleQuery qs) {
        this.label = qs.label;
        this.text = qs.text;
        this.usePredicate = qs.usePredicate;
        this.setExpectedResults(qs.expectedResults);
    }

    public SimpleQuery(SimpleQuery qs, String namespacePrefixHeader) {
        this.label = qs.label;
        this.text = namespacePrefixHeader + "\n" + qs.text;
        this.usePredicate = qs.usePredicate;
        this.setExpectedResults(qs.expectedResults);
    }

    public SimpleQuery() {
    }

    // --- Data Accessors ----------------------------
    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public boolean isUsePredicate() {
        return usePredicate;
    }

    @Override
    public void setUsePredicate(boolean usePredicate) {
        this.usePredicate = usePredicate;
    }

    @Override
    public long getExpectedResults() {
        return expectedResults;
    }

    @Override
    public void setExpectedResults(long expectedResults) {
        this.expectedResults = expectedResults;
        if (expectedResults == -1) {
            this.accuracyValidation = QueryAccuracyValidation.DATASET_DEPENDENT;
        } else if (expectedResults == -2) {
            this.accuracyValidation = QueryAccuracyValidation.TEMPLATE_DEPENDENT;
        } else {
            this.accuracyValidation = QueryAccuracyValidation.INDEPENDENT;
        }
    }

    // --- Methods -----------------------------------
    /**
     * Returns a deep copy clone of a {@link SimpleQuery} instance
     *
     * @return a {@link IQuery} instance
     */
    @JsonIgnore
    @Override
    public IQuery clone() {
        return new SimpleQuery(this);
    }

//    @JsonIgnore
//    @Override
//    public String getQnnLabel(int indexNo) {
//        String num = String.format("%1$02d", indexNo);
//        return ("Q" + num + (this.usePredicate ? "p" : "") + "-" + label);
//    }
    // a file read method where from a JSON file we can read 
    // a List<QueryStruct> 
    public static List<SimpleQuery> readJSONQuerySet(String querySetJSONFile) throws IOException {
        List<SimpleQuery> qsl = null;
        // TODO: read with JSON reader 
        InputStream is = SimpleQuery.class.getResourceAsStream("/" + querySetJSONFile);
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        ObjectMapper objectMapper = new ObjectMapper();
        qsl = objectMapper.readValue(in, new TypeReference<List<SimpleQuery>>() {
        });
        return qsl;
    }

    @JsonIgnore
    public IQuery getNamespaceHeaderedQuery(String namespacePrefixHeader) {
        return new SimpleQuery(this, namespacePrefixHeader);
    }

    @JsonIgnore
    @Override
    public String toString() {
        return "[ label : " + this.label + "\n"
                + "  usesPredicate : " + this.usePredicate + "\n"
                + "  text  : \n" + this.text + "\n"
                + "  expectedResults : " + this.expectedResults + "\n]";
    }

    /**
     * if accuracy validation is possible, it checks the provided number against
     * the query's expected results number
     *
     * @param results the number of results returned by a query run
     * @return <tt>true</tt> if the provided number equals the query's expected
     * results number
     */
    @Override
    public boolean isResultAccurate(long results) {
        boolean test = false;
        if (accuracyValidation.equals(QueryAccuracyValidation.INDEPENDENT)) {
            test = (this.expectedResults == results);
        }
        return test;
    }

    /**
     * if accuracy validation is possible, and result has no exception it checks 
     * the provided number against the query's expected results number.
     *
     * @param queryRepResult Query repetition result
     * @return <tt>true</tt> if the query repetition result has number of results 
     * equals the query's expected results number and the result has no exception.
     */
    @Override
    public boolean isResultAccurate(QueryRepResults.QueryRepResult queryRepResult) {
        boolean resultHasException = queryRepResult.getResException().hasException();
        if (resultHasException) { // when result is not valid skip validation
            return false;
        }
        boolean isAccurate = false;
        if (accuracyValidation.equals(QueryAccuracyValidation.INDEPENDENT)) {
            isAccurate = (this.expectedResults == queryRepResult.getNoResults());
        }
        return isAccurate;
    }
}
