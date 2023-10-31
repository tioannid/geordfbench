/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2013, Pyravlos Team
 *
 */
package gr.uoa.di.rdf.Geographica2.queries;

import gr.uoa.di.rdf.Geographica2.queries.*;
import gr.uoa.di.rdf.Geographica2.systemsundertest.SystemUnderTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;

/**
 * @author GeoRDFBench Creator <GeoRDFBench@Univ>
 */
public class MacroComputeStatisticsQueriesSet extends QueriesSet {

	static Logger logger = Logger.getLogger(MacroComputeStatisticsQueriesSet.class
			.getSimpleName());

	private List<String> municipalities = null;
	private String municipalityWKT, municipalityName;
	private int queriesN = 3; // IMPORTANT: Add/remove queries in getQuery
								// implies changing queriesN
	Random rn;

	public MacroComputeStatisticsQueriesSet(SystemUnderTest sut) throws IOException {
		super(sut);
		String municipalitiesFile = "municipalities.txt"; // TODO
		municipalities = new ArrayList<String>();
		
		InputStream is = getClass().getResourceAsStream("/"+municipalitiesFile);
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		String givenMunicipality;
		while ( (givenMunicipality = in.readLine()) != null ) {
			municipalities.add(givenMunicipality);
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
				String line = municipalities.get(rn.nextInt(municipalities.size()));
				this.municipalityName = line.split("\t")[0];
				this.municipalityWKT = line.split("\t")[1];
				
				label = "Count_CLC_categories";
				query = prefixes + "\n"
						+ "SELECT ?clcLandUse (COUNT(DISTINCT ?clc) AS ?count) \n"
//						+ "SELECT ?clcLandUse (SUM(strdf:area(?wkt)) AS ?count) \n"
						+ "WHERE { \n"
						+ "	GRAPH ns:clc { \n"
						+ "		?clc rdf:type clc:Area. \n"
						+ "		?clc clc:hasLandUse ?clcLandUse. \n"
						+ "		?clc clc:hasGeometry ?clcGeo. \n"
						+ "		?clcGeo clc:asWKT ?clcWkt. \n"
						+ "		FILTER(geof:sfIntersects(?clcWkt, "+municipalityWKT+"^^geo:wktLiteral)). \n"
						+ "}} \n"
						+ "GROUP BY ?clcLandUse \n"
				;
				//
				break;

			case 1:
				label = "Count_GeoNames_categories";
				query = prefixes + "\n"
						+ "SELECT ?fClass ?fCode (COUNT(DISTINCT ?f) as ?count) \n"
						+ "WHERE  \n"
						+ "{ \n"
						+ "	GRAPH ns:geonames { \n"
						+ "		?f rdf:type geonames:Feature. \n"
						+ "		?f geonames:featureClass ?fClass. \n"
						+ "		?f geonames:featureCode ?fCode. \n"
						+ "		?f geonames:hasGeometry ?fGeo. \n"
						+ "		?fGeo geonames:asWKT ?fWkt. \n"
						+ "		FILTER(geof:sfIntersects(?fWkt, "+municipalityWKT+"^^geo:wktLiteral)). \n"
						+ " }} \n"
						+ "GROUP BY ?fClass ?fCode \n"
						;
//				label = "Count_DBpedia_categories";
//				query = prefixes + "\n"
//						+ "SELECT ?fClass ?fCode (COUNT(DISTINCT ?f) as ?count) \n"
//						+ "WHERE  \n"
//						+ "{ \n"
//						+ "	GRAPH ns:geonames { \n"
//						+ "		?f rdf:type geonames:Feature. \n"
//						+ "		?f geonames:featureClass ?fClass. \n"
//						+ "		?f geonames:featureCode ?fCode. \n"
//						+ "		?f geonames:hasGeometry ?fGeo. \n"
//						+ "		?fGeo geonames:asWKT ?fWkt. \n"
//						+ "		FILTER(geof:sfIntersects(?fWkt, "+municipalityWKT+"^^geo:wktLiteral)). \n"
//						+ " }} \n"
//						+ "GROUP BY ?fClass ?fCode \n"
//						;
				break;	

			case 2:
				label = "Count_GeoNames_categories_in_ContinuousUrbanFabric";
				query = prefixes + "\n"
						+ "SELECT ?fClass (COUNT(DISTINCT ?f) as ?count) \n"
//						+ "SELECT ?clcLandUse ?fClass ?fCode (COUNT(DISTINCT ?f) as ?count) \n"
						+ "WHERE { \n"
						+ "	GRAPH ns:clc { \n"
						+ "		?clc rdf:type clc:Area. \n"
//						+ "		?clc clc:hasLandUse ?clcLandUse. \n"
						+ "		?clc clc:hasLandUse clc:continuousUrbanFabric. \n" 
						+ "		?clc clc:hasGeometry ?clcGeo. \n"
						+ "		?clcGeo clc:asWKT ?clcWkt. \n"
						+ "		FILTER(geof:sfIntersects(?clcWkt, "+municipalityWKT+"^^geo:wktLiteral)). \n"
						+ "} \n"
						+ "	GRAPH ns:geonames { \n"
						+ "		?f rdf:type geonames:Feature. \n"
						+ "		?f geonames:featureClass ?fClass. \n"
//						+ "		?f geonames:featureCode ?fCode. \n"
						+ "		?f geonames:hasGeometry ?fGeo. \n"
						+ "		?fGeo geonames:asWKT ?fWkt. \n"
						+ "		FILTER(geof:sfIntersects(?fWkt, "+municipalityWKT+"^^geo:wktLiteral)). \n"
						+ " } \n"
						+ "	FILTER(geof:sfIntersects(?clcWkt, ?fWkt)). \n"
						+ "} \n"
//						+ "GROUP BY ?clcLandUse ?fClass ?fCode \n"
						+ "GROUP BY ?fClass \n"
				;
				break;
			default:
				logger.error("No such query number exists:" + queryIndex);
		}

		String translatedQuery = sut.translateQuery(query, label);
		return new QueryStruct(translatedQuery, label);
	}

}
	
