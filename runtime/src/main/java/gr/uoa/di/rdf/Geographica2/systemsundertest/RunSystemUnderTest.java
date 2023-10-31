package gr.uoa.di.rdf.Geographica2.systemsundertest;

import gr.uoa.di.rdf.Geographica2.experiments.*;
import gr.uoa.di.rdf.Geographica2.queries.QueriesSet;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;

public abstract class RunSystemUnderTest  {

	protected Logger logger = Logger.getLogger(RunSystemUnderTest.class.getSimpleName());
	protected Options options = new Options();
	protected CommandLine cmd = null;
	protected SystemUnderTest sut = null;

	protected void printHelp() {
		System.err.println("Usage: "+this.getClass().getSimpleName()+" [options] (run|print) (MicroNonTopological|MicroSelections|MicroJoins|MicroAggregations|MacroGeocoding|MacroMapSearch|MacroRapidMapping|MacroReverseGeocoding|MacroComputeStatistics|Synthetic|SyntheticPOIs|Scalability|PregenSynthetic)+");
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(this.getClass().getSimpleName(), options );		
	}
	
	protected void addOptions() {
		options.addOption("?", "help", false, "Print help message");
				
		options.addOption("q", "queries", true, "List of queries to run");
		options.addOption("r", "repetitions", true, "Repetitions for experiments (default: 5)");
		options.addOption("t", "timeout", true, "Timeout (seconds) for experiments (default 30mins)");
		options.addOption("m", "runtime", true, "Run time (minutes) for experiments (Macro scenarios) (default: 2hours)");
		options.addOption("N", "syntheticN", true, "Parameter for synthetic experiments");
		options.addOption("l", "logpath", true, "Log path");   
                options.addOption("uPred", "usePredicates", true, "Use geospatial predicates in queries if query set supports it");
                options.addOption("qp", "qrypath", true, "Query path");
	}

	private void logAllArguments(String[] args) {
		for (int i = 0; i < args.length; i++) {
			System.out.println("args["+i+"] = " + args[i]);
		}
	}
	
	protected void logOptions() {
		int repetitions = Integer.parseInt((cmd.getOptionValue("r")!=null?cmd.getOptionValue("r"):"5"));
		int timeoutSecs = (cmd.getOptionValue("t")!=null?Integer.parseInt(cmd.getOptionValue("t")):30*60); // 30 mins
		int runTimeInMinutes = (cmd.getOptionValue("m")!=null?Integer.parseInt(cmd.getOptionValue("runtime")):2*60); // 2 hours
		int syntheticN = Integer.parseInt((cmd.getOptionValue("N")!=null?cmd.getOptionValue("N"):"0"));
                
		logger.info("Shared options");
		logger.info("Repetitions:\t"+repetitions);
		logger.info("Time out:\t"+timeoutSecs+" seconds");
		logger.info("Run time:\t"+runTimeInMinutes+" minutes");
		logger.info("N:\t"+syntheticN);
		logger.info("Log Path:\t"+cmd.getOptionValue("logpath"));
                logger.info("Query Path:\t"+cmd.getOptionValue("qrypath"));
		logger.info("Queries to run:\t"+cmd.getOptionValue("queries"));
                logger.info("Use geospatial predicates:\t"+cmd.getOptionValue("usePredicates"));
	}
	
	protected abstract void initSystemUnderTest() throws Exception; 
	
	private String[] parseArguments(String[] args) {
		PosixParser parser = new PosixParser();
		
		try {
			cmd = parser.parse(options, args);

			// Print help if required
			if (cmd.hasOption("?")) {
				System.out.println("Checkpoint #1");
				printHelp();
				System.exit(0);
			}

			args = cmd.getArgs();
			// Check arguments
			if (args.length < 2) {
				System.out.println("Arguments not correct");
				System.out.println("Arguments: "+args);
				printHelp();
				System.exit(-1);
			}
			
		} catch (ParseException e) {
			System.err.println( "Parsing failed.  Reason: " + e.getMessage() );
			System.out.println("Checkpoint #5");
			printHelp();
		}
		return args;
	}

	private void runExperiment(String[] args) throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException {

		int repetitions = Integer.parseInt((cmd.getOptionValue("r")!=null?cmd.getOptionValue("r"):"5"));
		int timeoutSecs = (cmd.getOptionValue("t")!=null?Integer.parseInt(cmd.getOptionValue("t")):30*60); // 30 mins
		int runTimeInMinutes = (cmd.getOptionValue("m")!=null?Integer.parseInt(cmd.getOptionValue("runtime")):2*60); // 2 hours		 			
		int syntheticN = Integer.parseInt((cmd.getOptionValue("N")!=null?cmd.getOptionValue("N"):"0"));
		String logPath = cmd.getOptionValue("l");
                String qryPath = cmd.getOptionValue("qp");
                boolean usePredicates = Boolean.parseBoolean((cmd.getOptionValue("uPred")!=null?cmd.getOptionValue("uPred"):"false"));
		
		// List of queries to run
		String queriesToRunString = cmd.getOptionValue("q");
		int[] queriesToRun = null;
		if (queriesToRunString != null) {
			String[] queriesToRunStringArray = queriesToRunString.split(" ");
			queriesToRun = new int[queriesToRunStringArray.length];
			for(int i=0; i<queriesToRunStringArray.length; i++) {
				queriesToRun[i] = Integer.parseInt(queriesToRunStringArray[i]);
			}
		}
		
		// Select and execute experiments
		Experiment experiment = null;
		
		for (int i= 1; i<args.length; i++){
			// Micro experiments
			if ( args[i].equalsIgnoreCase("MicroNonTopological") ) {
				experiment = new MicroNonTopologicalExperiment(sut, repetitions, timeoutSecs, queriesToRun, logPath);
			} else if ( args[i].equalsIgnoreCase("MicroSelections") ) {
				experiment = new MicroSelectionsExperiment(sut, repetitions, timeoutSecs, queriesToRun, logPath);
			} else if ( args[i].equalsIgnoreCase("MicroJoins") ) {
				experiment = new MicroJoinsExperiment(sut, repetitions, timeoutSecs, queriesToRun, logPath);
			} else if ( args[i].equalsIgnoreCase("MicroAggregations") ) {
				experiment = new MicroAggregationsExperiment(sut, repetitions, timeoutSecs, queriesToRun, logPath);
			// Macro experiments
			} else if ( args[i].equalsIgnoreCase("MacroReverseGeocoding") ) {
				experiment = new MacroReverseGeocodingExperiment(sut, repetitions, timeoutSecs, runTimeInMinutes, queriesToRun, logPath);
			} else if ( args[i].equalsIgnoreCase("MacroMapSearch") ) {
				experiment = new MacroMapSearchExperiment(sut, repetitions, timeoutSecs, runTimeInMinutes, queriesToRun, logPath);
			} else if ( args[i].equalsIgnoreCase("MacroRapidMapping") ) {
				experiment = new MacroRapidMappingExperiment(sut, repetitions, timeoutSecs, runTimeInMinutes, queriesToRun, logPath);
			} else if ( args[i].equalsIgnoreCase("MacroComputeStatistics") ) {
				experiment = new MacroComputeStatisticsExperiment(sut, repetitions, timeoutSecs, runTimeInMinutes, queriesToRun, logPath);				
			} else if ( args[i].equalsIgnoreCase("MacroGeocoding") ) {
				experiment = new MacroGeocodingExperiment(sut, repetitions, timeoutSecs, runTimeInMinutes, queriesToRun, logPath);				
			// Synthetic
			} else if ( args[i].equalsIgnoreCase("Synthetic") ) {
				if (syntheticN == 0) {
					System.err.println("For synthetic data experiments parameter N>0 is obligatory");
				}
				experiment = new SyntheticExperiment(sut, repetitions, timeoutSecs, syntheticN, queriesToRun, logPath);
			} else if ( args[i].equalsIgnoreCase("SyntheticPOIs") ) {
				if (syntheticN == 0) {
					System.err.println("For synthetic data experiments parameter N>0 is obligatory");
				}
				experiment = new SyntheticOnlyPointsExperiment(sut, repetitions, timeoutSecs, syntheticN, queriesToRun, logPath);
                        // Scalability
			} else if ( args[i].equalsIgnoreCase("Scalability") ) {
				experiment = new ScalabilityExperiment(sut, usePredicates, repetitions, timeoutSecs, queriesToRun, logPath);
			// PregenSynthetic
			} else if ( args[i].equalsIgnoreCase("PregenSynthetic") ) {
				experiment = new PreGeneratedSyntheticExperiment(sut, repetitions, timeoutSecs, queriesToRun, logPath, qryPath);
                        }                         
			else {
				System.err.println("Error: "+args[i]+" is not recognized.");
				System.err.println("Only MicroNonTopological, MicroSelections, MicroJoins, MicroAggreagations,etc.");
				System.exit(-1);
			}

			// Run, test or print queries of experiments
			if ( args[0].equalsIgnoreCase("run") ) {
				logger.info("Start "+experiment.getClass().getName());
				experiment.run();
				logger.info("End "+experiment.getClass().getName());
			} else if ( args[0].equalsIgnoreCase("print") ) {
				System.out.println("\n"+experiment.getClass().getName()+"\n");
				QueriesSet qs = experiment.getQueriesSet();
				for(int j=0; j<qs.getQueriesN(); j++) {
					if (qs.getQuery(j, 0).getQuery() != null) {
						System.out.println("\nQuery "+j+" - "+qs.getQuery(j, 0).getLabel()+":\n"+qs.getQuery(j, 0).getQuery());
					}
				}
			} else {
				System.out.println("Checkpoint #4");
				printHelp();
			}
		}
	}
	
	public void run(String[] args) throws Exception {

		addOptions();

		logAllArguments(args);

		args = parseArguments(args);

		logOptions();

		initSystemUnderTest();
		
		runExperiment(args);

		System.exit(0);
	}	
}
