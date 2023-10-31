/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2013, Pyravlos Team
 *
 */
package gr.uoa.di.rdf.Geographica.experiments;

import gr.uoa.di.rdf.Geographica.queries.MicroJoinsQueriesSet;
import gr.uoa.di.rdf.Geographica.systemsundertest.SystemUnderTest;

import org.apache.log4j.Logger;

/**
 * @author George Garbis <ggarbis@di.uoa.gr>
 */
public class MicroJoinsExperiment extends Experiment {
		   
	public MicroJoinsExperiment(SystemUnderTest sut, int repetitions, int timeoutSecs, String logPath) {
		super(sut, repetitions, timeoutSecs, logPath);
		logger = Logger.getLogger(MicroJoinsExperiment.class.getSimpleName());
		queriesSet = new MicroJoinsQueriesSet(sut);

	}
	
	public MicroJoinsExperiment(SystemUnderTest sut, int repetitions, int timeoutSecs, int[] queriesToRun, String logPath) {
		super(sut, repetitions, timeoutSecs, queriesToRun, logPath);
		logger = Logger.getLogger(MicroJoinsExperiment.class.getSimpleName());
		queriesSet = new MicroJoinsQueriesSet(sut);

	}
}
