/**
 * @author GeoRDFBench Creator <GeoRDFBench@Univ>
 */
package gr.uoa.di.rdf.Geographica3.runtime.querysets.partofworkload.impl;

import gr.uoa.di.rdf.Geographica3.runtime.executionspecs.IExecutionSpec;
import gr.uoa.di.rdf.Geographica3.runtime.executionspecs.impl.SimpleES;
import java.util.Map;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.simple.IQuery;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.simple.impl.SimpleQuery;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.util.QuerySetPartOfWorkloadUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.util.SyntheticQueriesSet;

// StaticTempParamQS relates a GeographicaDataSet dataset
// and an AbstractGeographicaSystem<C> _sys. By default the mapUsefulPrefixes
// is initialized with the union of the mapUsefulPrefixes of dataset and _sys.
public class StaticTempParamQS extends SimpleQSPartOfWorkload {

    // --- Static members -----------------------------
    public static String SCALABILITY_EUROPE_POLYGON_FILE = "givenPolygonCrossesEurope.txt";
    public static String WKT_LITERAL = "<http://www.opengis.net/ont/geosparql#wktLiteral>";

    // --- Static block/clause -----------------------
    static {
        logger = Logger.getLogger(StaticTempParamQS.class.getSimpleName());
    }

    /**
     * Creates the Scalability (spatial function) geographica queryset
     *
     * @return the Scalability (spatial function) geographica queryset
     */
    public static StaticTempParamQS newScalabilityFuncQS() {
        // read fixed Polygon from external file which might be used in spatial selection queries
        InputStream is = QuerySetPartOfWorkloadUtil.class.getResourceAsStream("/" + SCALABILITY_EUROPE_POLYGON_FILE);
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String givenPolygon = "";
        try {
            givenPolygon = in.readLine();
            givenPolygon = "\"" + givenPolygon + "\"^^" + WKT_LITERAL;
            in.close();
            in = null;
            is.close();
            is = null;
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
        // 2. initialize the map of useful general RDF related prefixes
        Map<String, String> mapTemplateParams = new HashMap<>();
        mapTemplateParams.put("FUNCTION", "sfIntersects");
        mapTemplateParams.put("GIVEN_SPATIAL_LITERAL", givenPolygon);
        Map<String, String> mapUsefulNamespacePrefixes = new HashMap<>();
        // populate Graph prefixes map
        Map<String, String> mapLiteralValues = new HashMap<>();
        Map<Integer, IQuery> mapQry = new HashMap<>();
        mapQry.put(0, new SimpleQuery("SC1_Geometries_Intersects_GivenPolygon",
                "SELECT ?s1 ?o1 WHERE { \n ?s1 geo:asWKT ?o1 . \n  FILTER(geof:FUNCTION(?o1, GIVEN_SPATIAL_LITERAL)). \n} \n",
                false));
        mapQry.put(1, new SimpleQuery("SC2_Intensive_Geometries_Intersect_Geometries",
                "SELECT ?s1 ?s2 \nWHERE { \n ?s1 geo:hasGeometry [ geo:asWKT ?o1 ] ;\n    lgo:has_code \"1001\"^^xsd:integer . \n ?s2 geo:hasGeometry [ geo:asWKT ?o2 ] ;\n    lgo:has_code ?code2 .  \n FILTER(?code2>5000 && ?code2<6000 && ?code2 != 5260) .\n FILTER(geof:FUNCTION(?o1, ?o2)). \n} \n",
                false));
        mapQry.put(2, new SimpleQuery("SC3_Relaxed_Geometries_Intersect_Geometries",
                "SELECT ?s1 ?s2\nWHERE {\n ?s1 geo:hasGeometry [ geo:asWKT ?o1 ] ;\n    lgo:has_code \"1001\"^^xsd:integer .\n ?s2 geo:hasGeometry [ geo:asWKT ?o2 ] ;\n    lgo:has_code ?code2 .\n FILTER(?code2 IN (5622, 5601, 5641, 5621, 5661)) .\n FILTER(geof:FUNCTION(?o1, ?o2)).\n} \n",
                false));
        // create the Execution specification
        return new StaticTempParamQS(SimpleES.newScalabilityES(), "scalabilityFunc", "", false, mapQry,
                mapTemplateParams, mapUsefulNamespacePrefixes,
                mapLiteralValues);
    }

    /**
     * Creates the Scalability (spatial predicate) geographica queryset
     *
     * @return the Scalability (spatial predicate) geographica queryset
     */
    public static StaticTempParamQS newScalabilityPredQS() {
        // read fixed Polygon from external file which might be used in spatial selection queries
        InputStream is = QuerySetPartOfWorkloadUtil.class.getResourceAsStream("/" + SCALABILITY_EUROPE_POLYGON_FILE);
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String givenPolygon = "";
        try {
            givenPolygon = in.readLine();
            givenPolygon = "\"" + givenPolygon + "\"^^" + WKT_LITERAL;
            in.close();
            in = null;
            is.close();
            is = null;
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
        // 2. initialize the map of useful general RDF related prefixes
        Map<String, String> mapTemplateParams = new HashMap<>();
        mapTemplateParams.put("FUNCTION", "sfIntersects");
        mapTemplateParams.put("GIVEN_SPATIAL_LITERAL", givenPolygon);
        Map<String, String> mapUsefulNamespacePrefixes = new HashMap<>();
        // populate Graph prefixes map
        Map<String, String> mapLiteralValues = new HashMap<>();
        Map<Integer, IQuery> mapQry = new HashMap<>();
        mapQry.put(0, new SimpleQuery("SC1_Geometries_Intersects_GivenPolygon",
                "SELECT ?s1 ?o1 WHERE { \n ?s1 geo:asWKT ?o1 . \n ?s1 geo:FUNCTION GIVEN_SPATIAL_LITERAL . \n} \n",
                true));
        mapQry.put(1, new SimpleQuery("SC2_Intensive_Geometries_Intersect_Geometries",
                "SELECT ?s1 ?s2 \nWHERE { \n ?s1 geo:hasGeometry ?g1 ;\n    lgo:has_code \"1001\"^^xsd:integer . \n ?s2 geo:hasGeometry ?g2 ;\n    lgo:has_code ?code2 .  \n ?g1 geo:FUNCTION ?g2 . \n FILTER(?code2>5000 && ?code2<6000 && ?code2 != 5260) .\n} \n",
                true));
        mapQry.put(2, new SimpleQuery("SC3_Relaxed_Geometries_Intersect_Geometries",
                "SELECT ?s1 ?s2\nWHERE {\n ?s1 geo:hasGeometry ?g1 ;\n    lgo:has_code \"1001\"^^xsd:integer .\n ?s2 geo:hasGeometry ?g2 ;\n    lgo:has_code ?code2 .\n ?g1 geo:FUNCTION ?g2 . \n FILTER(?code2 IN (5622, 5601, 5641, 5621, 5661)) .\n} \n",
                true));
        // create the Execution specification
        return new StaticTempParamQS(SimpleES.newScalabilityES(), "scalabilityPred", "", true, mapQry,
                mapTemplateParams, mapUsefulNamespacePrefixes,
                mapLiteralValues);
    }
    
    /**
     * Creates the Synthetic geographica queryset
     *
     * @param N synthetic dataset scale factor
     * @return the Synthetic N geographica queryset
     */
    public static StaticTempParamQS newSyntheticQS(int N) {
        SyntheticQueriesSet synthqs = null;
        try {
            synthqs = new SyntheticQueriesSet(null, N);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
        Map<Integer, IQuery> mapQry = new HashMap<>();
        for (int i = 0; i < synthqs.getQueriesN(); i++) {
            // since queries returned have the prefix headers, we
            // remove them by using SELECT as the delimiter
            mapQry.put(i, new SimpleQuery(
                    synthqs.getQuery(i, 0).getLabel(),
                    "SELECT" + synthqs.getQuery(i, 0).getQuery().split("SELECT")[1],
                    false));
        }
        Map<String, String> mapTemplateParams = new HashMap<>();
        Map<String, String> mapUsefulNamespacePrefixes = new HashMap<>();
        // populate Graph prefixes map
        Map<String, String> mapLiteralValues = new HashMap<>();
        return new StaticTempParamQS(SimpleES.newMicroES(),
                "synthetic", "", false, mapQry,
                mapTemplateParams, mapUsefulNamespacePrefixes,
                mapLiteralValues);
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
    public StaticTempParamQS(IExecutionSpec executionSpec, String name,
            String relativeBaseDir, boolean hasPredicateQueriesAlso,
            Map<Integer, IQuery> mapQueries,
            Map<String, String> mapTemplateParams,
            Map<String, String> mapUsefulNamespacePrefixes,
            Map<String, String> mapGraphPrefixes) {
        super(executionSpec, name, relativeBaseDir, hasPredicateQueriesAlso,
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
