/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2013, Pyravlos Team
 *
 */
package gr.uoa.di.rdf.Geographica2.experiments;

import gr.uoa.di.rdf.Geographica2.queries.SyntheticQueriesSet;
import gr.uoa.di.rdf.Geographica2.systemsundertest.SystemUnderTest;

import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * @author Kostis Kyzirakos <kkyzir@di.uoa.gr>
 */
public class SyntheticExperiment extends Experiment {
	
	public SyntheticExperiment(SystemUnderTest sut, int repetitions, int timeoutSecs, int N, String logPath) throws IOException {
		super(sut, repetitions, timeoutSecs, logPath);
		logger = Logger.getLogger(SyntheticExperiment.class.getSimpleName());
		queriesSet = new SyntheticQueriesSet(sut, N);
	}

	public SyntheticExperiment(SystemUnderTest sut, int repetitions, int timeoutSecs, int N, int[] queriesToRun, String logPath) throws IOException {
		super(sut, repetitions, timeoutSecs, queriesToRun, logPath);
		logger = Logger.getLogger(SyntheticExperiment.class.getSimpleName());
		queriesSet = new SyntheticQueriesSet(sut, N);
	}
}
