package gr.uoa.di.rdf.Geographica3.runtime.querysets.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import gr.uoa.di.rdf.Geographica2.queries.SyntheticQueriesSet;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.complex.impl.DynamicTempParamQS;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.complex.impl.StaticTempParamQS;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.complex.impl.MacroMapSearchQS;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.complex.impl.MacroReverseGeocodingQS;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.complex.impl.SimpleQS;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.simple.impl.SimpleQuery;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.complex.IQuerySet;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.complex.impl.MacroComputeStatisticsQS;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.complex.impl.MacroGeocodingQS;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.complex.impl.MacroRapidMappingQS;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.simple.IQuery;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public class QuerySetUtil {

    // --- Static members -----------------------------
    static org.apache.log4j.Logger logger
            = org.apache.log4j.Logger.getLogger(QuerySetUtil.class.getSimpleName());
    public static final String QUERYSETJSONDEFS_DIR = "../json_defs2/querysets/";
    public static final String SCALABILITY_FUNC_JSONDEF_FILE = QUERYSETJSONDEFS_DIR + "scalabilityFuncQSoriginal.json";
    public static final String SCALABILITY_PRED_JSONDEF_FILE = QUERYSETJSONDEFS_DIR + "scalabilityPredQSoriginal.json";
    public static String SCALABILITY_EUROPE_POLYGON_FILE = "givenPolygonCrossesEurope.txt";
    public static String WKT_LITERAL = "<http://www.opengis.net/ont/geosparql#wktLiteral>";
    public static final String SYNTHETICJSONDEF_FILE = QUERYSETJSONDEFS_DIR + "syntheticQSoriginal.json";
    public static final String RWMICROJSONDEF_FILE = QUERYSETJSONDEFS_DIR + "rwmicroQSoriginal.json";
    public static final String RWMICRO_SELECTIONS_POLYGON_FILE = "givenPolygon.txt";
    public static final String RWMICRO_SELECTIONS_LINES_FILE = "givenLine.txt";
    public static final String RWMACROREVERSEGEOCODING_JSONDEF_FILE = QUERYSETJSONDEFS_DIR + "rwmacroreversegeocodingQSoriginal.json";
    public static final String RWMACROMAPSEARCH_JSONDEF_FILE = QUERYSETJSONDEFS_DIR + "rwmacromapsearchQSoriginal.json";
    public static final String RWMACRORAPIDMAPPING_JSONDEF_FILE = QUERYSETJSONDEFS_DIR + "rwmacrorapidmappingQSoriginal.json";
    public static final String RWMACROCOMPUTESTATISTICS_JSONDEF_FILE = QUERYSETJSONDEFS_DIR + "rwmacrocomputestatisticsQSoriginal.json";
    public static final String RWMACROGEOCODING_JSONDEF_FILE = QUERYSETJSONDEFS_DIR + "censusmacrogeocodingQSoriginal.json";
    public static final String LUBM_1_0_JSONDEF_FILE = QUERYSETJSONDEFS_DIR + "LUBM_1_0_WLoriginal_GOLD_STANDARD.json";

    // --- Methods -----------------------------------
    // A) -- Methods that can re-create the JSON definition files
    //       Geographica3 querysets
    /**
     * Creates the JSON definition file for the Scalability (spatial function)
     * geographica queryset in
     * Geographica/runtime/src/main/resources/json_defs/querysets/scalabilityQSoriginal.json
     *
     */
    public static void createScalabilityFuncQS_OriginalJSONDefFile() {
        // read fixed Polygon from external file which might be used in spatial selection queries
        InputStream is = QuerySetUtil.class.getResourceAsStream("/" + SCALABILITY_EUROPE_POLYGON_FILE);
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
        StaticTempParamQS scalabilityQS
                = new StaticTempParamQS("scalabilityFunc", "", false, mapQry,
                        mapTemplateParams, mapUsefulNamespacePrefixes,
                        mapLiteralValues);
        try {
            ObjectMapper om = new ObjectMapper();
            om.enable(SerializationFeature.INDENT_OUTPUT);
            om.writeValue(new File(SCALABILITY_FUNC_JSONDEF_FILE), scalabilityQS);
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates the JSON definition file for the Scalability (spatial predicate)
     * geographica queryset in
     * Geographica/runtime/src/main/resources/json_defs/querysets/scalabilityQSoriginal.json
     *
     */
    public static void createScalabilityPredQS_OriginalJSONDefFile() {
        // read fixed Polygon from external file which might be used in spatial selection queries
        InputStream is = QuerySetUtil.class.getResourceAsStream("/" + SCALABILITY_EUROPE_POLYGON_FILE);
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
        StaticTempParamQS scalabilityQS
                = new StaticTempParamQS("scalabilityPred", "", true, mapQry,
                        mapTemplateParams, mapUsefulNamespacePrefixes,
                        mapLiteralValues);
        try {
            // scalabilityQS.serializeToJSON(new File(SCALABILITYJSONDEF_FILE));
            ObjectMapper om = new ObjectMapper();
            om.enable(SerializationFeature.INDENT_OUTPUT);
//            om.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, JsonTypeInfo.As.PROPERTY);
//            om.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
            om.writeValue(new File(SCALABILITY_PRED_JSONDEF_FILE), scalabilityQS);
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates the JSON definition file for the Synthetic geographica queryset
     * in
     * Geographica/runtime/src/main/resources/json_defs/querysets/syntheticQSoriginal.json
     *
     */
    public static void createSyntheticQS_OriginalJSONDefFile(int N) {
        SyntheticQueriesSet synthqs = null;
        try {
            synthqs = new SyntheticQueriesSet(null, N);
        } catch (IOException ex) {
            Logger.getLogger(QuerySetUtil.class.getName()).log(Level.SEVERE, null, ex);
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
        StaticTempParamQS syntheticQS
                = new StaticTempParamQS("synthetic", "", false, mapQry,
                        mapTemplateParams, mapUsefulNamespacePrefixes,
                        mapLiteralValues);
        try {
            syntheticQS.serializeToJSON(new File(SYNTHETICJSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    public static void createRWMicroQS_OriginalJSONDefFile() {
        // read fixed Polygon from external file which might be used in spatial selection queries
        String givenPolygon = "";
        InputStream is = QuerySetUtil.class.getResourceAsStream("/" + RWMICRO_SELECTIONS_POLYGON_FILE);
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
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
        // read fixed Line from external file which might be used in spatial selection queries
        String givenLine = "", givenLine2 = "", givenLine3 = "";
        is = QuerySetUtil.class.getResourceAsStream("/" + RWMICRO_SELECTIONS_LINES_FILE);
        in = new BufferedReader(new InputStreamReader(is));
        try {
            // <http://linkedgeodata.org/geometry/way168092715>
            givenLine = in.readLine();
            givenLine = "\"" + givenLine + "\"^^" + WKT_LITERAL;
            // <http://linkedgeodata.org/geometry/way31642973>
            givenLine2 = in.readLine();
            givenLine2 = "\"" + givenLine2 + "\"^^" + WKT_LITERAL;
            // <http://linkedgeodata.org/geometry/way45476887>
            givenLine3 = in.readLine();
            givenLine3 = "\"" + givenLine3 + "\"^^" + WKT_LITERAL;
            in.close();
            in = null;
            is.close();
            is = null;
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
        // Some constant literals to use
        String givenPoint = "\"POINT(23.71622 37.97945)\"^^" + WKT_LITERAL;
        String givenRadius = "3000";

        // populate Literal Values map
        Map<String, String> mapLiteralValues = new HashMap<>();
        mapLiteralValues.put("LL_givenPolygon", givenPolygon);
        mapLiteralValues.put("LL_givenLine", givenLine);
        mapLiteralValues.put("LL_givenLine2", givenLine2);
        mapLiteralValues.put("LL_givenLine3", givenLine3);
        // populate Useful namespace prefixes map
        Map<String, String> mapUsefulNamespacePrefixes = new HashMap<>();
        mapUsefulNamespacePrefixes.put("strdf", "<http://strdf.di.uoa.gr/ontology#>");
        mapUsefulNamespacePrefixes.put("uom", "<http://www.opengis.net/def/uom/OGC/1.0/>");
        // populate IQuery Templates
        Map<String, String> mapQueryTemplates = new HashMap<>();
        mapQueryTemplates.put("NONTOP_GeoSPARQL", "SELECT (geof:<FUNCTION1>(?o1) AS ?ret) \nWHERE {\n\tGRAPH <GRAPH1> {\n\t\t?s1 <ASWKT1> ?o1\n\t}\n}");
        mapQueryTemplates.put("NONTOP_STSPARQL", "SELECT (strdf:<FUNCTION1>(?o1) AS ?ret) \nWHERE {\n\tGRAPH <GRAPH1> {\n\t\t?s1 <ASWKT1> ?o1\n\t}\n}");
        mapQueryTemplates.put("NONTOP_BUFFER", "SELECT (geof:<FUNCTION1>(?o1, 4, uom:metre) AS ?ret) \nWHERE {\n\tGRAPH <GRAPH1> {\n\t\t?s1 <ASWKT1> ?o1\n\t}\n}");
        mapQueryTemplates.put("SELECT_BASIC", "SELECT ?s1 ?o1 \nWHERE {\n\tGRAPH <GRAPH1> {\n\t\t?s1 <ASWKT1> ?o1\n\t}\nFILTER(geof:<FUNCTION1>(?o1, <GIVEN_SPATIAL_LITERAL>)).\n}");
        mapQueryTemplates.put("SELECT_BUFFER", "SELECT ?s1 \nWHERE {\n\tGRAPH <GRAPH1> {\n\t\t?s1 <ASWKT1> ?o1\n\t}\nFILTER(geof:<FUNCTION1>(?o1, geof:<FUNCTION2>("
                + givenPoint + ", " + givenRadius + ", uom:metre))).\n}");
        mapQueryTemplates.put("SELECT_DISTANCE", "SELECT ?s1 \nWHERE {\n\tGRAPH <GRAPH1> {\n\t\t?s1 <ASWKT1> ?o1\n\t}\nFILTER(geof:<FUNCTION1>(?o1, " + givenPoint
                + ", uom:metre) <= " + givenRadius + ").\n}");
        mapQueryTemplates.put("JOIN_1", "SELECT ?s1 ?s2 \nWHERE { \n\tGRAPH <GRAPH1> {\n\t\t?s1 <ASWKT1> ?o1\n\t}\n\tGRAPH <GRAPH2> {\n\t\t?s2 <ASWKT2> ?o2\n\t}\nFILTER(geof:<FUNCTION1>(?o1, ?o2)).\n}");
        mapQueryTemplates.put("JOIN_2", "SELECT ?s1 ?s2 \nWHERE { \n\tGRAPH <GRAPH1> {\n\t\t?s1 <ASWKT1> ?o1\n\t}\n\tGRAPH <GRAPH2> {\n\t\t?s2 <ASWKT2> ?o2\n\t}\nFILTER(?s1 != ?s2).\nFILTER(geof:<FUNCTION1>(?o1, ?o2)).\n}");
        mapQueryTemplates.put("AGGR_1", "SELECT (strdf:extent(?o1) AS ?ret)\nWHERE {\n\tGRAPH <http://geographica.di.uoa.gr/dataset/gag> {\n\t\t?s1 <http://geo.linkedopendata.gr/gag/ontology/asWKT> ?o1\n\t}\n}");
        mapQueryTemplates.put("AGGR_2", "SELECT (geof:union(?o1) AS ?ret)\nWHERE {\n\tGRAPH <http://geographica.di.uoa.gr/dataset/gag> {\n\t\t?s1 <http://geo.linkedopendata.gr/gag/ontology/asWKT> ?o1\n\t}\n}");
        // populate Graph prefixes map
        mapLiteralValues.put("LL_gadm", "<http://geographica.di.uoa.gr/dataset/gag>");
        mapLiteralValues.put("LL_clc", "<http://geographica.di.uoa.gr/dataset/clc>");
        mapLiteralValues.put("LL_lgd", "<http://geographica.di.uoa.gr/dataset/lgd>");
        mapLiteralValues.put("LL_geonames", "<http://geographica.di.uoa.gr/dataset/geonames>");
        mapLiteralValues.put("LL_dbpedia", "<http://geographica.di.uoa.gr/dataset/dbpedia>");
        mapLiteralValues.put("LL_hotspots", "<http://geographica.di.uoa.gr/dataset/hotspots>");
        // populate asWKT prefixes map
        mapLiteralValues.put("LL_clc_asWKT", "<http://geo.linkedopendata.gr/corine/ontology#asWKT>");
        mapLiteralValues.put("LL_dbpedia_asWKT", "<http://dbpedia.org/property/asWKT>");
        mapLiteralValues.put("LL_gadm_asWKT", "<http://geo.linkedopendata.gr/gag/ontology/asWKT>");
        mapLiteralValues.put("LL_geonames_asWKT", "<http://www.geonames.org/ontology#asWKT>");
        mapLiteralValues.put("LL_hotspots_asWKT", "<http://teleios.di.uoa.gr/ontologies/noaOntology.owl#asWKT>");
        mapLiteralValues.put("LL_lgd_asWKT", "<http://linkedgeodata.org/ontology/asWKT>");
        // populate with GeoSPARQL functions
        mapLiteralValues.put("LL_fn_boundary", "boundary");
        mapLiteralValues.put("LL_fn_distance", "distance");
        mapLiteralValues.put("LL_fn_envelope", "envelope");
        mapLiteralValues.put("LL_fn_convexHull", "convexHull");
        mapLiteralValues.put("LL_fn_buffer", "buffer");
        mapLiteralValues.put("LL_fn_area", "area");
        mapLiteralValues.put("LL_sfEquals", "sfEquals");
        mapLiteralValues.put("LL_sfIntersects", "sfIntersects");
        mapLiteralValues.put("LL_sfOverlaps", "sfOverlaps");
        mapLiteralValues.put("LL_sfCrosses", "sfCrosses");
        mapLiteralValues.put("LL_sfWithin", "sfWithin");
        mapLiteralValues.put("LL_sfDisjoint", "sfDisjoint");
        mapLiteralValues.put("LL_sfTouches", "sfTouches");

        // populate queries map (query number, query label, query template name)
        Map<Integer, IQuery> mapQry = new HashMap<>();
        // --- Non Topological Function Queries
        mapQry.put(0, new SimpleQuery("Boundary_CLC", "NONTOP_GeoSPARQL", false, 44834));
        mapQry.put(1, new SimpleQuery("Envelope_CLC", "NONTOP_GeoSPARQL", false, 44834));
        mapQry.put(2, new SimpleQuery("ConvexHull_CLC", "NONTOP_GeoSPARQL", false, 44834));
        mapQry.put(3, new SimpleQuery("Buffer_GeoNames", "NONTOP_BUFFER", false, 21990));
        mapQry.put(4, new SimpleQuery("Buffer_LGD", "NONTOP_BUFFER", false, 12097));
        mapQry.put(5, new SimpleQuery("Area_CLC", "NONTOP_STSPARQL", false, 44834));
        // --- Spatial Selection Queries
        mapQry.put(6, new SimpleQuery("Equals_LGD_GivenLine", "SELECT_BASIC", false, 1));
        mapQry.put(7, new SimpleQuery("Equals_GAG_GivenPolygon", "SELECT_BASIC", false, 1));
        mapQry.put(8, new SimpleQuery("Intersects_LGD_GivenPolygon", "SELECT_BASIC", false, 216));
        mapQry.put(9, new SimpleQuery("Intersects_CLC_GivenLine", "SELECT_BASIC", false, 20));
        mapQry.put(10, new SimpleQuery("Overlaps_CLC_GivenPolygon", "SELECT_BASIC", false, 132));
        mapQry.put(11, new SimpleQuery("Crosses_LGD_GivenLine", "SELECT_BASIC", false, 14));
        mapQry.put(12, new SimpleQuery("Within_GeoNames_GivenPolygon", "SELECT_BASIC", false, 136));
        mapQry.put(13, new SimpleQuery("Within_GeoNames_Point_Buffer", "SELECT_BUFFER", false, 152));
        mapQry.put(14, new SimpleQuery("GeoNames_Point_Distance", "SELECT_DISTANCE", false, 153));
        mapQry.put(15, new SimpleQuery("Disjoint_GeoNames_GivenPolygon", "SELECT_BASIC", false, 21854));
        mapQry.put(16, new SimpleQuery("Disjoint_LGD_GivenPolygon", "SELECT_BASIC", false, 11881));
        // --- Spatial Joins
        mapQry.put(17, new SimpleQuery("Equals_GeoNames_DBPedia", "JOIN_1", false, 91));
        mapQry.put(18, new SimpleQuery("Intersects_GeoNames_LGD", "JOIN_1", false, 0));
        mapQry.put(19, new SimpleQuery("Intersects_GeoNames_GAG", "JOIN_1", false, 18689));
        mapQry.put(20, new SimpleQuery("Intersects_LGD_GAG", "JOIN_1", false, 13318));
        mapQry.put(21, new SimpleQuery("Within_GeoNames_GAG", "JOIN_1", false, 18689));
        mapQry.put(22, new SimpleQuery("Within_LGD_GAG", "JOIN_1", false, 10673));
        mapQry.put(23, new SimpleQuery("Within_CLC_GAG", "JOIN_1", false, 34083));
        mapQry.put(24, new SimpleQuery("Crosses_LGD_GAG", "JOIN_1", false, 2645));
        mapQry.put(25, new SimpleQuery("Touches_GAG_GAG", "JOIN_2", false, 1022));
        mapQry.put(26, new SimpleQuery("Overlaps_GAG_CLC", "JOIN_1", false, 17005));
        // --- Spatial Aggregates
        mapQry.put(27, new SimpleQuery("Extent_GAG", "AGGR_1", false, 1));
        mapQry.put(28, new SimpleQuery("Union_GAG", "AGGR_2", false, 1));

        List<String> templateDynamicParamNameList = new ArrayList<>();
        templateDynamicParamNameList.add("<FUNCTION1>");
        templateDynamicParamNameList.add("<FUNCTION2>");
        templateDynamicParamNameList.add("<ASWKT1>");
        templateDynamicParamNameList.add("<ASWKT2>");
        templateDynamicParamNameList.add("<GRAPH1>");
        templateDynamicParamNameList.add("<GRAPH2>");
        templateDynamicParamNameList.add("<GIVEN_SPATIAL_LITERAL>");
        DynamicTempParamQS rwmicroQS
                = new DynamicTempParamQS("rwmicro", "", false,
                        mapQry, mapUsefulNamespacePrefixes,
                        mapQueryTemplates,
                        mapLiteralValues,
                        templateDynamicParamNameList);
        // populate the correspondence matrix to query template param replacement
        // --- Non Topological Function Queries
        rwmicroQS.getTemplateParamValueMatrix().put(0, "<GRAPH1>", "LL_clc");
        rwmicroQS.getTemplateParamValueMatrix().put(0, "<ASWKT1>", "LL_clc_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(0, "<FUNCTION1>", "LL_fn_boundary");
        rwmicroQS.getTemplateParamValueMatrix().put(1, "<GRAPH1>", "LL_clc");
        rwmicroQS.getTemplateParamValueMatrix().put(1, "<ASWKT1>", "LL_clc_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(1, "<FUNCTION1>", "LL_fn_envelope");
        rwmicroQS.getTemplateParamValueMatrix().put(2, "<GRAPH1>", "LL_clc");
        rwmicroQS.getTemplateParamValueMatrix().put(2, "<ASWKT1>", "LL_clc_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(2, "<FUNCTION1>", "LL_fn_convexHull");
        rwmicroQS.getTemplateParamValueMatrix().put(3, "<GRAPH1>", "LL_geonames");
        rwmicroQS.getTemplateParamValueMatrix().put(3, "<ASWKT1>", "LL_geonames_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(3, "<FUNCTION1>", "LL_fn_buffer");
        rwmicroQS.getTemplateParamValueMatrix().put(4, "<GRAPH1>", "LL_lgd");
        rwmicroQS.getTemplateParamValueMatrix().put(4, "<ASWKT1>", "LL_lgd_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(4, "<FUNCTION1>", "LL_fn_buffer");
        rwmicroQS.getTemplateParamValueMatrix().put(5, "<GRAPH1>", "LL_clc");
        rwmicroQS.getTemplateParamValueMatrix().put(5, "<ASWKT1>", "LL_clc_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(5, "<FUNCTION1>", "LL_fn_area");
        // --- Spatial Selection Queries
        rwmicroQS.getTemplateParamValueMatrix().put(6, "<GRAPH1>", "LL_lgd");
        rwmicroQS.getTemplateParamValueMatrix().put(6, "<ASWKT1>", "LL_lgd_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(6, "<FUNCTION1>", "LL_sfEquals");
        rwmicroQS.getTemplateParamValueMatrix().put(6, "<GIVEN_SPATIAL_LITERAL>", "LL_givenLine");
        rwmicroQS.getTemplateParamValueMatrix().put(7, "<GRAPH1>", "LL_gadm");
        rwmicroQS.getTemplateParamValueMatrix().put(7, "<ASWKT1>", "LL_gadm_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(7, "<FUNCTION1>", "LL_sfEquals");
        rwmicroQS.getTemplateParamValueMatrix().put(7, "<GIVEN_SPATIAL_LITERAL>", "LL_givenPolygon");
        rwmicroQS.getTemplateParamValueMatrix().put(8, "<GRAPH1>", "LL_lgd");
        rwmicroQS.getTemplateParamValueMatrix().put(8, "<ASWKT1>", "LL_lgd_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(8, "<FUNCTION1>", "LL_sfIntersects");
        rwmicroQS.getTemplateParamValueMatrix().put(8, "<GIVEN_SPATIAL_LITERAL>", "LL_givenPolygon");
        rwmicroQS.getTemplateParamValueMatrix().put(9, "<GRAPH1>", "LL_clc");
        rwmicroQS.getTemplateParamValueMatrix().put(9, "<ASWKT1>", "LL_clc_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(9, "<FUNCTION1>", "LL_sfIntersects");
        rwmicroQS.getTemplateParamValueMatrix().put(9, "<GIVEN_SPATIAL_LITERAL>", "LL_givenLine2");
        rwmicroQS.getTemplateParamValueMatrix().put(10, "<GRAPH1>", "LL_clc");
        rwmicroQS.getTemplateParamValueMatrix().put(10, "<ASWKT1>", "LL_clc_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(10, "<FUNCTION1>", "LL_sfOverlaps");
        rwmicroQS.getTemplateParamValueMatrix().put(10, "<GIVEN_SPATIAL_LITERAL>", "LL_givenPolygon");
        rwmicroQS.getTemplateParamValueMatrix().put(11, "<GRAPH1>", "LL_lgd");
        rwmicroQS.getTemplateParamValueMatrix().put(11, "<ASWKT1>", "LL_lgd_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(11, "<FUNCTION1>", "LL_sfCrosses");
        rwmicroQS.getTemplateParamValueMatrix().put(11, "<GIVEN_SPATIAL_LITERAL>", "LL_givenLine3");
        rwmicroQS.getTemplateParamValueMatrix().put(12, "<GRAPH1>", "LL_geonames");
        rwmicroQS.getTemplateParamValueMatrix().put(12, "<ASWKT1>", "LL_geonames_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(12, "<FUNCTION1>", "LL_sfWithin");
        rwmicroQS.getTemplateParamValueMatrix().put(12, "<GIVEN_SPATIAL_LITERAL>", "LL_givenPolygon");
        rwmicroQS.getTemplateParamValueMatrix().put(13, "<GRAPH1>", "LL_geonames");
        rwmicroQS.getTemplateParamValueMatrix().put(13, "<ASWKT1>", "LL_geonames_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(13, "<FUNCTION1>", "LL_sfWithin");
        rwmicroQS.getTemplateParamValueMatrix().put(13, "<FUNCTION2>", "LL_fn_buffer");
        rwmicroQS.getTemplateParamValueMatrix().put(14, "<GRAPH1>", "LL_geonames");
        rwmicroQS.getTemplateParamValueMatrix().put(14, "<ASWKT1>", "LL_geonames_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(14, "<FUNCTION1>", "LL_fn_distance");
        rwmicroQS.getTemplateParamValueMatrix().put(15, "<GRAPH1>", "LL_geonames");
        rwmicroQS.getTemplateParamValueMatrix().put(15, "<ASWKT1>", "LL_geonames_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(15, "<FUNCTION1>", "LL_sfDisjoint");
        rwmicroQS.getTemplateParamValueMatrix().put(15, "<GIVEN_SPATIAL_LITERAL>", "LL_givenPolygon");
        rwmicroQS.getTemplateParamValueMatrix().put(16, "<GRAPH1>", "LL_lgd");
        rwmicroQS.getTemplateParamValueMatrix().put(16, "<ASWKT1>", "LL_lgd_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(16, "<FUNCTION1>", "LL_sfDisjoint");
        rwmicroQS.getTemplateParamValueMatrix().put(16, "<GIVEN_SPATIAL_LITERAL>", "LL_givenPolygon");
        // --- Spatial Join Queries
        rwmicroQS.getTemplateParamValueMatrix().put(17, "<GRAPH1>", "LL_geonames");
        rwmicroQS.getTemplateParamValueMatrix().put(17, "<ASWKT1>", "LL_geonames_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(17, "<GRAPH2>", "LL_dbpedia");
        rwmicroQS.getTemplateParamValueMatrix().put(17, "<ASWKT2>", "LL_dbpedia_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(17, "<FUNCTION1>", "LL_sfEquals");
        rwmicroQS.getTemplateParamValueMatrix().put(18, "<GRAPH1>", "LL_geonames");
        rwmicroQS.getTemplateParamValueMatrix().put(18, "<ASWKT1>", "LL_geonames_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(18, "<GRAPH2>", "LL_lgd");
        rwmicroQS.getTemplateParamValueMatrix().put(18, "<ASWKT2>", "LL_lgd_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(18, "<FUNCTION1>", "LL_sfIntersects");
        rwmicroQS.getTemplateParamValueMatrix().put(19, "<GRAPH1>", "LL_geonames");
        rwmicroQS.getTemplateParamValueMatrix().put(19, "<ASWKT1>", "LL_geonames_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(19, "<GRAPH2>", "LL_gadm");
        rwmicroQS.getTemplateParamValueMatrix().put(19, "<ASWKT2>", "LL_gadm_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(19, "<FUNCTION1>", "LL_sfIntersects");
        rwmicroQS.getTemplateParamValueMatrix().put(20, "<GRAPH1>", "LL_lgd");
        rwmicroQS.getTemplateParamValueMatrix().put(20, "<ASWKT1>", "LL_lgd_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(20, "<GRAPH2>", "LL_gadm");
        rwmicroQS.getTemplateParamValueMatrix().put(20, "<ASWKT2>", "LL_gadm_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(20, "<FUNCTION1>", "LL_sfIntersects");
        rwmicroQS.getTemplateParamValueMatrix().put(21, "<GRAPH1>", "LL_geonames");
        rwmicroQS.getTemplateParamValueMatrix().put(21, "<ASWKT1>", "LL_geonames_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(21, "<GRAPH2>", "LL_gadm");
        rwmicroQS.getTemplateParamValueMatrix().put(21, "<ASWKT2>", "LL_gadm_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(21, "<FUNCTION1>", "LL_sfWithin");
        rwmicroQS.getTemplateParamValueMatrix().put(22, "<GRAPH1>", "LL_lgd");
        rwmicroQS.getTemplateParamValueMatrix().put(22, "<ASWKT1>", "LL_lgd_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(22, "<GRAPH2>", "LL_gadm");
        rwmicroQS.getTemplateParamValueMatrix().put(22, "<ASWKT2>", "LL_gadm_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(22, "<FUNCTION1>", "LL_sfWithin");
        rwmicroQS.getTemplateParamValueMatrix().put(23, "<GRAPH1>", "LL_clc");
        rwmicroQS.getTemplateParamValueMatrix().put(23, "<ASWKT1>", "LL_clc_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(23, "<GRAPH2>", "LL_gadm");
        rwmicroQS.getTemplateParamValueMatrix().put(23, "<ASWKT2>", "LL_gadm_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(23, "<FUNCTION1>", "LL_sfWithin");
        rwmicroQS.getTemplateParamValueMatrix().put(24, "<GRAPH1>", "LL_lgd");
        rwmicroQS.getTemplateParamValueMatrix().put(24, "<ASWKT1>", "LL_lgd_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(24, "<GRAPH2>", "LL_gadm");
        rwmicroQS.getTemplateParamValueMatrix().put(24, "<ASWKT2>", "LL_gadm_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(24, "<FUNCTION1>", "LL_sfCrosses");
        rwmicroQS.getTemplateParamValueMatrix().put(25, "<GRAPH1>", "LL_gadm");
        rwmicroQS.getTemplateParamValueMatrix().put(25, "<ASWKT1>", "LL_gadm_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(25, "<GRAPH2>", "LL_gadm");
        rwmicroQS.getTemplateParamValueMatrix().put(25, "<ASWKT2>", "LL_gadm_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(25, "<FUNCTION1>", "LL_sfTouches");
        rwmicroQS.getTemplateParamValueMatrix().put(26, "<GRAPH1>", "LL_gadm");
        rwmicroQS.getTemplateParamValueMatrix().put(26, "<ASWKT1>", "LL_gadm_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(26, "<GRAPH2>", "LL_clc");
        rwmicroQS.getTemplateParamValueMatrix().put(26, "<ASWKT2>", "LL_clc_asWKT");
        rwmicroQS.getTemplateParamValueMatrix().put(26, "<FUNCTION1>", "LL_sfOverlaps");
        // intentionally not using rwmicroQS.translateAllQueries() in order
        // to store large literals only once in template params and avoid storing
        // them multiple times in ground query text.
        try {
            rwmicroQS.serializeToJSON(new File(RWMICROJSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates the JSON definition file for the Macro Reverse Geocoding
     * geographica queryset in
     * Geographica/runtime/src/main/resources/json_defs/querysets/scalabilityQSoriginal.json
     *
     */
    public static void createMacroReverseGeocodingQS_OriginalJSONDefFile() {
        // 2. initialize the map of useful general RDF related prefixes
        Map<String, String> mapUsefulNamespacePrefixes = new HashMap<>();
        mapUsefulNamespacePrefixes.put("uom", "<http://www.opengis.net/def/uom/OGC/1.0/>");
        // populate IQuery Templates
        Map<Integer, IQuery> mapQry = new HashMap<>();
        mapQry.put(0, new SimpleQuery("Find_Closest_Populated_Place",
                "SELECT ?f (geof:distance(?cGeoWKT, <POINT_LITERAL>, uom:metre) AS ?distance)\n"
                + "WHERE {\n"
                + "\tGRAPH <http://geographica.di.uoa.gr/dataset/geonames> {\n"
                + "\t\t?f geonames:featureCode geonames:P.PPL;\n"
                + "\t\t\t<http://www.geonames.org/ontology#hasGeometry> ?cGeo.\n"
                + "\t\t?cGeo <http://www.geonames.org/ontology#asWKT> ?cGeoWKT.\n"
                + "\t}\n"
                + "}\n"
                + "ORDER BY ASC(?distance)\n"
                + "LIMIT 1",
                false));
        mapQry.put(1, new SimpleQuery("Find_Closest_Motorway",
                "SELECT ?c ?type ?label (geof:distance(?cGeoWKT, <POINT_LITERAL>, uom:metre) AS ?distance) ?cGeoWKT\n"
                + "WHERE {\n"
                + "\tGRAPH <http://geographica.di.uoa.gr/dataset/lgd> {\n"
                + "\t\t?c rdf:type lgdo:Motorway;\n"
                + "\t\t\trdfs:label ?label;\n"
                + "\t\t\t<http://linkedgeodata.org/ontology/hasGeometry> ?cGeo.\n"
                + "\t\t?cGeo <http://linkedgeodata.org/ontology/asWKT> ?cGeoWKT.\n"
                + "\t}\n"
                + "}\n"
                + "ORDER BY ASC(?distance)\n"
                + "LIMIT 1",
                false));

        String templateParamName = "<POINT_LITERAL>";
        MacroReverseGeocodingQS revgeoqs
                = new MacroReverseGeocodingQS("reversegeocoding", "", false,
                        mapQry, mapUsefulNamespacePrefixes, templateParamName);
        try {
            revgeoqs.serializeToJSON(new File(RWMACROREVERSEGEOCODING_JSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates the JSON definition file for the Macro Reverse Geocoding
     * geographica queryset in
     * Geographica/runtime/src/main/resources/json_defs/querysets/scalabilityQSoriginal.json
     *
     */
    public static void createMacroMapSearchQS_OriginalJSONDefFile() {
        // 2. initialize the map of useful general RDF related prefixes
        Map<String, String> mapUsefulNamespacePrefixes = new HashMap<>();
        mapUsefulNamespacePrefixes.put("lgo", "<http://linkedgeodata.org/ontology/>");
        // populate IQuery Templates
        Map<Integer, IQuery> mapQry = new HashMap<>();
        mapQry.put(0, new SimpleQuery("Thematic_Search",
                "SELECT ?f ?name ?geo ?wkt\n"
                + "WHERE {\n"
                + "\tGRAPH <http://geographica.di.uoa.gr/dataset/geonames> {\n"
                + "\t\t?f geonames:name ?name;\n"
                + "\t\t\t<http://www.geonames.org/ontology#hasGeometry> ?geo.\n"
                + "\t\t?geo <http://www.geonames.org/ontology#asWKT> ?wkt.\n"
                + "\t\tFILTER(?name = \"<TOPONYME>\"^^xsd:string)\n"
                + "\t}\n"
                + "}",
                false));
        mapQry.put(1, new SimpleQuery("Get_Around_POIs",
                "SELECT ?f ?name ?fGeo ?code ?parent ?class ?fGeoWKT\n"
                + "WHERE {\n"
                + "\tGRAPH <http://geographica.di.uoa.gr/dataset/geonames> {\n"
                + "\t\t?f geonames:name ?name;\n"
                + "\t\t\tgeonames:hasGeometry ?fGeo;\n"
                + "\t\t\tgeonames:featureCode ?code;\n"
                + "\t\t\tgeonames:parentFeature ?parent;\n"
                + "\t\t\tgeonames:featureClass ?class.\n"
                + "\t\t?fGeo geonames:asWKT ?fGeoWKT.\n"
                + "\t\tFILTER(geof:sfIntersects(?fGeoWKT, \"<RECTANGLE_LITERAL>\"^^geo:wktLiteral)).\n"
                + "\t}\n"
                + "}",
                false));
        mapQry.put(2, new SimpleQuery("Get_Around_Roads",
                "SELECT ?r ?label ?rGeo ?rGeoWKT\n"
                + "WHERE {\n"
                + "\tGRAPH <http://geographica.di.uoa.gr/dataset/lgd> {\n"
                + "\t\t?r rdf:type ?type.\n"
                + "\t\tOPTIONAL{ ?r rdfs:label ?label }.\n"
                + "\t\t?r lgo:hasGeometry ?rGeo.\n"
                + "\t\t?rGeo lgo:asWKT ?rGeoWKT.\n"
                + "\t\tFILTER(geof:sfIntersects(?rGeoWKT, \"<RECTANGLE_LITERAL>\"^^geo:wktLiteral)).\n"
                + "\t}\n"
                + "}",
                false));
        String toponymTemplateName = "<TOPONYME>";
        String rectangleTemplateName = "<RECTANGLE_LITERAL>";
        MacroMapSearchQS revgeoqs
                = new MacroMapSearchQS("mapsearch", "", false,
                        mapQry, mapUsefulNamespacePrefixes,
                        toponymTemplateName, rectangleTemplateName);
        try {
            revgeoqs.serializeToJSON(new File(RWMACROMAPSEARCH_JSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates the JSON definition file for the Macro Rapid Mapping geographica
     * queryset in
     * Geographica/runtime/src/main/resources/json_defs/querysets/scalabilityQSoriginal.json
     *
     */
    public static void createMacroRapidMappingQS_OriginalJSONDefFile() {
        // 2. initialize the map of useful general RDF related prefixes
        Map<String, String> mapUsefulNamespacePrefixes = new HashMap<>();
        mapUsefulNamespacePrefixes.put("lgo", "<http://linkedgeodata.org/ontology/>");
        // populate IQuery Templates
        Map<Integer, IQuery> mapQry = new HashMap<>();
        mapQry.put(0, new SimpleQuery("Get_CLC_areas",
                "SELECT ?a ?aID ?aLandUse ?aGeo ?aGeoWKT\n"
                + "WHERE { \n"
                + "	GRAPH <http://geographica.di.uoa.gr/dataset/clc> { \n"
                + "		?a rdf:type clc:Area.\n"
                + "		?a clc:hasID ?aID.\n"
                + "		?a clc:hasLandUse ?aLandUse.\n"
                + "		?a clc:hasGeometry ?aGeo.\n"
                + "		?aGeo clc:asWKT ?aGeoWKT. \n"
                + "		FILTER(geof:sfIntersects(?aGeoWKT, \"<GIVEN_POLYGON_IN_WKT>\"^^geo:wktLiteral))\n"
                + "	}\n"
                + "}",
                false));
        mapQry.put(1, new SimpleQuery("Get_highways",
                "SELECT ?r ?rName ?rGeoWKT\n"
                + "WHERE { \n"
                + "	GRAPH <http://geographica.di.uoa.gr/dataset/lgd> { \n"
                + "		?r rdf:type lgdo:HighwayThing.\n"
                + "		?r rdfs:label ?rName.\n"
                + "		?r lgdo:hasGeometry ?rGeo.\n"
                + "		?rGeo lgdo:asWKT ?rGeoWKT.\n"
                + "		FILTER(geof:sfIntersects(?rGeoWKT, \"<GIVEN_POLYGON_IN_WKT>\"^^geo:wktLiteral))\n"
                + "	} \n"
                + "}",
                false));
        mapQry.put(2, new SimpleQuery("Get_municipalities",
                "SELECT (geof:boundary(?gGeoWKT) as ?boundary) ?gLabel \n"
                + "WHERE { \n"
                + "	GRAPH <http://geographica.di.uoa.gr/dataset/gag> { \n"
                + "		?g rdf:type gag:Municipality.\n"
                + "		?g rdfs:label ?gLabel.\n"
                + "		?g gag:hasGeometry ?gGeo.\n"
                + "		?gGeo gag:asWKT ?gGeoWKT.\n"
                + "		FILTER(geof:sfIntersects(?gGeoWKT, \"<GIVEN_POLYGON_IN_WKT>\"^^geo:wktLiteral))\n"
                + "	} \n"
                + "}",
                false));
        mapQry.put(3, new SimpleQuery("Get_hotspots",
                "SELECT ?h ?sensor ?confidence ?producer ?satellite ?chain ?confirmation ?geometry ?r ?wkt\n"
                + "WHERE {  \n"
                + "	GRAPH <http://geographica.di.uoa.gr/dataset/hotspots> { \n"
                + "		?h rdf:type noa:Hotspot.\n"
                + "		?h noa:isDerivedFromSensor ?sensor.\n"
                + "		?h noa:hasConfidence ?confidence.\n"
                + "		?h noa:isProducedBy ?producer.\n"
                + "		?h noa:isDerivedFromSatellite ?satellite.\n"
                + "		?h noa:producedFromProcessingChain ?chain.\n"
                + "		?h noa:hasConfirmation ?confirmation .\n"
                + "		?h noa:hasAcquisitionTime \"<TIMESTAMP>\"^^xsd:dateTime.\n"
                + "		?h noa:hasGeometry ?geomentry. \n"
                + "		OPTIONAL {?h noa:refinedBy ?r} \n"
                + "		FILTER(!bound(?r)) \n"
                + "		?geometry noa:asWKT ?wkt . \n"
                + "		FILTER(geof:sfIntersects(?wkt, \"<GIVEN_POLYGON_IN_WKT>\"^^geo:wktLiteral))\n"
                + "	}\n"
                + "}",
                false));
        mapQry.put(4, new SimpleQuery("Get_coniferous_forests_in_fire",
                "SELECT ?h ?hWKT \n"
                + "WHERE { \n"
                + "	GRAPH <http://geographica.di.uoa.gr/dataset/hotspots> { \n"
                + "		?h rdf:type noa:Hotspot.\n"
                + "		?h noa:hasGeometry ?hGeo.\n"
                + "		?h noa:hasAcquisitionTime \"<TIMESTAMP>\"^^xsd:dateTime. \n"
                + "		?hGeo noa:asWKT ?hWKT. \n"
                + "	} \n"
                + "	GRAPH <http://geographica.di.uoa.gr/dataset/clc> { \n"
                + "		?a a clc:Area.\n"
                + "		?a clc:hasGeometry ?aGeo.\n"
                + "		?a clc:hasLandUse clc:coniferousForest. \n"
                + "		?aGeo clc:asWKT ?aWKT. \n"
                + "	} \n"
                + "	FILTER(geof:sfIntersects(?hWKT, ?aWKT)) . \n"
                + "	FILTER(geof:sfIntersects(?aWKT, \"<GIVEN_POLYGON_IN_WKT>\"^^geo:wktLiteral))\n"
                + "}",
                false));
        mapQry.put(5, new SimpleQuery("Get_road_segments_affected_by_fire",
                "SELECT ?r (geof:difference(?rWKT, ?hWKT) as ?diff) \n"
                + "WHERE { \n"
                + "	GRAPH <http://geographica.di.uoa.gr/dataset/hotspots> { \n"
                + "		?h rdf:type noa:Hotspot.\n"
                + "		?h noa:hasGeometry ?hGeo.\n"
                + "		?h noa:hasAcquisitionTime ?hAcqTime. \n"
                + "		?hGeo noa:asWKT ?hWKT. \n"
                + "	} \n"
                + "	GRAPH <http://geographica.di.uoa.gr/dataset/lgd> { \n"
                + "		?r rdf:type lgdo:HighwayThing.\n"
                + "		?r lgdo:hasGeometry ?rGeo.\n"
                + "		?rGeo lgdo:asWKT ?rWKT. \n"
                + "	} \n"
                + "	FILTER(geof:sfIntersects(?rWKT, ?hWKT)) .  \n"
                + "	FILTER(geof:sfIntersects(?rWKT, \"<GIVEN_POLYGON_IN_WKT>\"^^geo:wktLiteral))\n"
                + "}",
                false));
        String timestampTemplateName = "<TIMESTAMP>";
        String polygonTemplateName = "<GIVEN_POLYGON_IN_WKT>";
        MacroRapidMappingQS rapmapqs
                = new MacroRapidMappingQS("rapidmapping", "", false,
                        mapQry, mapUsefulNamespacePrefixes,
                        timestampTemplateName, polygonTemplateName);
        try {
            rapmapqs.serializeToJSON(new File(RWMACRORAPIDMAPPING_JSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates the JSON definition file for the Macro Compute Statistics
     * geographica queryset in
     * Geographica/runtime/src/main/resources/json_defs/querysets/scalabilityQSoriginal.json
     *
     */
    public static void createMacroComputeStatisticsQS_OriginalJSONDefFile() {
        // 2. initialize the map of useful general RDF related prefixes
        Map<String, String> mapUsefulNamespacePrefixes = new HashMap<>();
        mapUsefulNamespacePrefixes.put("lgo", "<http://linkedgeodata.org/ontology/>");
        // populate IQuery Templates
        Map<Integer, IQuery> mapQry = new HashMap<>();
        mapQry.put(0, new SimpleQuery("Count_CLC_categories",
                "SELECT ?clcLandUse (COUNT(DISTINCT ?clc) AS ?count) \n"
                + "WHERE { \n"
                + "	GRAPH <http://geographica.di.uoa.gr/dataset/clc> { \n"
                + "		?clc rdf:type clc:Area. \n"
                + "		?clc clc:hasLandUse ?clcLandUse. \n"
                + "		?clc clc:hasGeometry ?clcGeo. \n"
                + "		?clcGeo clc:asWKT ?clcWkt. \n"
                + "		FILTER(geof:sfIntersects(?clcWkt, \"<MUNICIPALITY_WKT>\"^^geo:wktLiteral)). \n"
                + "}} \n"
                + "GROUP BY ?clcLandUse",
                false));
        mapQry.put(1, new SimpleQuery("Count_GeoNames_categories",
                "SELECT ?fClass ?fCode (COUNT(DISTINCT ?f) as ?count) \n"
                + "WHERE  \n"
                + "{ \n"
                + "	GRAPH <http://geographica.di.uoa.gr/dataset/geonames> { \n"
                + "		?f rdf:type geonames:Feature. \n"
                + "		?f geonames:featureClass ?fClass. \n"
                + "		?f geonames:featureCode ?fCode. \n"
                + "		?f geonames:hasGeometry ?fGeo. \n"
                + "		?fGeo geonames:asWKT ?fWkt. \n"
                + "		FILTER(geof:sfIntersects(?fWkt, \"<MUNICIPALITY_WKT>\"^^geo:wktLiteral)). \n"
                + " }} \n"
                + "GROUP BY ?fClass ?fCode",
                false));
        mapQry.put(2, new SimpleQuery("Count_GeoNames_categories_in_ContinuousUrbanFabric",
                "SELECT ?fClass (COUNT(DISTINCT ?f) as ?count) \n"
                + "WHERE { \n"
                + "	GRAPH <http://geographica.di.uoa.gr/dataset/clc> { \n"
                + "		?clc rdf:type clc:Area. \n"
                + "		?clc clc:hasLandUse clc:continuousUrbanFabric. \n"
                + "		?clc clc:hasGeometry ?clcGeo. \n"
                + "		?clcGeo clc:asWKT ?clcWkt. \n"
                + "		FILTER(geof:sfIntersects(?clcWkt, \"<MUNICIPALITY_WKT>\"^^geo:wktLiteral)). \n"
                + "} \n"
                + "	GRAPH <http://geographica.di.uoa.gr/dataset/geonames> { \n"
                + "		?f rdf:type geonames:Feature. \n"
                + "		?f geonames:featureClass ?fClass. \n"
                + "		?f geonames:hasGeometry ?fGeo. \n"
                + "		?fGeo geonames:asWKT ?fWkt. \n"
                + "		FILTER(geof:sfIntersects(?fWkt, \"<MUNICIPALITY_WKT>\"^^geo:wktLiteral)). \n"
                + " } \n"
                + "	FILTER(geof:sfIntersects(?clcWkt, ?fWkt)). \n"
                + "} \n"
                + "GROUP BY ?fClass",
                false));
        String municipalityWKTTemplateName = "<MUNICIPALITY_WKT>";
        MacroComputeStatisticsQS compstatsqs
                = new MacroComputeStatisticsQS("computestatistics", "", false,
                        mapQry, mapUsefulNamespacePrefixes,
                        municipalityWKTTemplateName);
        try {
            compstatsqs.serializeToJSON(new File(RWMACROCOMPUTESTATISTICS_JSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Creates the JSON definition file for the Macro Reverse Geocoding
     * geographica queryset in
     * Geographica/runtime/src/main/resources/json_defs/querysets/scalabilityQSoriginal.json
     *
     */
    public static void createMacroGeocodingQS_OriginalJSONDefFile() {
        // 2. initialize the map of useful general RDF related prefixes
        Map<String, String> mapUsefulNamespacePrefixes = new HashMap<>();
        mapUsefulNamespacePrefixes.put("census", "<http://geographica.di.uoa.gr/cencus/ontology#>");
        // populate IQuery Templates
        Map<Integer, IQuery> mapQry = new HashMap<>();
        mapQry.put(0, new SimpleQuery("Geocode_left",
                "SELECT ?lfromhn ?ltohn ?wkt \n"
                + "((<HOUSE_NO>-?lfromhn)*((?x1-?x0)/(?ltohn-?lfromhn))+?x1 AS ?x) \n"
                + "((<HOUSE_NO>-?lfromhn)*((?y1-?y0)/(?ltohn-?lfromhn))+?y1 AS ?y) \n"
                + "WHERE { \n"
                + "	?f census:lfromhn ?lfromhn. \n"
                + "	?f census:ltohn ?ltohn. \n"
                + "	?f census:parityl <PARITY> . \n"
                + "	?f census:fullname <STREET_NAME> . \n"
                + "	?f census:zipl \"<ZIP>\"^^xsd:integer. \n"
                + "	?f census:hasGeometry ?geo. \n"
                + "	?f census:minx ?x0. \n"
                + "	?f census:miny ?y0. \n"
                + "	?f census:maxx ?x1. \n"
                + "	?f census:maxy ?y1. \n"
                + "	?geo census:asWKT ?wkt. \n"
                + "	FILTER( (?lfromhn <= \"<HOUSE_NO>\"^^xsd:integer && \"<HOUSE_NO>\"^^xsd:integer <= ?ltohn) \n"
                + "		 || (?ltohn <= \"<HOUSE_NO>\"^^xsd:integer && \"<HOUSE_NO>\"^^xsd:integer <= ?lfromhn) ). \n"
                + "}",
                false));
        mapQry.put(1, new SimpleQuery("Geocode_right",
                "SELECT ?rfromhn ?rtohn ?wkt \n"
                + "((<HOUSE_NO>-?rfromhn)*((?x1-?x0)/(?rtohn-?rfromhn))+?x1 AS ?x) \n"
                + "((<HOUSE_NO>-?rfromhn)*((?y1-?y0)/(?rtohn-?rfromhn))+?y1 AS ?y) \n"
                + "WHERE { \n"
                + "	?f census:rfromhn ?rfromhn. \n"
                + "	?f census:rtohn ?rtohn. \n"
                + "	?f census:parityr <PARITY> . \n"
                + "	?f census:fullname <STREET_NAME> . \n"
                + "	?f census:zipr \"<ZIP>\"^^xsd:integer. \n"
                + "	?f census:hasGeometry ?geo. \n"
                + "	?f census:minx ?x0. \n"
                + "	?f census:miny ?y0. \n"
                + "	?f census:maxx ?x1. \n"
                + "	?f census:maxy ?y1. \n"
                + "	?geo census:asWKT ?wkt. \n"
                + "	FILTER( (?rfromhn <= \"<HOUSE_NO>\"^^xsd:integer && \"<HOUSE_NO>\"^^xsd:integer <= ?rtohn) \n"
                + "		 || (?rtohn <= \"<HOUSE_NO>\"^^xsd:integer && \"<HOUSE_NO>\"^^xsd:integer <= ?rfromhn) ). \n"
                + "}",
                false));
        String streetNameTemplateName = "<STREET_NAME>";
        String houseNoTemplateName = "<HOUSE_NO>";
        String zipTemplateName = "<ZIP>";
        String parityTemplateName = "<PARITY>";
        MacroGeocodingQS geocodingqs
                = new MacroGeocodingQS("geocoding", "", false,
                        mapQry, mapUsefulNamespacePrefixes,
                        streetNameTemplateName, houseNoTemplateName,
                        zipTemplateName, parityTemplateName);
        try {
            geocodingqs.serializeToJSON(new File(RWMACROGEOCODING_JSONDEF_FILE));
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    // A) -- Methods that can re-create the JSON definition files
    //       for GeoRDFBench workloads
    /**
     * Creates the JSON definition file for the LUBM(1, 0) workload in
     * ./json_defs/workloads/LUBM_1_0_WLoriginal_GOLD_STANDARD.json
     *
     */
    public static void createLUBM_1_0_OriginalJSONDefFile(String outputDir) {
        // 3. Create a queryset spec
        Map<String, String> mapTemplateParams = new HashMap<>();
        Map<String, String> mapUsefulNamespacePrefixes = new HashMap<>();
        mapUsefulNamespacePrefixes.put("ub", "<https://swat.cse.lehigh.edu/onto/univ-bench.owl#>");
        // populate Graph prefixes map
        Map<String, String> mapLiteralValues = new HashMap<>();

        Map<Integer, IQuery> mapQry = new HashMap<>();
        // using SimpleQuery(String label, String query, boolean usePredicate, long expectedResults)
        mapQry.put(0, new SimpleQuery("Q1_GradStudents_Taken_GradCource0_At_Univ0",
                "SELECT ?x WHERE {\n"
                + " ?x rdf:type ub:GraduateStudent .\n"
                + " ?x ub:takesCourse <http://www.Department0.University0.edu/GraduateCourse0> .\n"
                + "} \n",
                false,
                4));
        mapQry.put(1, new SimpleQuery("Q2_GradStudents_From_Any_Univ_Depart",
                "SELECT ?x ?y ?z WHERE { \n ?x rdf:type ub:GraduateStudent .\n"
                + " ?y rdf:type ub:University .\n"
                + " ?z rdf:type ub:Department .\n"
                + " ?x ub:memberOf ?z .\n"
                + " ?z ub:subOrganizationOf ?y .\n"
                + " ?x ub:undergraduateDegreeFrom ?y .\n"
                + "} \n",
                false,
                0));
        mapQry.put(2, new SimpleQuery("Q3_Publications_Of_AssistProfessor0_In_Univ0_Depart0",
                "SELECT ?x WHERE { \n ?x rdf:type ub:Publication .\n"
                + " ?x ub:publicationAuthor <http://www.Department0.University0.edu/AssistantProfessor0> .\n} \n",
                false,
                6));
        mapQry.put(3, new SimpleQuery("Q4_Properties_Of_Professors_In_Univ0_Depart0",
                "SELECT ?x ?y1 ?y2 ?y3 WHERE { \n ?x rdf:type ub:Professor .\n"
                + "  ?x ub:worksFor <http://www.Department0.University0.edu> .\n"
                + "  ?x ub:name ?y1 .\n"
                + "  ?x ub:emailAddress ?y2 .\n"
                + "  ?x ub:telephone ?y3 .\n} \n",
                false,
                -1));
        mapQry.put(4, new SimpleQuery("Q5_Persons_MembersOf_In_Univ0_Depart0",
                "SELECT ?x WHERE { \n ?x rdf:type ub:Person .\n"
                + "  ?x ub:memberOf <http://www.Department0.University0.edu> .\n} \n",
                false,
                -1));
        mapQry.put(5, new SimpleQuery("Q6_All_Students",
                "SELECT ?x WHERE { \n ?x rdf:type ub:Student .\n} \n",
                false,
                -1));
        // ... add as many queries as desired
        mapQry.put(13, new SimpleQuery("Q14_UndergradStudents_From_Any_Univ_Depart",
                "SELECT ?x WHERE { \n ?x rdf:type ub:UndergraduateStudent .\n} \n",
                false,
                5916));

        StaticTempParamQS lubm_qs = new StaticTempParamQS("lubm-1_0", "", false, mapQry,
                mapTemplateParams, mapUsefulNamespacePrefixes,
                mapLiteralValues);
        try {
            ObjectMapper om = new ObjectMapper();
            om.enable(SerializationFeature.INDENT_OUTPUT);
            om.writeValue(new File(LUBM_1_0_JSONDEF_FILE), lubm_qs);
        } catch (JsonMappingException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    // B) -- Methods that can re-create querysets from the JSON definition files
    /**
     * Deserialize from JSON file
     *
     * @param fileName
     * @return GeographicaDataSet object
     */
    public static IQuerySet deserializeFromJSON(String fileName) {
        File serObjFile = new File(fileName);
        // create the mapper
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        SimpleQS qs = null;
        try {
            qs = mapper.readValue(serObjFile, new TypeReference<SimpleQS>() {
            });
        } catch (IOException ex) {
            logger.info(ex.getMessage());
        }
        qs.initializeAfterDeserialization();
        return qs;
    }

    public static void main(String[] args) throws JsonProcessingException, IOException {
        // Realworld Micro Queryset - dynamic template
        QuerySetUtil.createRWMicroQS_OriginalJSONDefFile();
        IQuerySet qs
                = QuerySetUtil.deserializeFromJSON(QuerySetUtil.RWMICROJSONDEF_FILE);
        logger.info(qs.serializeToJSON());
        // Realworld Macro Reverse Geocoding Queryset
        QuerySetUtil.createMacroReverseGeocodingQS_OriginalJSONDefFile();
        qs = QuerySetUtil.deserializeFromJSON(QuerySetUtil.RWMACROREVERSEGEOCODING_JSONDEF_FILE);
        logger.info(qs.serializeToJSON());
        // Realworld Macro Map Search Queryset
        QuerySetUtil.createMacroMapSearchQS_OriginalJSONDefFile();
        qs = QuerySetUtil.deserializeFromJSON(QuerySetUtil.RWMACROMAPSEARCH_JSONDEF_FILE);
        logger.info(qs.serializeToJSON());
        // Realworld Macro Rapid Mapping Queryset
        QuerySetUtil.createMacroRapidMappingQS_OriginalJSONDefFile();
        qs = QuerySetUtil.deserializeFromJSON(QuerySetUtil.RWMACRORAPIDMAPPING_JSONDEF_FILE);
        logger.info(qs.serializeToJSON());
        // Realworld Macro Compute Statistics Queryset
        QuerySetUtil.createMacroComputeStatisticsQS_OriginalJSONDefFile();
        qs = QuerySetUtil.deserializeFromJSON(QuerySetUtil.RWMACROCOMPUTESTATISTICS_JSONDEF_FILE);
        logger.info(qs.serializeToJSON());
        // Realworld Macro Geocoding Queryset
        QuerySetUtil.createMacroGeocodingQS_OriginalJSONDefFile();
        qs = QuerySetUtil.deserializeFromJSON(QuerySetUtil.RWMACROGEOCODING_JSONDEF_FILE);
        logger.info(qs.serializeToJSON());
        // Synthetic N=512 Queryset
        QuerySetUtil.createSyntheticQS_OriginalJSONDefFile(512);
        qs = QuerySetUtil.deserializeFromJSON(QuerySetUtil.SYNTHETICJSONDEF_FILE);
        logger.info(qs.serializeToJSON());
        // Scalability Queryset
        QuerySetUtil.createScalabilityFuncQS_OriginalJSONDefFile();
        qs = QuerySetUtil.deserializeFromJSON(QuerySetUtil.SCALABILITY_FUNC_JSONDEF_FILE);
        logger.info(qs.serializeToJSON());
        QuerySetUtil.createScalabilityPredQS_OriginalJSONDefFile();
        qs = QuerySetUtil.deserializeFromJSON(QuerySetUtil.SCALABILITY_PRED_JSONDEF_FILE);
        logger.info(qs.serializeToJSON());
        // LUBM Workload
        QuerySetUtil.createLUBM_1_0_OriginalJSONDefFile(QuerySetUtil.LUBM_1_0_JSONDEF_FILE);
        qs = QuerySetUtil.deserializeFromJSON(QuerySetUtil.LUBM_1_0_JSONDEF_FILE);
        logger.info(qs.serializeToJSON());
        
    }
}
