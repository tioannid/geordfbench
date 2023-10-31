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
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import org.apache.commons.io.FilenameUtils;

/**
 * @author GeoRDFBench Creator <GeoRDFBench@Univ>
 */
public class PreGeneratedSyntheticQueriesSet extends QueriesSet {

    static Logger logger = Logger.getLogger(PreGeneratedSyntheticQueriesSet.class.getSimpleName());

    // Data Members 
    SortedMap<String, String> queryMap = new TreeMap<>(); // Map to hold queries
    SortedMap<String, String> queryLabelMap = new TreeMap<>(); // Map to hold query labels

    public PreGeneratedSyntheticQueriesSet(SystemUnderTest sut, String queryDir) throws IOException {
        super(sut);
        this.prefixes = ""; // pregenerated synthetic querysets will have their own prefixes
        // read queries from the queryDir folder
        File dir = new File(queryDir);
        File[] directoryListing = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.endsWith(".qry"));
            }
        });
        String qryFileName;
        String qryIndexStr, qryText;
        FileReader fr;
        BufferedReader br;
        StringBuffer sb;
        if (directoryListing != null) {
            queriesN = directoryListing.length;
            // Queries are expected to be in the following format
            // Q00_Synthetic_Selection_Intersects_Landownerships_1_1.0.qry
            for (File queryFile : directoryListing) {
                qryText = "";
                // get query file name
                qryFileName = queryFile.getName();
                // retrieve query index by isolating substring 2-3
                qryIndexStr = String.valueOf(Integer.parseInt(qryFileName.substring(1, 3)));
                // add a map entry for query label
                queryLabelMap.put(qryIndexStr, FilenameUtils.removeExtension(qryFileName));
                // read the query contained inside the query file
                fr = new FileReader(queryFile);
                br = new BufferedReader(fr);
                sb = new StringBuffer();
                while ((qryText = br.readLine()) != null) {
                    sb.append(qryText);
                    sb.append("\n");
                }
                fr.close();
                // add a map entry
                queryMap.put(qryIndexStr, sb.toString());
            }
        } else {
            logger.error("No queries found in directory " + queryDir);
        }

    }

    @Override
    public QueryStruct getQuery(int queryIndex, int repetition) {
        String queryIndexStr = String.valueOf(queryIndex);
        String label = queryLabelMap.get(queryIndexStr);
        String translatedQuery = sut.translateQuery(queryMap.get(queryIndexStr), label);
        return new QueryStruct(translatedQuery, label);
    }

}
