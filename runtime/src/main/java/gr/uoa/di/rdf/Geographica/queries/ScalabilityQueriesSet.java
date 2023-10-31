/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2013, Pyravlos Team
 *
 */
package gr.uoa.di.rdf.Geographica.queries;

import gr.uoa.di.rdf.Geographica.systemsundertest.SystemUnderTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

/**
 * @author GeoRDFBench Creator <GeoRDFBench@Univ>
 */
public class ScalabilityQueriesSet extends QueriesSet {

    static Logger logger = Logger.getLogger(ScalabilityQueriesSet.class.getSimpleName());

    // Template to create spatial selection queries
    private String spatialSelectionQryTemplate
            = "\n SELECT ?s1 ?o1 WHERE { \n"
            + " ?s1 geo:asWKT ?o1 . \n"
            + "  FILTER(geof:FUNCTION(?o1, GIVEN_SPATIAL_LITERAL)). \n"
            + "} \n";

    private String spatialJoinHardQryTemplate
            =   "SELECT ?s1 ?s2 \n" +
                "WHERE { \n" +
                " ?s1 geo:hasGeometry [ geo:asWKT ?o1 ] ;\n" +
                "    lgd:has_code \"1001\"^^xsd:integer . \n" +
                " ?s2 geo:hasGeometry [ geo:asWKT ?o2 ] ;\n" +
                "    lgd:has_code ?code2 .  \n" +
                " FILTER(?code2>5000 && ?code2<6000 && ?code2 != 5260) .\n" +
                " FILTER(geof:FUNCTION(?o1, ?o2)). \n" +
                "} ";

    private String spatialJoinEasyQryTemplate
            =   "SELECT ?s1 ?s2\n" +
                "WHERE {\n" +
                " ?s1 geo:hasGeometry [ geo:asWKT ?o1 ] ;\n" +
                "    lgd:has_code \"1001\"^^xsd:integer .\n" +
                " ?s2 geo:hasGeometry [ geo:asWKT ?o2 ] ;\n" +
                "    lgd:has_code ?code2 .\n" +
                " FILTER(?code2 IN (5622, 5601, 5641, 5621, 5661)) .\n" +
                " FILTER(geof:FUNCTION(?o1, ?o2)).\n" +
                "}";
            
    private String givenPolygonFile = "givenPolygonCrossesEurope.txt";
    private String givenPolygon;
    private String spatialDatatype = "<http://www.opengis.net/ont/geosparql#wktLiteral>";

    public ScalabilityQueriesSet(SystemUnderTest sut) throws IOException {
        super(sut);
        // redefine the prefixes to include just the necessary prefixes
        prefixes = "PREFIX geof: <http://www.opengis.net/def/function/geosparql/> \n" +
                    "PREFIX geo: <http://www.opengis.net/ont/geosparql#>  \n" +
                    "PREFIX lgd: <http://data.linkedeodata.eu/ontology#> \n" +
                    "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>";
        queriesN = 3; // IMPORTANT: Add/remove queries in getQuery implies changing queriesN

        // read static Polygon from external file which might be used in spatial selection queries
        InputStream is = getClass().getResourceAsStream("/" + givenPolygonFile);
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        givenPolygon = in.readLine();
        givenPolygon = "\"" + givenPolygon + "\"^^" + spatialDatatype;
        in.close();
        in = null;
        is.close();
        is = null;
    }

    @Override
    public QueryStruct getQuery(int queryIndex, int repetition) {

        String query = null, label = null;

        // IMPORTANT: Add/remove queries in getQuery implies changing queriesN and changing case numbers
        switch (queryIndex) {

            case 0:
                // SC1 - Find all features that spatially intersect with a given polygon
                label = "SC1_Geometries_Intersects_GivenPolygon";
                query = spatialSelectionQryTemplate;
                query = query.replace("GIVEN_SPATIAL_LITERAL", givenPolygon);
                query = query.replace("FUNCTION", "sfIntersects");
                break;

            case 1:
                // SC2 - Intensive: Find all features that spatially intersect with other features
                label = "SC2_Intensive_Geometries_Intersect_Geometries";
                query = spatialJoinHardQryTemplate;
                query = query.replace("FUNCTION", "sfIntersects");
                break;

            case 2:
                // SC3 - Relaxed: Find all features that spatially intersect with other features
                label = "SC3_Relaxed_Geometries_Intersect_Geometries";
                query = spatialJoinEasyQryTemplate;
                query = query.replace("FUNCTION", "sfIntersects");
                break;

            default:
                logger.error("No such query number exists:" + queryIndex);
        }

        String translatedQuery = sut.translateQuery(query, label);
        return new QueryStruct(prefixes + translatedQuery, label);
    }
}