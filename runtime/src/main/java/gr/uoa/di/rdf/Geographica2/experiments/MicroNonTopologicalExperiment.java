/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2013, Pyravlos Team
 *
 */
package gr.uoa.di.rdf.Geographica2.experiments;

import gr.uoa.di.rdf.Geographica2.queries.MicroNonTopologicalQueriesSet;
import gr.uoa.di.rdf.Geographica2.systemsundertest.SystemUnderTest;

import org.apache.log4j.Logger;

/**
 * @author GeoRDFBench Creator <GeoRDFBench@Univ>
 */
public class MicroNonTopologicalExperiment extends Experiment {

	public MicroNonTopologicalExperiment(SystemUnderTest sut, int repetitions, int timeoutSecs, String logPath) {
            /*
            super(sut, repetitions, timeoutSecs, logPath);
            logger = Logger.getLogger(MicroNonTopologicalExperiment.class.getSimpleName());
            queriesSet = new MicroNonTopologicalQueriesSet(sut);
            */
            this(sut, repetitions, timeoutSecs, null, logPath);
	}

	public MicroNonTopologicalExperiment(SystemUnderTest sut, int repetitions, int timeoutSecs, int[] queriesToRun, String logPath) {
            super(sut, repetitions, timeoutSecs, queriesToRun, logPath);
            logger = Logger.getLogger(MicroNonTopologicalExperiment.class.getSimpleName());
            queriesSet = new MicroNonTopologicalQueriesSet(sut);
	}
}
