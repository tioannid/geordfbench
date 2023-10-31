/**
 * @author GeoRDFBench Creator <GeoRDFBench@Univ>
 */
package gr.uoa.di.rdf.Geographica3.runtime.querysets.partofworkload.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gr.uoa.di.rdf.Geographica3.runtime.executionspecs.IExecutionSpec;
import gr.uoa.di.rdf.Geographica3.runtime.executionspecs.impl.SimpleES;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.simple.IQuery;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.simple.impl.SimpleQuery;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.util.QuerySetPartOfWorkloadUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A Geographica query set implementation that allows dynamically creating the
 * final query set by specifying: - list of query templates to use - list of
 * template parameters to use - the declarative matrix of template parameter
 * values to use for each query
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public class DynamicTempParamQS extends SimpleQSPartOfWorkload {

    // --- Static members -----------------------------
    public static String WKT_LITERAL = "<http://www.opengis.net/ont/geosparql#wktLiteral>";
    public static final String RWMICRO_SELECTIONS_POLYGON_FILE = "givenPolygon.txt";
    public static final String RWMICRO_SELECTIONS_LINES_FILE = "givenLine.txt";

    // --- Static block/clause -----------------------
    static {
        logger = Logger.getLogger(DynamicTempParamQS.class.getSimpleName());
    }

    public static DynamicTempParamQS newRWMicroQS() {
        // read fixed Polygon from external file which might be used in spatial selection queries
        String givenPolygon = "";
        InputStream is = QuerySetPartOfWorkloadUtil.class.getResourceAsStream("/" + RWMICRO_SELECTIONS_POLYGON_FILE);
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
        is = QuerySetPartOfWorkloadUtil.class.getResourceAsStream("/" + RWMICRO_SELECTIONS_LINES_FILE);
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
        mapQry.put(3, new SimpleQuery("Buffer_GeoNames", "NONTOP_BUFFER", false,21990));
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
                = new DynamicTempParamQS(SimpleES.newMicroES(), "rwmicro", "", false,
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
        return rwmicroQS;
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
    public DynamicTempParamQS(IExecutionSpec executionSpec, String name,
            String relativeBaseDir, boolean hasPredicateQueriesAlso,
            Map<Integer, IQuery> mapQueries,
            Map<String, String> mapUsefulNamespacePrefixes,
            Map<String, String> mapQueryTemplates,
            Map<String, String> mapLiteralValues,
            List<String> templateDynamicParamNameList) {
        super(executionSpec, name, relativeBaseDir, hasPredicateQueriesAlso,
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
     * @param executionSpec
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
    public DynamicTempParamQS(IExecutionSpec executionSpec, String name,
            String relativeBaseDir, boolean hasPredicateQueriesAlso,
            Map<Integer, IQuery> mapQueries,
            Map<String, String> mapUsefulNamespacePrefixes,
            Map<String, String> mapQueryTemplates,
            Map<String, String> mapLiteralValues,
            List<String> templateDynamicParamNameList,
            DynamicQryTempParamMatrix templateParamValueMatrix,
            boolean isTranslated) {
        super(executionSpec, name, relativeBaseDir, hasPredicateQueriesAlso, mapQueries,
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
