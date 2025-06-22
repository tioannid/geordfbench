package gr.uoa.di.rdf.geordfbench.runtime.querysets.partofworkload.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gr.uoa.di.rdf.geordfbench.runtime.executionspecs.IExecutionSpec;
import gr.uoa.di.rdf.geordfbench.runtime.executionspecs.impl.SimpleES;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.geordfbench.runtime.querysets.simple.IQuery;
import gr.uoa.di.rdf.geordfbench.runtime.querysets.simple.impl.SimpleQuery;
import gr.uoa.di.rdf.geordfbench.runtime.querysets.simple.impl.SimpleQuery.QueryAccuracyValidation;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public class MacroQS extends SimpleQSPartOfWorkload {

    // --- Static members -----------------------------
    static {
        logger = Logger.getLogger(MacroQS.class.getSimpleName());
    }

    /**
     * Creates the Macro Geocoding-Census geographica queryset
     *
     * @return the Macro Geocoding-Census geographica queryset
     */
    public static MacroGeocodingQS newMacroGeocodingQS() {
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
        return new MacroGeocodingQS(SimpleES.newMacroES(),
                "geocoding", "", false,
                mapQry, mapUsefulNamespacePrefixes,
                streetNameTemplateName, houseNoTemplateName,
                zipTemplateName, parityTemplateName);

    }

    /**
     * Creates the Macro MapSearch geographica queryset
     *
     * @return the Macro MapSearch geographica queryset
     */
    public static MacroMapSearchQS newMacroMapSearchQS() {
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
        return new MacroMapSearchQS(SimpleES.newMacroES(),
                "mapsearch", "", false,
                mapQry, mapUsefulNamespacePrefixes,
                toponymTemplateName, rectangleTemplateName);
    }

    /**
     * Creates the Macro Reverse Geocoding geographica queryset
     *
     * @return the Macro Reverse Geocoding geographica queryset
     */
    public static MacroReverseGeocodingQS newMacroReverseGeocodingQS() {
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
        return new MacroReverseGeocodingQS(SimpleES.newMacroES(),
                "reversegeocoding", "", false,
                mapQry, mapUsefulNamespacePrefixes, templateParamName);
    }

    /**
     * Creates the Macro Rapid Mapping geographica queryset
     *
     * @return the Macro Rapid Mapping geographica queryset
     */
    public static MacroRapidMappingQS newMacroRapidMappingQS() {
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
        return new MacroRapidMappingQS(SimpleES.newMacroES(),
                "rapidmapping", "", false,
                mapQry, mapUsefulNamespacePrefixes,
                timestampTemplateName, polygonTemplateName);
    }

    /**
     * Creates the Macro Compute Statistics geographica queryset
     *
     * @return the Macro Compute Statistics geographica queryset
     */
    public static MacroComputeStatisticsQS newMacroComputeStatisticsQS() {
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
        return new MacroComputeStatisticsQS(SimpleES.newMacroES(),
                "computestatistics", "", false,
                mapQry, mapUsefulNamespacePrefixes,
                municipalityWKTTemplateName);
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
    public MacroQS(IExecutionSpec executionSpec, String name,
            String relativeBaseDir, boolean hasPredicateQueriesAlso,
            Map<Integer, IQuery> mapQueries,
            Map<String, String> mapUsefulNamespacePrefixes) {
        super(executionSpec, name, relativeBaseDir, hasPredicateQueriesAlso,
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
     * to initialize/translate the queries of the query set before a complete
     * run of all the queries takes place. It allows for implementing classes to
     * use ad-hoc logic to achieve this..
     *
     * MacroQS uses static text queries and therefore does not need to do
     * anything.
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
