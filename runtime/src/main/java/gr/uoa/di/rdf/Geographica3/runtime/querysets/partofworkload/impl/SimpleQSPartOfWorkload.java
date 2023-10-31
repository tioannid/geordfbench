package gr.uoa.di.rdf.Geographica3.runtime.querysets.partofworkload.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import gr.uoa.di.rdf.Geographica3.runtime.executionspecs.IExecutionSpec;
import gr.uoa.di.rdf.Geographica3.runtime.executionspecs.IExecutionSpec.Action;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.complex.IQuerySet;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.partofworkload.IQuerySetPartOfWorkload;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.simple.IQuery;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.simple.impl.SimpleQuery;
import java.io.StringWriter;
import java.util.TreeMap;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY,
        property = "classname")
public class SimpleQSPartOfWorkload implements IQuerySetPartOfWorkload {

    // --- Static members -----------------------------
    static Logger logger = Logger.getLogger(SimpleQSPartOfWorkload.class.getSimpleName());

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        SimpleQSPartOfWorkload.logger = logger;
    }

    // --- Data members ------------------------------
    String name;
    String relativeBaseDir;
    boolean hasPredicateQueriesAlso;
    Map<Integer, IQuery> mapQueries; // queries with no prefix header!!
    Map<String, String> mapUsefulNamespacePrefixes;
    IExecutionSpec executionSpec;
    /**
     * A volatile utility data structure which serves as a storage area for the
     * results of the first binding set of the last query executed!
     */
    @JsonIgnore
    Map<String, String> firstBindingSetValues;

    // --- Constructors ------------------------------
    // 1. Partial constructor:
    //      The query list is deserialized from a JSON file placed
    //      in the resources folder or the classpath in general
    //      Queries are placed on a map property
    public SimpleQSPartOfWorkload(IExecutionSpec executionSpec, String name,
            String relativeBaseDir, boolean hasPredicateQueriesAlso,
            Map<Integer, IQuery> mapQueries,
            Map<String, String> mapUsefulNamespacePrefixes) {
        this.name = name;
        this.relativeBaseDir = relativeBaseDir;
        this.hasPredicateQueriesAlso = hasPredicateQueriesAlso;
        this.mapQueries = mapQueries;
        this.mapUsefulNamespacePrefixes = mapUsefulNamespacePrefixes;
        this.executionSpec = executionSpec;
    }

    public SimpleQSPartOfWorkload() {
    }

    // --- Data Accessors ----------------------------
    public IExecutionSpec getExecutionSpec() {
        return executionSpec;
    }

    public void setExecutionSpec(IExecutionSpec executionSpec) {
        this.executionSpec = executionSpec;
    }

    @JsonIgnore
    @Override
    public Action getAction() {
        return executionSpec.getAction();
    }

    @JsonIgnore
    @Override
    public void setAction(Action action) {
        executionSpec.setAction(action);
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getRelativeBaseDir() {
        return relativeBaseDir;
    }

    public void setRelativeBaseDir(String relativeBaseDir) {
        this.relativeBaseDir = relativeBaseDir;
    }

    @Override
    public boolean isHasPredicateQueriesAlso() {
        return hasPredicateQueriesAlso;
    }

    public void setHasPredicateQueriesAlso(boolean hasPredicateQueriesAlso) {
        this.hasPredicateQueriesAlso = hasPredicateQueriesAlso;
    }

    @Override
    public Map<Integer, IQuery> getMapQueries() {
        return mapQueries;
    }

    public void setMapQueries(Map<Integer, IQuery> mapQueries) {
        this.mapQueries = mapQueries;
    }

    @Override
    public Map<String, String> getMapUsefulNamespacePrefixes() {
        return mapUsefulNamespacePrefixes;
    }

    public void setMapUsefulNamespacePrefixes(Map<String, String> mapUsefulNamespacePrefixes) {
        this.mapUsefulNamespacePrefixes = mapUsefulNamespacePrefixes;
    }

    public Map<String, String> getFirstBindingSetValues() {
        return firstBindingSetValues;
    }

    public void setFirstBindingSetValues(Map<String, String> firstBindingSetValues) {
        this.firstBindingSetValues = firstBindingSetValues;
    }

    // --- Methods ----------------------------    
    @JsonIgnore
    @Override
    public int getQueriesNum() {
        return this.mapQueries.size();
    }

    /**
     * Gets a query, searching by position in the query map.
     *
     * @param position the query position in the query map
     * @return the query in the provided position
     */
    @JsonIgnore
    @Override
    public IQuery getQuery(int position) {
        if (this.mapQueries != null) {
            return this.mapQueries.get(position);
        } else {
            throw new RuntimeException("Queryset map is null!");
        }
    }

//    // prints all numbered query labels for the query set
//    public void printQnnQueryLabels() {
//        String msg = "";
//        for (Map.Entry<Integer, IQuery> e : this.mapQueries.entrySet()) {
//            msg += (e.getValue().getQnnLabel(e.getKey()) + "\n");
//        }
//        logger.info(msg);
//    }
    /**
     * Adds the entries of the provided map namespace prefixes to the underlying
     * map of useful namespace prefixes. Can be used to augment the query set
     * namespace prefixes with dataset namespace prefixes
     *
     * @param mapUsefulNamespacePrefixes A {@link Map} of useful namespace
     * prefixes to add
     */
    @Override
    public void addMapUsefulNamespacePrefixes(Map<String, String> mapUsefulNamespacePrefixes) {
        if (this.mapUsefulNamespacePrefixes == null) {
            this.mapUsefulNamespacePrefixes = new HashMap<>();
        }
        this.mapUsefulNamespacePrefixes.putAll(mapUsefulNamespacePrefixes);
    }

    @JsonIgnore
    @Override
    public String prettyPrint() {
        // TODO: Think of something meaningful
        return this.toString();
    }

    /**
     * Filters the queries map either by preserving or removing the queries with
     * position listed in the int[].
     *
     * @param qryPositions <tt>int[]</tt> with query positions
     * @param filterAction IQuerySet.FilterAction.<tt>EXCLUDE</tt> to exclude
     * the query positions provided or IQuerySet.FilterAction.<tt>INCLUDE</tt>
     * to preserve the query positions provided and exclude all others.
     */
    @Override
    public void filter(int[] qryPositions, FilterAction filterAction) {
        boolean match;
        int pos;
        int i;
        if (filterAction.equals(IQuerySet.FilterAction.EXCLUDE)) {
            for (i = 0; i < qryPositions.length; i++) {
                this.mapQueries.remove(qryPositions[i]);
            }
        } else {
            // INCLUDE that is PRESERVE the query positions provided
            Map<Integer, IQuery> newQueryMap = new HashMap<>();
            for (Map.Entry<Integer, IQuery> e : this.mapQueries.entrySet()) {
                pos = e.getKey();
                match = false;
                i = 0;
                while ((i < qryPositions.length) & !match) {
                    match = match || (qryPositions[i] == pos);
                    i++;
                }
                if (match) {
                    // map entry's position not found in INCLUDE position arrah
                    newQueryMap.put(e.getKey(), e.getValue());
                }
            }
            this.setMapQueries(newQueryMap);
        }
    }

    @JsonIgnore
    @Override
    public String getNamespacePrefixHeader() {
        String prefixes = "";
        if (this.mapUsefulNamespacePrefixes != null) {
            for (String nsprefix : new TreeMap<>(this.mapUsefulNamespacePrefixes).keySet()) {
                prefixes = prefixes + "PREFIX " + nsprefix + ": " + this.mapUsefulNamespacePrefixes.get(nsprefix) + " \n";
            }
        }
        return prefixes;
    }

    /**
     * Serialize the geographica queryset to a JSON string
     *
     * @return the serialized JSON string
     */
    public String serializeToJSON() {
        // create the mapper
        ObjectMapper mapper = new ObjectMapper();

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // enable pretty printing
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // serialize the object
        StringWriter strwrt = new StringWriter();
        try {
            mapper.writeValue(strwrt, this);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
        return strwrt.toString();
    }

    /**
     * Serialize the geographica queryset to a JSON file
     *
     * @param serObjFile File with the serialized object
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    public void serializeToJSON(File serObjFile) throws JsonGenerationException, JsonMappingException, IOException {
        // create the mapper
        ObjectMapper mapper = new ObjectMapper();
        // do not serialize null fields
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // enable pretty printing
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // serialize the object
        mapper.writeValue(serObjFile, this);
    }

    /**
     * Initialize for every iteration of the entire query set. This is used once
     * to initialize/translate the queries of the query set before a complete
     * run of all the queries takes place. It allows for implementing classes to
     * use ad-hoc logic to achieve this..
     *
     * SimpleQS uses static text queries and therefore does not need to do
     * anything.
     */
    @Override
    public void initializeQuerysetIteration() {
    }

    @Override
    public void initializeAfterDeserialization() {
    }

    public void setExpectedResults(SimpleQuery.QueryAccuracyValidation queryAccuracyValidation) {
        for (Integer qryNo : mapQueries.keySet()) {
            mapQueries.get(qryNo).setExpectedResults(queryAccuracyValidation.getValue());
        }
    }

    @Override
    public String toString() {
        String msg;
        msg = "{" + this.getClass().getSimpleName() + ": "
                + name + ", "
                + relativeBaseDir + ","
                + String.valueOf(hasPredicateQueriesAlso) + ", "
                + executionSpec.toString();
        msg = msg + "}";
        return msg;
    }

}
