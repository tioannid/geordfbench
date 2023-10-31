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
public class MacroGeocodingQueriesSet extends QueriesSet {

	static Logger logger = Logger.getLogger(MacroGeocodingQueriesSet.class
			.getSimpleName());

	private List<String> newYorkAddresses = null;
	private String streetname, houseno, zip, parity;
	private int queriesN = 2; // IMPORTANT: Add/remove queries in getQuery
							   // implies changing queriesN
	Random rn;

	public MacroGeocodingQueriesSet(SystemUnderTest sut) throws IOException {
		super(sut);
		String newYorkAddressFile = "new-york-addresses.txt";
		newYorkAddresses = new ArrayList<String>();
		
		InputStream is = getClass().getResourceAsStream("/"+newYorkAddressFile);
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		String givenAddress;
		while ( (givenAddress = in.readLine()) != null ) {
			newYorkAddresses.add(givenAddress);
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
				String timestamp = newYorkAddresses.get(rn.nextInt(newYorkAddresses.size()));
				this.streetname = timestamp.split("\t")[0];
				this.houseno = timestamp.split("\t")[1];
				this.zip = timestamp.split("\t")[2];
				this.parity = timestamp.split("\t")[3];
				
				label = "Geocode_left";
				query = prefixes + "\n"
						+ "SELECT ?lfromhn ?ltohn ?wkt \n"
						+ "(("+houseno+"-?lfromhn)*((?x1-?x0)/(?ltohn-?lfromhn))+?x1 AS ?x) \n"
						+ "(("+houseno+"-?lfromhn)*((?y1-?y0)/(?ltohn-?lfromhn))+?y1 AS ?y) \n"
						+ "WHERE { \n"
						+ "	?f census:lfromhn ?lfromhn. \n"
						+ "	?f census:ltohn ?ltohn. \n"
						+ "	?f census:parityl "+parity+". \n"
						+ "	?f census:fullname "+streetname+". \n"
						+ "	?f census:zipl \""+zip+"\"^^xsd:integer. \n"
						+ "	?f census:hasGeometry ?geo. \n"
						+ "	?f census:minx ?x0. \n"
						+ "	?f census:miny ?y0. \n"
						+ "	?f census:maxx ?x1. \n"
						+ "	?f census:maxy ?y1. \n"
						+ "	?geo census:asWKT ?wkt. \n"
						+ "	FILTER( (?lfromhn <= \""+houseno+"\"^^xsd:integer && \""+houseno+"\"^^xsd:integer <= ?ltohn) \n"
						+ "		 || (?ltohn <= \""+houseno+"\"^^xsd:integer && \""+houseno+"\"^^xsd:integer <= ?lfromhn) ). \n"
						+ "} \n"

				;				
				break;

			case 1:
				label = "Geocode_right";
				query = prefixes + "\n"
						+ "SELECT ?rfromhn ?rtohn ?wkt \n"
						+ "(("+houseno+"-?rfromhn)*((?x1-?x0)/(?rtohn-?rfromhn))+?x1 AS ?x) \n"
						+ "(("+houseno+"-?rfromhn)*((?y1-?y0)/(?rtohn-?rfromhn))+?y1 AS ?y) \n"
						+ "WHERE { \n"
						+ "	?f census:rfromhn ?rfromhn. \n"
						+ "	?f census:rtohn ?rtohn. \n"
						+ "	?f census:parityr "+parity+". \n"
						+ "	?f census:fullname "+streetname+". \n"
						+ "	?f census:zipr \""+zip+"\"^^xsd:integer. \n"
						+ "	?f census:hasGeometry ?geo. \n"
						+ "	?f census:minx ?x0. \n"
						+ "	?f census:miny ?y0. \n"
						+ "	?f census:maxx ?x1. \n"
						+ "	?f census:maxy ?y1. \n"
						+ "	?geo census:asWKT ?wkt. \n"
						+ "	FILTER( (?rfromhn <= \""+houseno+"\"^^xsd:integer && \""+houseno+"\"^^xsd:integer <= ?rtohn) \n"
						+ "		 || (?rtohn <= \""+houseno+"\"^^xsd:integer && \""+houseno+"\"^^xsd:integer <= ?rfromhn) ). \n"
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
	
