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

import java.util.Random;

import org.apache.log4j.Logger;

/**
 * @author George Garbis <ggarbis@di.uoa.gr>
 */
public class MacroReverseGeocodingQueriesSet extends QueriesSet {

	static Logger logger = Logger.getLogger(MacroReverseGeocodingQueriesSet.class.getSimpleName());
	
	private String pointWKT = null;
	private Random rnd = new Random(0);
	private int queriesN = 2; // IMPORTANT: Add/remove queries in getQuery implies changing queriesN
	
	public MacroReverseGeocodingQueriesSet(SystemUnderTest sut) {
		super(sut);
	}

	@Override
	public int getQueriesN() { return queriesN; }
	
	@Override
	public QueryStruct getQuery(int queryIndex, int repetition) {
		
		String query = null, label = null;
		double xMin = 20.7861328125; 
		double xMax = 22.9833984375; 
		double yMin = 37.705078125; 
		double yMax = 39.990234375; 
		
		if (queryIndex == 0) {
			double x = xMin + (xMax -xMin)*rnd.nextDouble();
			double y = yMin + (yMax -yMin)*rnd.nextDouble();
			pointWKT = "POINT("+x+" "+y+")";
		}

		
		// IMPORTANT: Add/remove queries in getQuery implies changing queriesN and changing case numbers
		switch (queryIndex) {
		
			case 0:
				label = "Find_Closest_Populated_Place"; 
				query = prefixes
					+ " \n SELECT ?f (geof:distance(?cGeoWKT, \""+pointWKT+"\"^^geo:wktLiteral, <http://www.opengis.net/def/uom/OGC/1.0/metre>) as ?distance)"
					+ "WHERE { \n"
					+ " GRAPH <"+geonames+"> { \n"
					+ "  ?f geonames:featureCode geonames:P.PPL; \n"
					+ "     "+geonames_hasGeometry+" ?cGeo. \n"
					+ "  ?cGeo "+geonames_asWKT+" ?cGeoWKT. \n"
					+ " } } \n"
					+ "order by asc(?distance) \n"
					+ "limit 1 \n"
					;
				break;

			case 1:
				label = "Find_Closest_Motorway"; 
				query = prefixes
					+ " \n SELECT ?c ?type ?label (geof:distance(?cGeoWKT, \""+pointWKT+"\"^^geo:wktLiteral, <http://www.opengis.net/def/uom/OGC/1.0/metre>) as ?distance) ?cGeoWKT \n"
					+ "WHERE { \n"
					+ " GRAPH <"+lgd+"> { \n"
					+ "  ?c rdf:type lgdo:Motorway; \n"
					+ "     rdfs:label ?label; \n"
					+ "     "+lgd_hasGeometry+" ?cGeo. \n"
					+ "  ?cGeo "+lgd_asWKT+" ?cGeoWKT. \n"
					+ "} } \n"
					+ "order by asc(?distance) \n"
					+ "limit 1 \n"
					;
				break;
				
			default:
				logger.error("No such query number exists:"+queryIndex);
		}
		
		String translatedQuery = sut.translateQuery(query, label);
		return new QueryStruct(translatedQuery, label);
	}
	
}
