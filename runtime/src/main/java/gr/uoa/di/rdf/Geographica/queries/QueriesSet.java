/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2013, Pyravlos Team
 *
 */
package gr.uoa.di.rdf.Geographica.queries;

import java.io.IOException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import gr.uoa.di.rdf.Geographica.systemsundertest.SystemUnderTest;

/**
 * @author George Garbis <ggarbis@di.uoa.gr>
 */
public abstract class QueriesSet {

	// gadm for gag
	public static final String gadm     = "http://geographica.di.uoa.gr/dataset/gag";
	public static final String clc      = "http://geographica.di.uoa.gr/dataset/clc";
	public static final String lgd      = "http://geographica.di.uoa.gr/dataset/lgd";
	public static final  String geonames = "http://geographica.di.uoa.gr/dataset/geonames";
	public static final  String dbpedia  = "http://geographica.di.uoa.gr/dataset/dbpedia"; 
	public static final  String hotspots = "http://geographica.di.uoa.gr/dataset/hotspots"; 
	
	public static String clc_asWKT;
	public static String dbpedia_asWKT;
	// gadm for gag
	public static String gadm_asWKT;
	public static String geonames_asWKT;
	public static String hotspots_asWKT;
	public static String lgd_asWKT;

	public static String clc_hasGeometry, dbpedia_hasGeometry, gadm_hasGeometry,
			  geonames_hasGeometry, hotspots_hasGeometry, lgd_hasGeometry;
	
	public String prefixes;
	protected SystemUnderTest sut;
	protected int queriesN;
	
	public int getQueriesN() { return queriesN; }
	
	public QueriesSet(SystemUnderTest sut) {
		
		this.sut = sut;

		prefixes = "PREFIX clc: <http://geo.linkedopendata.gr/corine/ontology#> \n"
				+ "PREFIX noa: <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#> \n"
				+ "PREFIX gadm: <http://www.gadm.org/ontology#> \n"
				+ "PREFIX lgdo: <http://linkedgeodata.org/ontology/> \n"
				+ "PREFIX geonames: <http://www.geonames.org/ontology#> \n"
				+ "PREFIX gag: <http://geo.linkedopendata.gr/gag/ontology/> \n"
				+ "PREFIX lgdp: <http://linkedgeodata.org/property/> \n"
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" 
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
				+ "PREFIX strdf: <http://strdf.di.uoa.gr/ontology#> \n"
				+ "PREFIX geof: <http://www.opengis.net/def/function/geosparql/> \n"
				+ "PREFIX geo: <http://www.opengis.net/ont/geosparql#> \n" 
				+ "PREFIX geo-sf: <http://www.opengis.net/ont/sf#> \n" 
				+ "PREFIX ext: <http://rdf.useekm.com/ext#> \n"
				+ "PREFIX dbpedia: <http://dbpedia.org/property/> \n"
				+ "PREFIX ns: <http://geographica.di.uoa.gr/dataset/> \n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n"
				+ "PREFIX census: <http://geographica.di.uoa.gr/cencus/ontology#> \n"
				+ "\n"
				;

		clc_asWKT = "<http://geo.linkedopendata.gr/corine/ontology#asWKT>";
		dbpedia_asWKT = "<http://dbpedia.org/property/asWKT>";
		// gadm for gag
		gadm_asWKT = "<http://geo.linkedopendata.gr/gag/ontology/asWKT>";
		geonames_asWKT = "<http://www.geonames.org/ontology#asWKT>";
		hotspots_asWKT = "<http://teleios.di.uoa.gr/ontologies/noaOntology.owl#asWKT>";
		lgd_asWKT = "<http://linkedgeodata.org/ontology/asWKT>";

		clc_hasGeometry = "<http://geo.linkedopendata.gr/corine/ontology#hasGeometry>";
		dbpedia_hasGeometry = "<http://dbpedia.org/property/hasGeometry>";
		// gadm for gag
		gadm_hasGeometry = "<http://geo.linkedopendata.gr/gag/ontology/hasGeometry>";
		geonames_hasGeometry = "<http://www.geonames.org/ontology#hasGeometry>";
		hotspots_hasGeometry = "<http://teleios.di.uoa.gr/ontologies/noaOntology.owl#hasGeometry>";
		lgd_hasGeometry = "<http://linkedgeodata.org/ontology/hasGeometry>";
	}
	
	public abstract QueryStruct getQuery(int queryIndex, int repetition) throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException;
	
	public class QueryStruct {
		private String query;
		private String label;
		
		public String getQuery() { return query; }

		public String getLabel() { return label; }
		
		QueryStruct(String query, String label) {this.query=query; this.label=label;}
	}
}
