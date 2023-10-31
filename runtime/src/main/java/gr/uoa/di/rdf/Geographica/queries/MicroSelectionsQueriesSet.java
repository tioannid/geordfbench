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
 * @author George Garbis <ggarbis@di.uoa.gr>
 */
public class MicroSelectionsQueriesSet extends QueriesSet {

	static Logger logger = Logger.getLogger(MicroSelectionsQueriesSet.class.getSimpleName());
	
	// Template to create queries
	private final String queryTemplate = prefixes 
			+ "\n select ?s1 ?o1 where { \n"
			+ "	GRAPH <GRAPH1> {?s1 ASWKT1 ?o1} \n"
			+ "  FILTER(geof:FUNCTION(?o1, GIVEN_SPATIAL_LITERAL)). " 
			+ "}  \n" 
	;

	private String givenPolygonFile = "givenPolygon.txt";
	private String givenLinesFile = "givenLine.txt";
	private String givenPolygon;
	private String givenLine, givenLine2, givenLine3;
	private String givenPoint;
	private String givenRadius;
	
	public MicroSelectionsQueriesSet(SystemUnderTest sut) throws IOException {
		super(sut);
		queriesN = 11; // IMPORTANT: Add/remove queries in getQuery implies changing queriesN
		
		String spatialDatatype = "<http://www.opengis.net/ont/geosparql#wktLiteral>";
		givenPoint = "\"POINT(23.71622 37.97945)\"^^"+spatialDatatype;
		givenRadius = "3000";
		
		InputStream is = getClass().getResourceAsStream("/"+givenPolygonFile);
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		givenPolygon = in.readLine();
		givenPolygon = "\""+givenPolygon+"\"^^"+spatialDatatype;
		in.close();
		in = null;
		is.close();
		is = null;
		is = getClass().getResourceAsStream("/"+givenLinesFile);
		in = new BufferedReader(new InputStreamReader(is));
		// <http://linkedgeodata.org/geometry/way168092715>
		givenLine = in.readLine();
		givenLine = "\""+givenLine+"\"^^"+spatialDatatype;
		// <http://linkedgeodata.org/geometry/way31642973>
		givenLine2 = in.readLine();
		givenLine2 = "\""+givenLine2+"\"^^"+spatialDatatype;
		// <http://linkedgeodata.org/geometry/way45476887>
		givenLine3 = in.readLine();
		givenLine3 = "\""+givenLine3+"\"^^"+spatialDatatype;
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

		// -- Equals -- //
		case 0:
			// Q7 Find all lines of LGD that are spatially equal with a given line
			label = "Q7_Equals_LGD_GivenLine"; 
			query = queryTemplate;
			query = query.replace("GRAPH1", lgd);
			query = query.replace("ASWKT1", lgd_asWKT);
			query = query.replace("GIVEN_SPATIAL_LITERAL", givenLine);
			query = query.replace("FUNCTION", "sfEquals");
			break;
	
		case 1:
			// Q8 Find all polygons of GAG that are spatially equal a given polygon
			label = "Q8_Equals_GAG_GivenPolygon"; 
			query = queryTemplate;
			query = query.replace("GRAPH1", gadm);
			query = query.replace("ASWKT1", gadm_asWKT);
			query = query.replace("GIVEN_SPATIAL_LITERAL", givenPolygon);
			query = query.replace("FUNCTION", "sfEquals");
			break;
	
		case 2:
			// Q9 Find all lines of LGD that spatially intersect with a given polygon
			label = "Q9_Intersects_LGD_GivenPolygon"; 
			query = queryTemplate;
			query = query.replace("GRAPH1", lgd);
			query = query.replace("ASWKT1", lgd_asWKT);
			query = query.replace("GIVEN_SPATIAL_LITERAL", givenPolygon);
			query = query.replace("FUNCTION", "sfIntersects");
			break;
	
		case 3:
			// Q10 Find all polygons of CLC that spatially intersect with a given line
			label = "Q10_Intersects_CLC_GivenLine"; 
			query = queryTemplate;
			query = query.replace("GRAPH1", clc);
			query = query.replace("ASWKT1", clc_asWKT);
			query = query.replace("GIVEN_SPATIAL_LITERAL", givenLine2);
			query = query.replace("FUNCTION", "sfIntersects");
			break;
	
		case 4:
                        // Q11 Find all polygons of CLC that spatially overlap with a given polygon
			label = "Q11_Overlaps_CLC_GivenPolygon"; 
			query = queryTemplate;
			query = query.replace("GRAPH1", clc);
			query = query.replace("ASWKT1", clc_asWKT);
			query = query.replace("GIVEN_SPATIAL_LITERAL", givenPolygon);
			query = query.replace("FUNCTION", "sfOverlaps");
			break;

		case 5:
                        // Q12 Find all lines of LGD that spatially cross a given line
			label = "Q12_Crosses_LGD_GivenLine"; 
			query = queryTemplate;
			query = query.replace("GRAPH1", lgd);
			query = query.replace("ASWKT1", lgd_asWKT);
			query = query.replace("GIVEN_SPATIAL_LITERAL", givenLine3);
			query = query.replace("FUNCTION", "sfCrosses");
			break;
			
		case 6:
                        // Q13 Find all points of GeoNames that are contained in a given polygon
			label = "Q13_Within_GeoNames_GivenPolygon";// times = 345699520 + 840787868 = 1186487388, 136
			query = queryTemplate;
			query = query.replace("GRAPH1", geonames);
			query = query.replace("ASWKT1", geonames_asWKT);
			query = query.replace("GIVEN_SPATIAL_LITERAL", givenPolygon);
			query = query.replace("FUNCTION", "sfWithin");
			break;
	
		case 7:
                        // Q14 Find all points of GeoNames that are contained in the buffer of a given point
			label = "Q14_Within_GeoNames_Point_Buffer";
			query = prefixes + "\n select ?s1 where { \n"
					+ "	GRAPH <" + geonames	+ "> {?s1 "+geonames_asWKT+" ?o1} \n"
					+ " FILTER(geof:sfWithin(?o1, geof:buffer("
					+ givenPoint + ", " + givenRadius + ", <http://www.opengis.net/def/uom/OGC/1.0/metre>"
					+ "))).  \n" 
					+ "} ";
			break;
	
		case 8:
                        // Q15 Find all points of GeoNames that are within specific distance from a given point
			label = "Q15_GeoNames_Point_Distance";
			query = prefixes + "\n select ?s1 where { \n" 
					+ "	GRAPH <" + geonames	+ "> {?s1 "+geonames_asWKT+" ?o1} \n" 
					+ "  FILTER(geof:distance(?o1, "+ givenPoint + ", <http://www.opengis.net/def/uom/OGC/1.0/metre>) <= " + givenRadius + ").  \n" 
					+ "} ";
			break;
		case 9:
			// Q16 Find all points of GeoNames that are spatially disjoint of a given polygon
			label = "Q16_Disjoint_GeoNames_GivenPolygon";
			query = queryTemplate;
			query = query.replace("GRAPH1", geonames);
			query = query.replace("ASWKT1", geonames_asWKT);
			query = query.replace("GIVEN_SPATIAL_LITERAL", givenPolygon);
			query = query.replace("FUNCTION", "sfDisjoint");
			break;
		case 10:
			// Q17 Find all lines of LGD that are spatially disjoint of a given polygon
			label = "Q17_Disjoint_LGD_GivenPolygon";
			query = queryTemplate;
			query = query.replace("GRAPH1", lgd);
			query = query.replace("ASWKT1", lgd_asWKT);
			query = query.replace("GIVEN_SPATIAL_LITERAL", givenPolygon);
			query = query.replace("FUNCTION", "sfDisjoint");
			break;
			
		default:
			logger.error("No such query number exists:"+queryIndex);
		}
	
		String translatedQuery = sut.translateQuery(query, label);
		return new QueryStruct(translatedQuery, label);
	}
}
