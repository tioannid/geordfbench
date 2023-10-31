/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2013, Pyravlos Team
 *
 */
package gr.uoa.di.rdf.Geographica.experiments;

import gr.uoa.di.rdf.Geographica.queries.SyntheticOnlyPointsQueriesSet;
import gr.uoa.di.rdf.Geographica.systemsundertest.SystemUnderTest;

import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * @author Kostis Kyzirakos <kkyzir@di.uoa.gr>
 */
public class SyntheticOnlyPointsExperiment extends Experiment {
	
	public SyntheticOnlyPointsExperiment(SystemUnderTest sut, int repetitions, int timeoutSecs, int N, String logPath) throws IOException {
		super(sut, repetitions, timeoutSecs, logPath);
		logger = Logger.getLogger(SyntheticOnlyPointsExperiment.class.getSimpleName());
		queriesSet = new SyntheticOnlyPointsQueriesSet(sut, N);
	}

	public SyntheticOnlyPointsExperiment(SystemUnderTest sut, int repetitions, int timeoutSecs, int N, int[] queriesToRun, String logPath) throws IOException {
		super(sut, repetitions, timeoutSecs, queriesToRun, logPath);
		logger = Logger.getLogger(SyntheticOnlyPointsExperiment.class.getSimpleName());
		queriesSet = new SyntheticOnlyPointsQueriesSet(sut, N);
	}
}
