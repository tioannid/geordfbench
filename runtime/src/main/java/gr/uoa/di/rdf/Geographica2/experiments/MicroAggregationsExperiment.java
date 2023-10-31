/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2013, Pyravlos Team
 *
 */
package gr.uoa.di.rdf.Geographica2.experiments;

import gr.uoa.di.rdf.Geographica2.queries.MicroAggregationQueriesSet;
import gr.uoa.di.rdf.Geographica2.systemsundertest.SystemUnderTest;

import org.apache.log4j.Logger;

/**
 * @author GeoRDFBench Creator <GeoRDFBench@Univ>
 */
public class MicroAggregationsExperiment extends Experiment {
	
	public MicroAggregationsExperiment(SystemUnderTest sut, int repetitions, int timeoutSecs, String logPath) {
		super(sut, repetitions, timeoutSecs, logPath);
		logger = Logger.getLogger(MicroAggregationsExperiment.class.getSimpleName());
		queriesSet = new MicroAggregationQueriesSet(sut);
	}

	public MicroAggregationsExperiment(SystemUnderTest sut, int repetitions, int timeoutSecs, int[] queriesToRun, String logPath) {
		super(sut, repetitions, timeoutSecs, queriesToRun, logPath);
		logger = Logger.getLogger(MicroAggregationsExperiment.class.getSimpleName());
		queriesSet = new MicroAggregationQueriesSet(sut);
	}
}
