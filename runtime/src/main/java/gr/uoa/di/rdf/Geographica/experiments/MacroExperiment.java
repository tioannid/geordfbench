/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2013, Pyravlos Team
 *
 */
package gr.uoa.di.rdf.Geographica.experiments;

import gr.uoa.di.rdf.Geographica.queries.QueriesSet.QueryStruct;
import gr.uoa.di.rdf.Geographica.systemsundertest.SystemUnderTest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Logger;

/**
 * @author George Garbis <ggarbis@di.uoa.gr>
 */
public abstract class MacroExperiment extends Experiment {

	protected long[] measurements;
	protected int runTimeInMinutes;
	
	protected int[] queriesToRun = null;
	protected int queriesToRunN;

	public MacroExperiment(SystemUnderTest sut, int repetitions,
			int timeoutSecs, int runTimeInMinutes, String logPath) throws IOException {
		super(sut, repetitions, timeoutSecs, logPath);
		logger = Logger.getLogger(MacroExperiment.class.getSimpleName());
		this.runTimeInMinutes = runTimeInMinutes;
	}

	public MacroExperiment(SystemUnderTest sut, int repetitions,
			int timeoutSecs, int runTimeInMinutes, int[] queriesToRun, String logPath) throws IOException {
		super(sut, repetitions, timeoutSecs, logPath);
		logger = Logger.getLogger(MacroExperiment.class.getSimpleName());
		this.runTimeInMinutes = runTimeInMinutes;
		this.queriesToRun = queriesToRun;
	}
	
	@Override
	public void run() {
		
		QueryStruct queryStruct = null;
		long time;

		measurements = new long[4];

		// It's pointless to consider warm and cold caches about updates
		sut.clearCaches();
		sut.initialize();
		logger.info("Clear caches and initialize sut before updates");

		int repetitionI = 0;
		int queryI = 0;
		long t1 = System.currentTimeMillis();
		
		if (queriesToRun == null) {
			queriesToRunN = queriesSet.getQueriesN();
		} else {
			queriesToRunN = queriesToRun.length;
		}
		
		while (true) {
			try {
				for (int i = 0; i < queriesToRunN; i++) {
					if (queriesToRun == null) {
						queryI = i;
					} else {
						queryI = queriesToRun[i];
					}
					
					queryStruct = queriesSet.getQuery(queryI, repetitionI);

					logger.info("Executing query (" + queryI + ", " + repetitionI + "): "	+ queryStruct.getQuery());

					measurements =  sut.runQueryWithTimeout(queryStruct.getQuery(), timeoutSecs);
					logger.info("Query executed ("
									+ queryI + ", "	+ repetitionI + "): "
									+ measurements[0] + " + "
									+ measurements[1] + " = "
									+ measurements[2] + ", "
									+ measurements[3] + "\n");

					logger.debug("Reinitialize? query-result:"+measurements[3]);
					if (measurements[3] == -1) {
						// Sth went wront (e.g., time out) and sut should be reinitialized
						logger.debug("reinitialization");
						sut.initialize();
					}
				
					try {
						printStatisticsPerQuery(this.getClass().getSimpleName(), queryI, repetitionI, queryStruct, measurements);
					} catch (IOException e) {
						logger.error("While printing statistics (cold, " + queryI + ")");
						StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						String stacktrace = sw.toString();
						logger.error(stacktrace);
					}
				}
				
				repetitionI++;
				time = System.currentTimeMillis() - t1;
				logger.info("Executed " + repetitionI+" - "+time+"/"+runTimeInMinutes*60000);
				if ( time > runTimeInMinutes*60000) {
					logger.info("Finish at: " + time);
					break;
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
		}
		
		try {
			printStatisticsAll(this.getClass().getSimpleName(), repetitionI, time);
		} catch (IOException e) {
			logger.error("While printing statistics (cold, " + queryI + ")");
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String stacktrace = sw.toString();
			logger.error(stacktrace);
		}
		

		sut.close();
		sut.clearCaches();
		
	}
	
	@SuppressWarnings("all")
	public void printStatisticsPerQuery(String experiment, int queryI, int repetitionI,
			QueryStruct queryStruct, long[] measurements)
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

		// If not exists create short file
		filePath = dirPath + "/" + queryStruct.getLabel();
		file = new File (filePath);
		//if (!file.createNewFile()) {
		//	logger.error("File "+filePath+" already exists");
		//}
		
		// Print short mode
		fstream = new FileWriter(filePath, true);
		
		out = new BufferedWriter(fstream);

		out.write(measurements[0]+" "+measurements[1]+" "+measurements[2]+" "+measurements[3]+"\n");
		out.close();
		
		logger.info("Statistiscs printed: " + filePath);
		
		out.close();

		logger.info("Statistiscs printed: " + filePath);
	}
	
	@SuppressWarnings("all")
	public void printStatisticsAll(String experiment, int repetitions, long runtime)
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

		// Create short file
		filePath = dirPath + "/" + "repetitions";
		file = new File (filePath);
		//if (!file.createNewFile()) {
		//	logger.error("File "+filePath+" already exists");
		//}
		
		// Print short mode
		fstream = new FileWriter(filePath, true);
		
		out = new BufferedWriter(fstream);

		out.write(repetitions+" "+runtime+"\n");
		out.close();
		logger.info("Statistiscs printed: " + filePath);
		
		out.close();

		logger.info("Statistiscs printed: " + filePath);
	}

}
