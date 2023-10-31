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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;

/**
 * @author George Garbis <ggarbis@di.uoa.gr>
 */
public class MacroRapidMappingQueriesSet extends QueriesSet {

	static Logger logger = Logger.getLogger(MacroRapidMappingQueriesSet.class
			.getSimpleName());

	private List<String> timestamps = null;
	private String polygonWKT, timestamp;
	private int queriesN = 6; // IMPORTANT: Add/remove queries in getQuery
								// implies changing queriesN
	Random rn;

	public MacroRapidMappingQueriesSet(SystemUnderTest sut) throws IOException {
		super(sut);
		String timestampsFile = "timestamps.txt";
		timestamps = new ArrayList<String>();
		
		InputStream is = getClass().getResourceAsStream("/"+timestampsFile);
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		String givenTimestamp;
		while ( (givenTimestamp = in.readLine()) != null ) {
			timestamps.add(givenTimestamp);
		}
		in.close();
		in = null;
		is.close();
		is = null;
		
		rn = new Random(0);
	}

	@Override
	public int getQueriesN() {
		return queriesN;
	}

	@Override
	public QueryStruct getQuery(int queryIndex, int repetition) throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException {

		String query = null, label = null;

		// IMPORTANT: Add/remove queries in getQuery implies changing queriesN
		// and changing case numbers
		switch (queryIndex) {

			case 0:
				String timestamp = timestamps.get(rn.nextInt(timestamps.size()));
				this.timestamp = timestamp.split("\t")[0];
				this.polygonWKT = timestamp.split("\t")[1];
				
				label = "Get_CLC_areas";
				query = prefixes + "\n"
					+ "SELECT ?a ?aID ?aLandUse ?aGeo ?aGeoWKT \n"
					+ "WHERE { \n"
					+ "  GRAPH <"+clc+"> { \n"
					+ "    ?a rdf:type clc:Area ; \n"
					+ "       clc:hasID ?aID ; \n"
					+ "       clc:hasLandUse ?aLandUse ; \n"
					+ "       "+clc_hasGeometry+" ?aGeo . \n"
					+ "    ?aGeo "+clc_asWKT+" ?aGeoWKT. \n"
					+ "  }"
					+ "  FILTER(geof:sfIntersects(?aGeoWKT, "+polygonWKT+"^^<http://www.opengis.net/ont/geosparql#wktLiteral>))\n"
					+ "} \n"
				;				
				break;

			case 1:
				label = "Get_highways";
				query = prefixes + "\n"
					+ "SELECT ?r ?rName ?rGeo ?rGeoWKT \n"+ "WHERE { \n"
					+ "  GRAPH <"+lgd+"> { \n"
					+ "    ?r rdf:type lgdo:HighwayThing ; \n"
					+ "       rdfs:label ?rName ; \n"
					+ "       "+lgd_hasGeometry+" ?rGeo . \n"
					+ "    ?rGeo "+lgd_asWKT+" ?rGeoWKT. \n"
					+ "  } \n"
					+ "  FILTER(geof:sfIntersects(?rGeoWKT, "+polygonWKT+"^^<http://www.opengis.net/ont/geosparql#wktLiteral>))\n"
					+ "} \n"
				;
				break;	

			case 2:
				label = "Get_municipalities";
				query = prefixes + "\n"
					 + "SELECT (geof:boundary(?gGeoWKT) as ?boundary) ?gLabel \n"
					 + "WHERE { \n"
					 + "  GRAPH <"+gadm+"> { \n"
					 + "    ?g rdf:type gag:Δήμος; \n"
					 + "       rdfs:label ?gLabel ; \n"
					 + "       "+gadm_hasGeometry+" ?gGeo . \n"
					 + "    ?gGeo "+gadm_asWKT+" ?gGeoWKT . \n"
					 + "  } \n" 
					 + "  FILTER(geof:sfIntersects(?gGeoWKT, "+polygonWKT+"^^<http://www.opengis.net/ont/geosparql#wktLiteral>))\n"
					 + "} \n" 
				;
				break;
				
			case 3:
				label = "Get_hotspots";
				query = prefixes + "\n"				
					 + "SELECT ?h ?sensor ?confidence ?producer ?satellite ?chain ?confirmation ?geomentry ?r ?wkt \n"
					 + "WHERE {  \n"
					 + "  GRAPH <"+hotspots+"> { \n"
					 + "    ?h rdf:type noa:Hotspot; \n"
					 + "       noa:isDerivedFromSensor ?sensor; \n"
					 + "       noa:hasConfidence ?confidence; \n"
					 + "       noa:isProducedBy ?producer; \n"
					 + "       noa:isDerivedFromSatellite ?satellite; \n"
					 + "       noa:producedFromProcessingChain ?chain; \n"
					 + "       noa:hasConfirmation ?confirmation ; \n"
					 + "       noa:hasAcquisitionTime "+this.timestamp+"^^xsd:dateTime; \n"
					 + "       "+hotspots_hasGeometry+" ?geomentry. \n"
					 + "    OPTIONAL {?h noa:refinedBy ?r} \n"
					 + "    FILTER(!bound(?r)) \n"
					 + "    ?geometry "+hotspots_asWKT+" ?wkt . \n"
					 + "  FILTER(geof:sfIntersects(?wkt, "+polygonWKT+"^^<http://www.opengis.net/ont/geosparql#wktLiteral>))\n"
					 + "} }\n"
				;
				break;
				
			case 4:
				label = "Get_coniferous_forests_in_fire";
				query = prefixes + "\n"
					+ "SELECT ?h ?hWKT \n"
					+ "WHERE { \n"
					+ "  GRAPH <"+hotspots+"> { \n"
					+ "    ?h  rdf:type noa:Hotspot ; \n"
					+ "        "+hotspots_hasGeometry+" ?hGeo ; \n"
					+ "        noa:hasAcquisitionTime "+this.timestamp+"^^xsd:dateTime . \n"
					+ "    ?hGeo "+hotspots_asWKT+" ?hWKT. \n"
					+ "  } \n"
					+ "  GRAPH <"+clc+"> { \n"
					+ "    ?a  a clc:Area ; \n"
					+ "        "+clc_hasGeometry+" ?aGeo ; \n"
					+ "        clc:hasLandUse clc:coniferousForest . \n"
					+ "    ?aGeo "+clc_asWKT+" ?aWKT . \n"
					+ "  } \n"
					+ "  FILTER( geof:sfIntersects(?hWKT, ?aWKT) ) . \n"
					+ "  FILTER(geof:sfIntersects(?aWKT, "+polygonWKT+"^^<http://www.opengis.net/ont/geosparql#wktLiteral>))\n"
					+ "} \n"
				;
				break;
				
			case 5:
				label = "Get_road_segments_affected_by_fire";
				query = prefixes + "\n"
					+ "SELECT ?r (geof:difference(?rWKT, ?hWKT) as ?diff) \n"
					+ "WHERE { \n"
					+ "  GRAPH <"+hotspots+"> { \n"
					+ "    ?h  rdf:type noa:Hotspot ; \n"
					+ "        "+hotspots_hasGeometry+" ?hGeo ; \n"
					+ "        noa:hasAcquisitionTime ?hAcqTime . \n"
					+ "	   ?hGeo "+hotspots_asWKT+" ?hWKT. \n"
					+ "  } \n"
					+ "  GRAPH <"+lgd+"> { \n"
					+ "    ?r  rdf:type lgdo:HighwayThing ; \n"
					+ "        "+lgd_hasGeometry+" ?rGeo . \n"
					+ "	   ?rGeo	"+lgd_asWKT+" ?rWKT. \n"
					+ "  } \n"
					+ "  FILTER( geof:sfIntersects(?rWKT, ?hWKT) ) .  \n"
					+ "  FILTER(geof:sfIntersects(?rWKT, "+polygonWKT+"^^<http://www.opengis.net/ont/geosparql#wktLiteral>))\n"
					+ "} \n"
				;
				break;
				
			default:
				logger.error("No such query number exists:" + queryIndex);
		}

		String translatedQuery = sut.translateQuery(query, label);
		return new QueryStruct(translatedQuery, label);
	}
}
	
