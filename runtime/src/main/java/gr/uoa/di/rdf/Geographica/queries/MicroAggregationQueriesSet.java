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
public class MicroAggregationQueriesSet  extends QueriesSet {	
	
	static Logger logger = Logger.getLogger(MicroAggregationQueriesSet.class.getSimpleName());
	
	private String strdfQueryTemplate = prefixes 
			+ "\n select (strdf:FUNCTION(?o1) as ?ret) where {  \n"
			+ "	GRAPH <GRAPH1> {?s1 ASWKT1 ?o1} \n"
			+ "}";
	
	private String geofQueryTemplate = prefixes 
			+ "\n select (geof:FUNCTION(?o1) as ?ret) where {  \n"
			+ "	GRAPH <GRAPH1> {?s1 ASWKT1 ?o1} \n"
			+ "}";

	public MicroAggregationQueriesSet(SystemUnderTest sut) {
		super(sut);
		queriesN = 2; // IMPORTANT: Add/remove queries in getQuery implies changing queriesN
	}
	
	@Override
	public int getQueriesN() { return queriesN; }
	
	@Override
	public QueryStruct getQuery(int queryIndex, int repetition) {
		
		String query = null, label = null;
		
		// IMPORTANT: Add/remove queries in getQuery implies changing queriesN and changing case numbers
		switch (queryIndex) {
			case 0:
				// Q28 Construct the extension of all polygons of GAG
                                //     (Extension of many simple Polygons)
				label = "Q28_Extent_GAG"; 
				query = strdfQueryTemplate;
				query = query.replace("GRAPH1", gadm);
				query = query.replace("ASWKT1", gadm_asWKT);
				query = query.replace("FUNCTION", "extent");
				break;
				
			case 1:
				// Q29 Construct the union of all polygons of GAG
                                //     (Union of many simple Polygons)
				label = "Q29_Union_GAG"; 
				query = geofQueryTemplate;
				query = query.replace("GRAPH1", gadm);
				query = query.replace("ASWKT1", gadm_asWKT);
				query = query.replace("FUNCTION", "union");
				break;
				
			default:
				logger.error("No such query number exists:"+queryIndex);	
		}
		
		String translatedQuery = sut.translateQuery(query, label);
		return new QueryStruct(translatedQuery, label);
	}
}
