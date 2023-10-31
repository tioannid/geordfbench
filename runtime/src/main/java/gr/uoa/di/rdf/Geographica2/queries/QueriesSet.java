/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2013, Pyravlos Team
 *
 */
package gr.uoa.di.rdf.Geographica2.queries;

import java.io.IOException;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import gr.uoa.di.rdf.Geographica2.systemsundertest.SystemUnderTest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author GeoRDFBench Creator <GeoRDFBench@Univ>
 */
public abstract class QueriesSet {

    public static final String gadm = "http://geographica.di.uoa.gr/dataset/gag";
    public static final String clc = "http://geographica.di.uoa.gr/dataset/clc";
    public static final String lgd = "http://geographica.di.uoa.gr/dataset/lgd";
    public static final String geonames = "http://geographica.di.uoa.gr/dataset/geonames";
    public static final String dbpedia = "http://geographica.di.uoa.gr/dataset/dbpedia";
    public static final String hotspots = "http://geographica.di.uoa.gr/dataset/hotspots";

    public static final String clc_asWKT = "<http://geo.linkedopendata.gr/corine/ontology#asWKT>";
    public static final String dbpedia_asWKT = "<http://dbpedia.org/property/asWKT>";
    public static final String gadm_asWKT = "<http://geo.linkedopendata.gr/gag/ontology/asWKT>";
    public static final String geonames_asWKT = "<http://www.geonames.org/ontology#asWKT>";
    public static final String hotspots_asWKT = "<http://teleios.di.uoa.gr/ontologies/noaOntology.owl#asWKT>";
    public static final String lgd_asWKT = "<http://linkedgeodata.org/ontology/asWKT>";
    public static final String default_asWKT = "<http://www.opengis.net/ont/geosparql#asWKT>";
    public static final Map<String, String> mapAsWKTs = new HashMap<String, String>();

    public static final String default_hasGeometry = "<http://www.opengis.net/ont/geosparql#hasGeometry>";
    public static final String clc_hasGeometry = "<http://geo.linkedopendata.gr/corine/ontology#hasGeometry>";
    public static final String dbpedia_hasGeometry = "<http://dbpedia.org/property/hasGeometry>";
    public static final String gadm_hasGeometry = "<http://geo.linkedopendata.gr/gag/ontology/hasGeometry>";
    public static final String geonames_hasGeometry = "<http://www.geonames.org/ontology#hasGeometry>";
    public static final String hotspots_hasGeometry = "<http://teleios.di.uoa.gr/ontologies/noaOntology.owl#hasGeometry>";
    public static final String lgd_hasGeometry = "<http://linkedgeodata.org/ontology/hasGeometry>";
    public static final Map<String, String> mapsHasGeometry = new HashMap<String, String>();

    static {    // initialize the maps that hold #asWKT and #hasGeometry
        mapAsWKTs.put("default", default_asWKT);
        mapAsWKTs.put("clc", clc_asWKT);
        mapAsWKTs.put("dbpedia", dbpedia_asWKT);
        mapAsWKTs.put("gadm", gadm_asWKT);
        mapAsWKTs.put("geonames", geonames_asWKT);
        mapAsWKTs.put("hotspots", hotspots_asWKT);
        mapAsWKTs.put("lgd", lgd_asWKT);

        mapsHasGeometry.put("default", default_hasGeometry);
        mapsHasGeometry.put("clc", clc_hasGeometry);
        mapsHasGeometry.put("dbpedia", dbpedia_hasGeometry);
        mapsHasGeometry.put("gadm", gadm_hasGeometry);
        mapsHasGeometry.put("geonames", geonames_hasGeometry);
        mapsHasGeometry.put("hotspots", hotspots_hasGeometry);
        mapsHasGeometry.put("lgd", lgd_hasGeometry);
    }

    public String prefixes;
    protected SystemUnderTest sut;
    protected int queriesN;

    public int getQueriesN() {
        return queriesN;
    }

    public static Map<String, String> getMapAsWKTs() {
        return mapAsWKTs;
    }

    public static Map<String, String> getMapsHasGeometry() {
        return mapsHasGeometry;
    }

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
                + "\n";
    }

    public abstract QueryStruct getQuery(int queryIndex, int repetition) throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException;

    public class QueryStruct {

        private String query;
        private String label;

        public String getQuery() {
            return query;
        }

        public String getLabel() {
            return label;
        }

        QueryStruct(String query, String label) {
            this.query = query;
            this.label = label;
        }
    }
}
