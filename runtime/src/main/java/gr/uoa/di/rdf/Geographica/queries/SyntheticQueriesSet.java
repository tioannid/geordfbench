/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2013, Pyravlos Team
 *
 */
package gr.uoa.di.rdf.Geographica.queries;

import gr.uoa.di.rdf.Geographica.generators.SyntheticGenerator;
import gr.uoa.di.rdf.Geographica.systemsundertest.SystemUnderTest;

import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * @author Kostis Kyzirakos <kkyzri@di.uoa.gr>
 */
public class SyntheticQueriesSet extends QueriesSet {

	static Logger logger = Logger.getLogger(SyntheticQueriesSet.class.getSimpleName());
	int N;
	private SyntheticGenerator generator;
	
	public SyntheticQueriesSet(SystemUnderTest sut, int N) throws IOException {
		super(sut);
		queriesN = 36; // IMPORTANT: Add/remove queries in getQuery implies changing queriesN
		this.N = N;
		this.generator = new SyntheticGenerator("", N);
	}
	
	@Override
	public QueryStruct getQuery(int queryIndex, int repetition) {		
		String query = null, label = null;
		
		String[][][] queries = this.generator.generateQueries();
		double[] selectivities = this.generator.returnSelectivities();
		
		String[] intersectsSelections = queries[0][0];
		assert intersectsSelections.length == selectivities.length*2;
		String[] withinSelections = queries[2][0];
		assert withinSelections.length == selectivities.length*2;
		
		String[] intersectsJoins = queries[0][1];
		assert intersectsJoins.length == 4;		
		String[] touchesJoins = queries[1][1];
		assert touchesJoins.length == 4;		
		String[] withinJoins = queries[2][1];
		assert withinJoins.length == 4;
		
		// IMPORTANT: Add/remove queries in getQuery implies changing queriesN and changing case numbers		
		if (queryIndex >= 0 && queryIndex < selectivities.length*2) {
			label = "Synthetic_Selection_Intersects_" + ((queryIndex%2 == 0) ? "1" : this.generator.returnMaxTagValue().toString()) + "_" + selectivities[queryIndex/2];
			query = intersectsSelections[queryIndex];
		} else if (queryIndex >= selectivities.length*2 && queryIndex < selectivities.length*4) {			
			label = "Synthetic_Selection_Within_" + ((queryIndex%2 == 0) ? "1" : this.generator.returnMaxTagValue().toString()) + "_" + selectivities[queryIndex/2 - selectivities.length];
			query = withinSelections[queryIndex-selectivities.length*2];
		} 
		
		else if (queryIndex == selectivities.length*4) {
			label = "Synthetic_Join_Intersects_1_1"; query = intersectsJoins[0]; 
		} else if (queryIndex == selectivities.length*4 + 1) {
			label = "Synthetic_Join_Intersects_1_" + this.generator.returnMaxTagValue() + ""; query = intersectsJoins[1]; 
		} else if (queryIndex == selectivities.length*4 + 2) {
			label = "Synthetic_Join_Intersects_" + this.generator.returnMaxTagValue() + "_1"; query = intersectsJoins[2]; 
		} else if (queryIndex == selectivities.length*4 + 3) {
			label = "Synthetic_Join_Intersects_" + this.generator.returnMaxTagValue() + "_" + this.generator.returnMaxTagValue() + ""; query = intersectsJoins[3]; 
		}
			
		else if (queryIndex == selectivities.length*4 + 4) {
			label = "Synthetic_Join_Touches_1_1"; query = touchesJoins[0]; 
		} else if (queryIndex == selectivities.length*4 + 4 + 1) {
			label = "Synthetic_Join_Touches_1_" + this.generator.returnMaxTagValue() + ""; query = touchesJoins[1]; 
		} else if (queryIndex == selectivities.length*4 + 4 + 2) {
			label = "Synthetic_Join_Touches_" + this.generator.returnMaxTagValue() + "_1"; query = touchesJoins[2]; 
		} else if (queryIndex == selectivities.length*4 + 4 + 3) {
			label = "Synthetic_Join_Touches_" + this.generator.returnMaxTagValue() + "_" + this.generator.returnMaxTagValue() + ""; query = touchesJoins[3]; 
		}
		
		else if (queryIndex == selectivities.length*4 + 4*2) {
			label = "Synthetic_Join_Within_1_1"; query = withinJoins[0]; 
		} else if (queryIndex == selectivities.length*4 + 4*2 + 1) {
			label = "Synthetic_Join_Within_1_" + this.generator.returnMaxTagValue() + ""; query = withinJoins[1]; 
		} else if (queryIndex == selectivities.length*4 + 4*2 + 2) {
			label = "Synthetic_Join_Within_" + this.generator.returnMaxTagValue() + "_1"; query = withinJoins[2]; 
		} else if (queryIndex == selectivities.length*4 + 4*2 + 3) {
			label = "Synthetic_Join_Within_" + this.generator.returnMaxTagValue() + "_" + this.generator.returnMaxTagValue() + ""; query = withinJoins[3]; 			
		}
		
		else {
			logger.error("No such query number exists:" + queryIndex);
		}
		
		String translatedQuery = sut.translateQuery(query, label);
		return new QueryStruct(translatedQuery, label);	
	}

}
