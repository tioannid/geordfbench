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

/**
 * @author George Garbis <ggarbis@di.uoa.gr>
 */
public class MacroMapSearchQueriesSet extends QueriesSet {

	static Logger logger = Logger.getLogger(MacroMapSearchQueriesSet.class
			.getSimpleName());

	private int queriesN = 3; // IMPORTANT: Add/remove queries in getQuery
								// implies changing queriesN
	private List<String> names = null;
	private String currentPoint = null, boundingBox = null;
	private Random rn;

	public MacroMapSearchQueriesSet(SystemUnderTest sut) throws IOException {
		super(sut);
		
		String geonamesFile = "geonames.txt";
		names = new ArrayList<String>();
		
		InputStream is = getClass().getResourceAsStream("/"+geonamesFile);
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		String name;
		while ( (name = in.readLine()) != null ) {
			names.add(name);
		}
		in.close();
		in = null;
		is.close();
		is = null;
		
		rn  = new Random(0);
	}

	public void setCurrentPoint(String currentPoint) {
		this.currentPoint = currentPoint;
		
                /* Commented out because it works only when currentPoint is in
                   the form POINT(xxx.xx yyy.yy), but fails when the CRS URI is
                   included!!
		String[] temp = currentPoint.split("[( )]");
                */
 
                String[] temp = currentPoint.split("POINT")[1].split("[( )]");
                
		double x = Double.parseDouble(temp[1]);
		double y = Double.parseDouble(temp[2]);
		double xd=0.03;
		double yd=0.02;
		
		double x_min = x - xd;
		double x_max = x + xd;
		double y_min = y - yd;
		double y_max = y + yd;
		
		this.boundingBox = "POLYGON(("+x_min+" "+y_min+", " +
					""+x_min+" "+y_max+", " +
					""+x_max+" "+y_max+", " +
					""+x_max+" "+y_min+", " +
					""+x_min+" "+y_min+"))";
		logger.debug("PP: "+this.currentPoint);
		logger.debug("BB: "+this.boundingBox);
	}

	
	@Override
	public int getQueriesN() {
		return queriesN;
	}

	@Override
	public QueryStruct getQuery(int queryIndex, int repetition) {

		String query = null, label = null;

		// IMPORTANT: Add/remove queries in getQuery implies changing queriesN
		// and changing case numbers
		switch (queryIndex) {

		case 0:			
			String name = names.get(rn.nextInt(names.size()));
			label = "Thematic_Search"; 
			query = prefixes + "\n" + "SELECT ?f ?name ?geo ?wkt \n" + "WHERE { \n"
					+ " GRAPH <"+geonames+"> { \n"
					+ "	?f geonames:name ?name;\n"
					+ "	   "+geonames_hasGeometry+" ?geo.\n"
					+ "	?geo "+geonames_asWKT+" ?wkt.\n"
					+ " FILTER(?name = \""+name+"\"^^xsd:string) \n"
					+ " } }";
			break;

		case 1:
			label = "Get_Around_POIs"; 
			query = prefixes
					+ "\n"
					+ " SELECT ?f ?name ?fGeo ?code ?parent ?class ?fGeoWKT \n"
					+ " WHERE { \n"
					+ " GRAPH <"+geonames+"> { \n"
					+ "  ?f geonames:name ?name; \n"
					+ "     "+geonames_hasGeometry+" ?fGeo; \n"
					+ "     geonames:featureCode ?code;  \n"
					+ "     geonames:parentFeature ?parent; \n"
					+ "     geonames:featureClass ?class. \n"
					+ "  ?fGeo "+geonames_asWKT+" ?fGeoWKT. \n"
					+ "  FILTER(geof:sfIntersects(?fGeoWKT, \""+boundingBox+"\"^^<http://www.opengis.net/ont/geosparql#wktLiteral>)).\n"
					+ " } } \n";
			break;

		case 2:
			label = "Get_Around_Roads";  
			query = prefixes
					+ "\n"
					+ "SELECT ?r ?type ?label ?rGeo ?rGeoWKT \n"
					+ "WHERE { \n"
					+ " GRAPH <"+lgd+"> { \n"
					+ "	 ?r rdf:type ?type. \n"
					+ "	 OPTIONAL{ ?r rdfs:label ?label }. \n"
					+ "	 ?r "+lgd_hasGeometry+" ?rGeo. \n"
					+ "	 ?rGeo "+lgd_asWKT+" ?rGeoWKT. \n"
					+ "  FILTER(geof:sfIntersects(?rGeoWKT, \""+boundingBox+"\"^^<http://www.opengis.net/ont/geosparql#wktLiteral>)).\n"
					+ " } \n"
					+ "}";
			break;
		default:
			logger.error("No such query number exists:" + queryIndex);
		}

		String translatedQuery = sut.translateQuery(query, label);
		return new QueryStruct(translatedQuery, label);
	}

}
