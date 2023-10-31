/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2013, Pyravlos Team
 *
 */
package gr.uoa.di.rdf.Geographica2.experiments;

import gr.uoa.di.rdf.Geographica2.queries.QueriesSet;
import gr.uoa.di.rdf.Geographica2.queries.QueriesSet.QueryStruct;
import gr.uoa.di.rdf.Geographica2.systemsundertest.SystemUnderTest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

/**
 * @author GeoRDFBench Creator <GeoRDFBench@Univ>
 */
public abstract class Experiment {

	static protected Logger logger;
	
	public int repetitions;
	public int timeoutSecs;
	public final String logPath;

	protected QueriesSet queriesSet = null;

	protected SystemUnderTest sut = null;
	protected long[][][] warmruns = null;
	protected long[][][] coldruns = null;
	protected int[] queriesToRun = null;
	protected int queriesToRunN;
	
	Experiment(SystemUnderTest sut, int repetitions, int timeoutSecs, String logPath) {
		this.sut = sut;
		this.repetitions = repetitions;
		this.timeoutSecs = timeoutSecs;
		this.logPath = logPath;
	}
	
	Experiment(SystemUnderTest sut, int repetitions, int timeoutSecs, int[] queriesToRun, String logPath) {
		this.sut = sut;
		this.repetitions = repetitions;
		this.timeoutSecs = timeoutSecs;
		this.logPath = logPath;
		this.queriesToRun = queriesToRun;
	}
	
	public QueriesSet getQueriesSet() {
		return queriesSet;
	}
	
	public void run() {
		QueryStruct queryStruct = null;
		int queriesN;
		int queryI;
		

		queriesN = queriesSet.getQueriesN();
		if (queriesToRun == null) {
			queriesToRunN = queriesSet.getQueriesN();
		} else {
			queriesToRunN = queriesToRun.length;
		}
		
		// Initialize cold runs
		coldruns = new long[queriesN][repetitions][4];
		for (int i=0; i<queriesN; i++) {
			for (int j=0; j<repetitions; j++) {
				coldruns[i][j][0] = timeoutSecs + 1;  
				coldruns[i][j][1] = timeoutSecs + 1; 
				coldruns[i][j][2] = timeoutSecs + 1;
				coldruns[i][j][3] = -1; 
			}
		}
					
		// cold runs
		sut.clearCaches();
		logger.info("Clear caches before cold runs");

		// Run first all repetitions of each query so stats can be printed
		for (int i = 0; i < queriesToRunN; i++) {
			
			if (queriesToRun == null) {
				queryI = i;
			} else {
				queryI = queriesToRun[i];
			}
			
			// KK 
			// hack
//			if (sut instanceof VirtuosoSUT) {
//				if (this instanceof MicroSelectionsExperiment) {
//					if (!(queryI == 7 || queryI == 8))
//						continue;
//				} else if (this instanceof MicroJoinsExperiment) {
//					if (!(queryI == 0))
//						continue;
//				} else {
//					continue;
//				}
//			} 
			// KK
			
			int repetitionI = 0;
			try {
				for (repetitionI = 0; repetitionI < repetitions; repetitionI++) {
					queryStruct = queriesSet.getQuery(queryI, repetitionI);

					if (queryStruct.getQuery() == null) {
						logger.warn("Query '"+queryStruct.getLabel()+"' is not defined");
						continue;
					}
					
					logger.info("Executing query ["+timeoutSecs+"] (cold, "
							+ queryI + ", " + repetitionI + "): \n"
							+ queryStruct.getQuery());

					sut.initialize();
					coldruns[queryI][repetitionI] = sut.runQueryWithTimeout(queryStruct.getQuery(), timeoutSecs);
					logger.info("Query executed (cold, "
									+ queryI + ", "	+ repetitionI + "): "
									+ coldruns[queryI][repetitionI][0] + " + "
									+ coldruns[queryI][repetitionI][1] + " = "
									+ coldruns[queryI][repetitionI][2] + ", "
									+ coldruns[queryI][repetitionI][3]);
					sut.close();
					sut.clearCaches();

					// If query times out
					if ( coldruns[queryI][repetitionI][3] == -1 ) {
						break;
					}
				}
			} catch (Exception e) {
				logger.error("While evaluating query(cold, " 
						+ queryI + ", " + repetitionI + ")");
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				String stacktrace = sw.toString();
				logger.error(stacktrace);
				sut.close();
				sut.clearCaches();
				sut.initialize();
			}

			try {
				printStatistics(this.getClass().getSimpleName(), "cold", queryI,
						queryStruct, coldruns);
			} catch (IOException e) {
				logger.error("While printing statistics (cold, " + queryI + ")");
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				String stacktrace = sw.toString();
				logger.error(stacktrace);
			}
		}

		// Initialize warm runs
		warmruns = new long[queriesN][repetitions][4];
		for (int i=0; i<queriesN; i++) {
			for (int j=0; j<repetitions; j++) {
				warmruns[i][j][0] = timeoutSecs + 1;  
				warmruns[i][j][1] = timeoutSecs + 1; 
				warmruns[i][j][2] = timeoutSecs + 1;
				warmruns[i][j][3] = -1; 
			}
		}
		
		// warm runs
		// Run first all repetitions of each query so stats can be printed
		for (int i = 0; i < queriesToRunN; i++) {
		
			if (queriesToRun == null) {
				queryI = i;
			} else {
				queryI = queriesToRun[i];
			}
			// KK 
			// hack
//			if (sut instanceof VirtuosoSUT) {
//				if (this instanceof MicroSelectionsExperiment) {
//					if (!(queryI == 7 || queryI == 8))
//						continue;
//				} else if (this instanceof MicroJoinsExperiment) {
//					if (!(queryI == 0))
//						continue;
//				} else {
//					continue;
//				}
//			} 
			// KK

			// If cold run has timed out then skip warm run as well
			if ( coldruns[queryI][0][0] > (long)timeoutSecs*1000000000 ) {
				logger.info("Skip warm run of query "+queryI+" because cold has timed out("+coldruns[queryI][0][0]+">"+(long)timeoutSecs*1000000000);
				continue;
			}
			
			int repetitionI = 0;
			try {
				sut.clearCaches();
				sut.initialize();
				queryStruct = queriesSet.getQuery(queryI, 0);
				// warm up caches
				logger.info("Warming query (warm, "
						+ queryI + ", " + repetitionI + "): "
						+ queryStruct.getQuery());

				long [] tempTimes = sut.runQueryWithTimeout(queryStruct.getQuery(), timeoutSecs);

				if ( tempTimes[0] > (long)timeoutSecs*1000000000 ) {
					logger.info("Skip warm run of query "+queryI+" because cold has timed out("+tempTimes[0]+">"+(long)timeoutSecs*1000000000);
					sut.close();
					continue;
				}

				logger.info("Query warmed (warm, "
						+ queryI + "," + repetitionI + "): "
						+ tempTimes[0] + " + "
						+ tempTimes[1] + " = "
						+ tempTimes[2] + ", "
						+ tempTimes[3]);
				
				for (repetitionI = 0; repetitionI < repetitions; repetitionI++) {
				
					logger.info("Executing query (warm, "
							+ queryI + ", " + repetitionI + "): \n"
							+ queryStruct.getQuery());

					// measure times
					warmruns[queryI][repetitionI] = sut.runQueryWithTimeout(queryStruct.getQuery(), timeoutSecs);
					logger.info("Query executed (warm, "
							+ queryI + "," + repetitionI + "): "
							+ warmruns[queryI][repetitionI][0] + " + "
							+ warmruns[queryI][repetitionI][1] + " = "
							+ warmruns[queryI][repetitionI][2] + ", "
							+ warmruns[queryI][repetitionI][3]);
					// If query times out
					if ( warmruns[queryI][repetitionI][3] == -1 ) {
						break;
					}
				}
				sut.close();
			} catch (Exception e) {
				logger.error("While evaluating query(warm, " + queryI
						+ ", " + repetitionI + ")");
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				String stacktrace = sw.toString();
				logger.error(stacktrace);
				
				sut.close();
				sut.clearCaches();
				sut.initialize();
			}

			try {
				printStatistics(this.getClass().getSimpleName(), "warm", queryI,
						queryStruct, warmruns);
			} catch (IOException e) {
				logger.error("While printing statistics (warm, " + queryI + ")");
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				String stacktrace = sw.toString();
				logger.error(stacktrace);
			}

		}
		
		warmruns = null;
		coldruns = null;
	}
	
	@SuppressWarnings("all")
	public void printStatistics(String experiment, String mode, int queryI,
			QueryStruct queryStruct, long[][][] measurements)
			throws IOException {

		FileWriter fstream;
		BufferedWriter out;
		String filePath;
		File file;

		// If not exists create experiment folder
		String dirPath = logPath + "/" + sut.getClass().getSimpleName() + "-" + experiment;
		File dir = new File(dirPath);
		if (!dir.exists()) {
			logger.info("Creating directory: " + dirPath);
			boolean result = dir.mkdir();
			if (result) {
				logger.info("Directory created");
			}
		}

		// sort results and keep only total time
		SortedSet<Long> measurementsSorted = new TreeSet<Long>();
		for (int repetitionI = 0; repetitionI < repetitions; repetitionI++) {
			measurementsSorted.add(measurements[queryI][repetitionI][2]);
		}

		// calculate median
		int i = 0;
		long time = 0;
		for (Long long1 : measurementsSorted) {
			i++;
			if ((repetitions % 2 == 1) && ((repetitions / 2 + 1) == i)) {
				time = long1;
			} else if ((repetitions % 2 == 0) && ((repetitions / 2) == i)) {
				time = long1;
			} else if ((repetitions % 2 == 0) && ((repetitions / 2 + 1) == i)) {
				time = time + long1;
				time = time / 2;
			}
		}

		// Create short file
		filePath = dirPath + "/" + String.format("%02d", queryI) + "-" + queryStruct.getLabel() + "-" + mode;
		file = new File (filePath);
		if (!file.createNewFile()) {
			logger.error("File "+filePath+" already exists");
		}
		
		// Print short mode
		fstream = new FileWriter(filePath, true);
		
		out = new BufferedWriter(fstream);

		// If time out write 0 as median
		if (time > (long)timeoutSecs*1000000000)
			out.write(measurements[queryI][0][3] + " " + 0 + "\n");
		else
			out.write(measurements[queryI][0][3] + " " + time + "\n");
		out.close();
		logger.info("Statistiscs printed: " + filePath);
		
		// Create long file
		filePath = dirPath + "/" + String.format("%02d", queryI) + "-" + queryStruct.getLabel() + "-" + mode + "-long";
		file = new File (filePath);
		if (!file.createNewFile()) {
			logger.info("File "+filePath+" already exists");
			
		}
		
		// Print long mode
		fstream = new FileWriter(filePath, true);
		out = new BufferedWriter(fstream);
		for (int repetitionI = 0; repetitionI < repetitions; repetitionI++) {
			out.write(measurements[queryI][repetitionI][3] + " "
					+ measurements[queryI][repetitionI][0] + " "
					+ measurements[queryI][repetitionI][1] + " "
					+ measurements[queryI][repetitionI][2] + "\n");
		}
		out.close();

		logger.info("Statistiscs printed: " + filePath);
	}
}


