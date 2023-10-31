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

import org.apache.log4j.Logger;

/**
 * @author George Garbis <ggarbis@di.uoa.gr>
 */
public class MicroJoinsQueriesSet extends QueriesSet {

	static Logger logger = Logger.getLogger(MicroJoinsQueriesSet.class.getSimpleName());
		
	public  MicroJoinsQueriesSet(SystemUnderTest sut) {
		super(sut);
		queriesN = 10; // IMPORTANT: Add/remove queries in getQuery implies changing queriesN
	}

	@Override
	public int getQueriesN() { return queriesN; }
	
	private String queryTemplate = prefixes 
			+ "\n select ?s1 ?s2 where { \n"
			+ "	GRAPH <GRAPH1> {?s1 ASWKT1 ?o1} \n"
			+ "	GRAPH <GRAPH2> {?s2 ASWKT2 ?o2} \n"
			+ "  FILTER(geof:FUNCTION(?o1, ?o2)).  \n"
			+ "} \n";
	
	private String queryTemplate2 = prefixes 
			+ "\n select ?s1 ?s2 where { \n"
			+ "	GRAPH <GRAPH1> {?s1 ASWKT1 ?o1} \n"
			+ "	GRAPH <GRAPH2> {?s2 ASWKT2 ?o2} \n"
			+ " FILTER(?s1 != ?s2).  \n"
			+ " FILTER(geof:FUNCTION(?o1, ?o2)).  \n"
			+ "} \n";

	@Override
	public QueryStruct getQuery(int queryIndex, int repetition) {
	
		String query = null, label = null;
		
		// IMPORTANT: Add/remove queries in getQuery implies changing queriesN and changing case numbers
		switch (queryIndex) {
			// -- Equals -- //
			case 0:			
				// Q18 Find all points of GeoNames that are spatially equal with a point of DBpedia
				label = "Q18_Equals_GeoNames_DBPedia";
				query = queryTemplate;
				query = query.replace("GRAPH1", geonames);
				query = query.replace("ASWKT1", geonames_asWKT);
				query = query.replace("GRAPH2", dbpedia);
				query = query.replace("ASWKT2", dbpedia_asWKT);
				query = query.replace("FUNCTION", "sfEquals");
				break;

			case 1:
				// Q19 Find all points of GeoNames that spatially intersect a line of LGD
                                //     (POIS of GeoNames reached by road)
				label = "Q19_Intersects_GeoNames_LGD"; 
				query = queryTemplate;
				query = query.replace("GRAPH1", geonames);
				query = query.replace("ASWKT1", geonames_asWKT);
				query = query.replace("GRAPH2", lgd);
				query = query.replace("ASWKT2", lgd_asWKT);
				query = query.replace("FUNCTION", "sfIntersects");
				break;

			case 2:
				// Q20 Find all points of GeoNames that spatially intersect a polygon of GAG
                                //     (POIS of GeoNames in an area)
				label = "Q20_Intersects_GeoNames_GAG"; 
				query = queryTemplate;
				query = query.replace("GRAPH1", geonames);
				query = query.replace("ASWKT1", geonames_asWKT);
				query = query.replace("GRAPH2", gadm);
				query = query.replace("ASWKT2", gadm_asWKT);
				query = query.replace("FUNCTION", "sfIntersects");
				break;
				
			case 3:
				// Q21 Find all lines of LGD that spatially intersect a polygon of GAG
                                //     (Roads of an area)
				label = "Q21_Intersects_LGD_GAG"; 
				query = queryTemplate;
				query = query.replace("GRAPH1", lgd);
				query = query.replace("ASWKT1", lgd_asWKT);
				query = query.replace("GRAPH2", gadm);
				query = query.replace("ASWKT2", gadm_asWKT);
				query = query.replace("FUNCTION", "sfIntersects");
				break;
				
			case 4:
				// Q22 Find all points of GeoNames that are within a polygon of GAG
                                //     (POIS of GeoNames inside an area)
				label = "Q22_Within_GeoNames_GAG"; 
				query = queryTemplate;
				query = query.replace("GRAPH1", geonames);
				query = query.replace("ASWKT1", geonames_asWKT);
				query = query.replace("GRAPH2", gadm);
				query = query.replace("ASWKT2", gadm_asWKT);
				query = query.replace("FUNCTION", "sfWithin");
				break;
	
			case 5:
				// Q23 Find all lines of LGD that are within a polygon of GAG
                                //     (Roads within an area)
				label = "Q23_Within_LGD_GAG"; 
				query = queryTemplate;
				query = query.replace("GRAPH1", lgd);
				query = query.replace("ASWKT1", lgd_asWKT);
				query = query.replace("GRAPH2", gadm);
				query = query.replace("ASWKT2", gadm_asWKT);
				query = query.replace("FUNCTION", "sfWithin");
				break;
				
			case 6:
				// Q24 Find all polygons of CLC that are within a polygon of GAG
                                //     (Areas contained in a country)
				label = "Q24_Within_CLC_GAG";
				query = queryTemplate;
				query = query.replace("GRAPH1", clc);
				query = query.replace("ASWKT1", clc_asWKT);
				query = query.replace("GRAPH2", gadm);
				query = query.replace("ASWKT2", gadm_asWKT);
				query = query.replace("FUNCTION", "sfWithin");
				break;
				
			case 7:
				// Q25 Find all lines of LGD that spatially cross a polygon of GAG
                                //     (Roads leaving/reaching an area)
				label = "Q25_Crosses_LGD_GAG"; 
				query = queryTemplate;
				query = query.replace("GRAPH1", lgd);
				query = query.replace("ASWKT1", lgd_asWKT);
				query = query.replace("GRAPH2", gadm);
				query = query.replace("ASWKT2", gadm_asWKT);
				query = query.replace("FUNCTION", "sfCrosses");
				break;
				
			case 8:
				// Q26 Find all polygons of GAG that spatially touch other polygons of GAG
                                //     (Countries with sharing borders)
				label = "Q26_Touches_GAG_GAG"; 
				query = queryTemplate2;
				query = query.replace("GRAPH1", gadm);
				query = query.replace("ASWKT1", gadm_asWKT);
				query = query.replace("GRAPH2", gadm);
				query = query.replace("ASWKT2", gadm_asWKT);
				query = query.replace("FUNCTION", "sfTouches");
				break;

			case 9:
				// Q27 Find all polygons of CLC that spatially overlap polygons of GAG
                                //     (Areas overlaping countries)
				label = "Q27_Overlaps_GAG_CLC"; 
				query = queryTemplate;
				query = query.replace("GRAPH1", gadm);
				query = query.replace("ASWKT1", gadm_asWKT);
				query = query.replace("GRAPH2", clc);
				query = query.replace("ASWKT2", clc_asWKT);
				query = query.replace("FUNCTION", "sfOverlaps");
				break;
	
			default:
				logger.error("No such query number exists:"+queryIndex);
		}
		
		String translatedQuery = sut.translateQuery(query, label);
		return new QueryStruct(translatedQuery, label);
	}
}
