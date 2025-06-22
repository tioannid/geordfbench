package gr.uoa.di.rdf.geordfbench.runtime.runsut;

import gr.uoa.di.rdf.geordfbench.runtime.experiments.Experiment;
import gr.uoa.di.rdf.geordfbench.runtime.hosts.util.HostUtil;
import gr.uoa.di.rdf.geordfbench.runtime.reportspecs.util.ReportSpecUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.geordfbench.runtime.querysets.complex.IQuerySet;
import gr.uoa.di.rdf.geordfbench.runtime.executionspecs.IExecutionSpec.Action;
import gr.uoa.di.rdf.geordfbench.runtime.experiments.ExperimentWorkload;
import gr.uoa.di.rdf.geordfbench.runtime.hosts.IHost;
import gr.uoa.di.rdf.geordfbench.runtime.querysets.partofworkload.IQuerySetPartOfWorkload;
import gr.uoa.di.rdf.geordfbench.runtime.reportsource.IReportSource;
import gr.uoa.di.rdf.geordfbench.runtime.reportsource.util.ReportSourceUtil;
import gr.uoa.di.rdf.geordfbench.runtime.reportspecs.IReportSpec;
import gr.uoa.di.rdf.geordfbench.runtime.sut.ISUT;
import gr.uoa.di.rdf.geordfbench.runtime.sys.interfaces.IGeographicaSystem;
import gr.uoa.di.rdf.geordfbench.runtime.workloadspecs.IGeospatialWorkLoadSpec;
import gr.uoa.di.rdf.geordfbench.runtime.workloadspecs.util.WorkLoadSpecUtil;

public abstract class RunSUTExperimentWorkload {

    protected static Logger logger = Logger.getLogger(RunSUTExperimentWorkload.class.getSimpleName());

    protected Options options = new Options();
    protected CommandLine cmd = null;
    protected ISUT<? extends IGeographicaSystem> sut = null;
    protected IHost host;
    protected IReportSpec rptSpec;
    protected IReportSource rptSrcSpec;
    protected int[] qif = null;
    protected int[] qef = null;
    protected String relReportPath;
    protected String expdescription;
    protected IGeospatialWorkLoadSpec workLoadSpec;

    protected void printHelp() {
        logger.info("Usage: " + this.getClass().getSimpleName() + " [options]+");
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(this.getClass().getSimpleName(), options);
    }

    // add execution and dataset related options
    protected void addOptions() {
        options.addOption("?", "help", false, "Print help message");

        options.addOption("expdesc", "expdescription", true, "Experiment description");

        // -- workload related options
        options.addOption("wl", "workloadspec", true, "Workload specs configuration JSON file");
        options.addOption("qif", "queryincludefilter", true, "List of queries to include in the run");
        options.addOption("qef", "queryexcludefilter", true, "List of queries to exclude from the run");

        // -- host related options
        //    TODO: check if <logpath> option is necessary or if it means only the relative part of the logpath
        options.addOption("h", "hostconffile", true, "Host configuration JSON file");

        // -- report related options
        options.addOption("rs", "reportspec", true, "Report specs configuration JSON file");

        // -- report source related options
        options.addOption("rpsr", "reportsource", true, "Report source specs configuration JSON file");
    }

    // print all flat arguments
    private void logAllArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            logger.info("args[" + i + "] = " + args[i]);
        }
    }

    // log options
    protected void logOptions() {
        // -- experiment related options
        logger.info("|==> Experiment related options");
        logger.info("Experiment description:\t" + cmd.getOptionValue("expdescription"));

        logger.info("|==> Workload, Host, Report related options");
        logger.info("Workload specs configuration JSON file:\t" + cmd.getOptionValue("workloadspec"));
        logger.info("List of queries "
                + (cmd.hasOption("queryincludefilter")
                ? "to include in the run:\t" + cmd.getOptionValue("queryincludefilter") // inclusion filter overrules exclusion filter
                : cmd.hasOption("queryexcludefilter")
                ? "to exclude from the run:\t" + cmd.getOptionValue("queryexcludefilter") // exclusion filter works only by itself
                : "to include in the run:\t" + "all"));

        // -- host related options
        logger.info("Host configuration JSON file:\t" + cmd.getOptionValue("hostconffile"));
        // -- report related options
        logger.info("Report specs configuration JSON file:\t" + cmd.getOptionValue("reportspec"));
        // -- report source related options
        logger.info("Report source specs configuration JSON file:\t" + cmd.getOptionValue("reportsource"));
    }

    protected void initSystemUnderTest() {
        // -- experiment related options
        expdescription = cmd.getOptionValue("expdescription");
        // -- workload related options
        workLoadSpec = WorkLoadSpecUtil.deserializeFromJSON(cmd.getOptionValue("workloadspec"));
        // -- queryset query include filter
        // apply filter
        IQuerySetPartOfWorkload querySet = workLoadSpec.getGeospatialQueryset();
        if (cmd.hasOption("queryincludefilter")) {
            String[] qryPositionsStr = cmd.getOptionValue("queryincludefilter").split(",");
            qif = new int[qryPositionsStr.length];
            for (int i = 0; i < qryPositionsStr.length; i++) {
                qif[i] = Integer.parseInt(qryPositionsStr[i]);
            }
            querySet.filter(qif, IQuerySet.FilterAction.INCLUDE);
            // inform user about exclusion filter being ignored
            if (cmd.hasOption("queryexcludefilter")) {
                logger.warn("Exclusion filter is ignored!");
            }
        } else if (cmd.hasOption("queryexcludefilter")) {
            String[] qryPositionsStr = cmd.getOptionValue("queryexcludefilter").split(",");
            qef = new int[qryPositionsStr.length];
            for (int i = 0; i < qryPositionsStr.length; i++) {
                qef[i] = Integer.parseInt(qryPositionsStr[i]);
            }
            // convert qef to qif
            qif = new int[querySet.getQueriesNum() - qef.length];
            boolean found = false;
            int qifPos = 0;
            for (int i = 0; i < querySet.getQueriesNum(); i++) {
                found = false;
                for (int j = 0; j < qef.length; j++) {
                    if (qef[j] == i) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    qif[qifPos++] = i;
                } else {
                    continue;
                }
            }
            querySet.filter(qef, IQuerySet.FilterAction.EXCLUDE);
        } else {
            qif = new int[querySet.getQueriesNum()];
            for (int i = 0; i < querySet.getQueriesNum(); i++) {
                qif[i] = i;
            }
        }
        logger.info(workLoadSpec.toString());
        // read host related options
        host = HostUtil.deserializeFromJSON(cmd.getOptionValue("hostconffile"));
        // -- report related options
        rptSpec = ReportSpecUtil.deserializeFromJSON(cmd.getOptionValue("reportspec"));
        // -- report source related options
        rptSrcSpec = ReportSourceUtil.deserializeFromJSON(cmd.getOptionValue("reportsource"));
    }

    private String[] parseArguments(String[] args) {
        PosixParser parser = new PosixParser();

        try {
            cmd = parser.parse(options, args);

            // Print help if required
            if (cmd.hasOption("?")) {
                printHelp();
                System.exit(0);
            }

            // keep only arguments, get rid of options
            args = cmd.getArgs();
            // Check arguments. Should be 0 arguments because I moved
            // everything into options
            if (args.length != 0) {
                logger.info("Arguments not correct");
                logger.info("Arguments: " + args);
                printHelp();
                System.exit(-1);
            }

        } catch (ParseException e) {
            logger.error("Parsing failed.  Reason: " + e.getMessage());
            printHelp();
        }
        return args;
    }

    private void runExperiment(String[] args) {
        // Select and execute experiments
        ExperimentWorkload experimentWL = null;
        Experiment experiment = null;
        // expect no args, use queryset interface data member
        // to choose appropriate test
        IQuerySetPartOfWorkload querySet = workLoadSpec.getGeospatialQueryset();

        try {
            experimentWL = new ExperimentWorkload(workLoadSpec, sut, qif, rptSpec, rptSrcSpec, relReportPath, expdescription);
            experiment = experimentWL.getExperiment(experimentWL);
            // insert experiment unique id in report source DB
            experiment.setExpID(experiment.getRptSrcSpec().insExperiment(experiment));

            // Run, test or print queries of experiments
            // expect no args, use execSpec interface data member
            // to choose between run | print
            switch (querySet.getAction()) {
                case RUN:
                    logger.info("Start " + experimentWL.getType());
                    querySet.setAction(Action.RUN);
                    experiment.run();
                    logger.info("End " + experimentWL.getType());
                    break;
                case PRINT:
                    System.out.println(experimentWL.getType());
                    System.out.println(querySet.getName() + "\n");
                    querySet.setAction(Action.PRINT);
                    // print the common prefix header for all queries
                    System.out.println(querySet.getNamespacePrefixHeader());
                    // print the main text for all queries
                    int qryNo = 0;
                    for (int qifNo = 0; qifNo < querySet.getQueriesNum(); qifNo++) {
                        qryNo = qif[qifNo];
                        if (querySet.getQuery(qryNo).getText() != null) {
                            System.out.println("\nQuery " + qryNo + " - " + querySet.getQuery(qryNo).getLabel() + ":\n" + querySet.getQuery(qryNo).getText());
                        }
                    }
                    break;
                default:
                    logger.debug("Checkpoint #4");
                    printHelp();
                    break;
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
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

    /**
     * Construct the final report directory for logging results. The concrete
     * subclasses will use host, sut and other properties to construct the final
     * reporting location. e.g. host.getReportsBaseDir() + "/" +
     * this.sut.getClass().getSimpleName() + "/" + experimentShortDesc + "/" +
     *
     * @return a String with the report directory for logging results
     */
    protected abstract String getReportDir();
}
