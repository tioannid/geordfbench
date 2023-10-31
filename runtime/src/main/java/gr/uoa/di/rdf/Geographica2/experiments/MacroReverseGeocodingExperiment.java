/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2013, Pyravlos Team
 *
 */
package gr.uoa.di.rdf.Geographica2.experiments;

import gr.uoa.di.rdf.Geographica2.queries.MacroReverseGeocodingQueriesSet;
import gr.uoa.di.rdf.Geographica2.systemsundertest.SystemUnderTest;

import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * @author GeoRDFBench Creator <GeoRDFBench@Univ>
 */
public class MacroReverseGeocodingExperiment extends MacroExperiment {

	public MacroReverseGeocodingExperiment(SystemUnderTest sut,
			int repetitions, int timeoutSecs, int runTimeInMinutes, String logPath)
			throws IOException {
		super(sut, repetitions, timeoutSecs, runTimeInMinutes, logPath);
		logger = Logger.getLogger(MacroReverseGeocodingExperiment.class.getSimpleName());
		queriesSet = new MacroReverseGeocodingQueriesSet(sut);
		this.runTimeInMinutes = runTimeInMinutes;
	}

	public MacroReverseGeocodingExperiment(SystemUnderTest sut,
			int repetitions, int timeoutSecs, int runTimeInMinutes, int[] queriesToRun, String logPath)
			throws IOException {
		super(sut, repetitions, timeoutSecs, runTimeInMinutes, logPath);
		logger = Logger.getLogger(MacroReverseGeocodingExperiment.class.getSimpleName());
		queriesSet = new MacroReverseGeocodingQueriesSet(sut);
		this.runTimeInMinutes = runTimeInMinutes;
		this.queriesToRun = queriesToRun;
	}
}

