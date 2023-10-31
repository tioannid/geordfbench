/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2013, Pyravlos Team
 *
 */
package gr.uoa.di.rdf.Geographica2.queries;

import gr.uoa.di.rdf.Geographica.generators.SyntheticGenerator;
import gr.uoa.di.rdf.Geographica2.systemsundertest.SystemUnderTest;
import java.io.IOException;
import org.apache.log4j.Logger;

/**
 * @author Kostis Kyzirakos <kkyzri@di.uoa.gr>
 */
public class SyntheticOnlyPointsQueriesSet extends QueriesSet {

	static Logger logger = Logger.getLogger(SyntheticOnlyPointsQueriesSet.class.getSimpleName());
	int N;
	private SyntheticGenerator generator;
        private String[] queries;
        private double[] selectivities;
	
	public SyntheticOnlyPointsQueriesSet(SystemUnderTest sut, int N) throws IOException {
		super(sut);
		this.N = N;
		this.generator = new SyntheticGenerator("", N);
                // according to "7.2.2. Queries" section of the paper
                // only spatial selections using topological relation 
                // geof:sfWithin were run
                this.queries = this.generator.generatePointQueries()[0][0];
                this.selectivities = this.generator.returnSelectivities();
		queriesN = queries.length; // IMPORTANT: Add/remove queries in getQuery implies changing queriesN
	}
	
	@Override
	public QueryStruct getQuery(int queryIndex, int repetition) {		
		String query = null, label = null;

		// IMPORTANT: Add/remove queries in getQuery implies changing queriesN and changing case numbers		
		if (queryIndex >= 0 && queryIndex < queries.length) {
			label = "Synthetic_Selection_Distance_" + ((queryIndex%2 == 0) ? "1" : this.generator.returnMaxTagValue().toString()) + "_" + selectivities[queryIndex/2];
			query = queries[queryIndex];
		} else {
			logger.error("No such query number exists:" + queryIndex);
		}
	
		String translatedQuery = sut.translateQuery(query, label);
		return new QueryStruct(translatedQuery, label);
	}

}
