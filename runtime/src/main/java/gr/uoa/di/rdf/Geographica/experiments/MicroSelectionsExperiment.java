/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2013, Pyravlos Team
 *
 */
package gr.uoa.di.rdf.Geographica.experiments;

import java.io.IOException;

import gr.uoa.di.rdf.Geographica.queries.MicroSelectionsQueriesSet;
import gr.uoa.di.rdf.Geographica.systemsundertest.SystemUnderTest;

import org.apache.log4j.Logger;

/**
 * @author George Garbis <ggarbis@di.uoa.gr>
 */
public class MicroSelectionsExperiment extends Experiment {
	
	public MicroSelectionsExperiment(SystemUnderTest sut, int repetitions, int timeoutSecs, String logPath) throws IOException {
		super(sut, repetitions, timeoutSecs, logPath);
		logger = Logger.getLogger(MicroSelectionsExperiment.class.getSimpleName());
		queriesSet = new MicroSelectionsQueriesSet(sut);
	}

	public MicroSelectionsExperiment(SystemUnderTest sut, int repetitions, int timeoutSecs, int[] queriesToRun, String logPath) throws IOException {
		super(sut, repetitions, timeoutSecs, queriesToRun, logPath);
		logger = Logger.getLogger(MicroSelectionsExperiment.class.getSimpleName());
		queriesSet = new MicroSelectionsQueriesSet(sut);
	}
}
