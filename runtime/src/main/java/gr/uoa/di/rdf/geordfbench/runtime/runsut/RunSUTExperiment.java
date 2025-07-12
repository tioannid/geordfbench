package gr.uoa.di.rdf.geordfbench.runtime.runsut;

import gr.uoa.di.rdf.geordfbench.runtime.datasets.util.DataSetUtil;
import gr.uoa.di.rdf.geordfbench.runtime.executionspecs.util.ExecutionSpecUtil;
import gr.uoa.di.rdf.geordfbench.runtime.experiments.Experiment;
import gr.uoa.di.rdf.geordfbench.runtime.hosts.util.HostUtil;
import gr.uoa.di.rdf.geordfbench.runtime.querysets.util.QuerySetUtil;
import gr.uoa.di.rdf.geordfbench.runtime.reportspecs.util.ReportSpecUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.geordfbench.runtime.querysets.complex.IQuerySet;
import gr.uoa.di.rdf.geordfbench.runtime.datasets.complex.IGeospatialDataSet;
import gr.uoa.di.rdf.geordfbench.runtime.executionspecs.IExecutionSpec;
import gr.uoa.di.rdf.geordfbench.runtime.executionspecs.IExecutionSpec.Action;
import gr.uoa.di.rdf.geordfbench.runtime.hosts.IHost;
import gr.uoa.di.rdf.geordfbench.runtime.reportsource.IReportSource;
import gr.uoa.di.rdf.geordfbench.runtime.reportsource.util.ReportSourceUtil;
import gr.uoa.di.rdf.geordfbench.runtime.reportspecs.IReportSpec;
import gr.uoa.di.rdf.geordfbench.runtime.sut.ISUT;
import gr.uoa.di.rdf.geordfbench.runtime.sys.interfaces.IGeographicaSystem;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public abstract class RunSUTExperiment {

    private static final String CONTENT_TYPE = "Content-type";
    private static final String APPLICATION_JSON = "application/json";

    // Inner class which helps create an http request object with
    // application/json content-type and uri from experiment specification
    // argument
    public static class JsonSpecHttpRequest {

        // Data Members
        private String cmdOptionName;

        // Constructor
        public JsonSpecHttpRequest(String cmdOptionName) {
            this.cmdOptionName = cmdOptionName;
        }

        // Methods
        public HttpRequest build() {
            return HttpRequest.newBuilder()
                    .uri(URI.create(cmdOptionName))
                    .setHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .build();
        }
    }
    protected static Logger logger = Logger.getLogger(RunSUTExperiment.class.getSimpleName());

    protected Options options = new Options();
    protected CommandLine cmd = null;
    protected ISUT<? extends IGeographicaSystem> sut = null;
    protected IHost host;
    protected IGeospatialDataSet geoDS;
    protected IReportSpec rptSpec;
    protected IReportSource rptSrcSpec;
    protected IExecutionSpec execSpec;
    protected IQuerySet querySet = null;
    protected int[] qif = null;
    protected int[] qef = null;
    protected String relReportPath;
    protected String expdescription;

    protected void printHelp() {
        logger.info("Usage: " + this.getClass().getSimpleName() + " [options]+");
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(this.getClass().getSimpleName(), options);
    }

    // add execution and dataset related options
    protected void addOptions() {
        options.addOption("?", "help", false, "Print help message");

        options.addOption("expdesc", "expdescription", true, "Experiment description");

        // -- dataset related options
        options.addOption("ds", "datasetconffile", true, "Dataset configuration JSON file");
        options.addOption("rds", "remotedatasetconffile", true, "Remote Dataset JSON configuration uri");

        // -- host related options
        //    TODO: check if <logpath> option is necessary or if it means only the relative part of the logpath
        options.addOption("h", "hostconffile", true, "Host configuration JSON file");
        options.addOption("rh", "remotehostconffile", true, "Remote Host JSON configuration uri");

        // -- report related options
        options.addOption("rs", "reportspec", true, "Report specs configuration JSON file");
        options.addOption("rrs", "remotereportspec", true, "Remote Report specs JSON configuration uri");

        // -- report source related options
        options.addOption("rpsr", "reportsource", true, "Report source specs configuration JSON file");
        options.addOption("rrpsr", "remotereportsource", true, "Remote Report source specs JSON configuration uri");

        // -- execution related options
        options.addOption("es", "executionspec", true, "Execution specs configuration JSON file");
        options.addOption("res", "remoteexecutionspec", true, "Remote Execution specs JSON configuration uri");

        // -- queryset related options
        options.addOption("qs", "querysetspec", true, "Queryset configuration JSON file");
        options.addOption("rqs", "remotequerysetspec", true, "Remote Queryset JSON configuration uri");
        options.addOption("qif", "queryincludefilter", true, "List of queries to include in the run");
        options.addOption("qef", "queryexcludefilter", true, "List of queries to exclude from the run");
    }

    // print all flat arguments
    private void logAllArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            logger.info("args[" + i + "] = " + args[i]);
        }
    }

    // log execution and dataset related options
    protected void logOptions() {
        logger.info("Experiment description:\t" + cmd.getOptionValue("expdescription"));

        logger.info("|==> Benchmark related experiment options");
        // -- dataset option
        if (cmd.hasOption("datasetconffile")) {
            logger.info("Dataset configuration JSON file:\t" + cmd.getOptionValue("datasetconffile"));
        } else {
            logger.info("Remote Dataset JSON configuration uri:\t" + cmd.getOptionValue("remotedatasetconffile"));
        }
        // -- queryset option
        if (cmd.hasOption("querysetspec")) {
            logger.info("Queryset configuration JSON file:\t" + cmd.getOptionValue("querysetspec"));
        } else {
            logger.info("Remote Queryset JSON configuration uri:\t" + cmd.getOptionValue("remotequerysetspec"));
        }
        logger.info("List of queries "
                + (cmd.hasOption("queryincludefilter")
                ? "to include in the run:\t" + cmd.getOptionValue("queryincludefilter") // inclusion filter overrules exclusion filter
                : cmd.hasOption("queryexcludefilter")
                ? "to exclude from the run:\t" + cmd.getOptionValue("queryexcludefilter") // exclusion filter works only by itself
                : "to include in the run:\t" + "all"));
        // -- execution option
        if (cmd.hasOption("executionspec")) {
            logger.info("Execution specs configuration JSON file:\t" + cmd.getOptionValue("executionspec"));
        } else {
            logger.info("Remote Execution specs JSON configuration uri:\t" + cmd.getOptionValue("remoteexecutionspec"));
        }

        logger.info("|==> Environment related experiment options");
        // -- host option
        if (cmd.hasOption("hostconffile")) {
            logger.info("Host configuration JSON file:\t" + cmd.getOptionValue("hostconffile"));
        } else {
            logger.info("Remote Host JSON configuration uri:\t" + cmd.getOptionValue("remotehostconffile"));
        }
        // -- report option
        if (cmd.hasOption("reportspec")) {
            logger.info("Report specs configuration JSON file:\t" + cmd.getOptionValue("reportspec"));
        } else {
            logger.info("Remote Report specs JSON configuration uri:\t" + cmd.getOptionValue("remotereportspec"));
        }
        // -- report source option
        if (cmd.hasOption("reportsource")) {
            logger.info("Report source specs configuration JSON file:\t" + cmd.getOptionValue("reportsource"));
        } else {
            logger.info("Remote Report source specs JSON configuration uri:\t" + cmd.getOptionValue("remotereportsource"));
        }
    }

    protected void initSystemUnderTest() {
        // create a common HTTP client for all probable remote requests
        HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.ALWAYS).build();
        expdescription = cmd.getOptionValue("expdescription");
        // BEWARE: local spec takes precedence over remote HTTP spec
        // -- dataset option
        if (cmd.hasOption("datasetconffile")) {
            geoDS = DataSetUtil.deserializeFromJSON(cmd.getOptionValue("datasetconffile"));
        } else if (cmd.hasOption("remotedatasetconffile")) {
            HttpRequest request = new JsonSpecHttpRequest(cmd.getOptionValue("remotedatasetconffile")).build();
            HttpResponse<String> response = null;
            try {
                response = client.send(request, BodyHandlers.ofString());
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            } catch (InterruptedException ex) {
                logger.error(ex.getMessage());
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }
            if (response != null) {
                geoDS = DataSetUtil.deserializeFromJSONString(response.body());
            }
        }
        logger.info(geoDS.toString());
        // -- queryset option
        if (cmd.hasOption("querysetspec")) {
            querySet = QuerySetUtil.deserializeFromJSON(cmd.getOptionValue("querysetspec"));
        } else if (cmd.hasOption("remotequerysetspec")) {
            HttpRequest request = new JsonSpecHttpRequest(cmd.getOptionValue("remotequerysetspec")).build();
            HttpResponse<String> response = null;
            try {
                response = client.send(request, BodyHandlers.ofString());
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            } catch (InterruptedException ex) {
                logger.error(ex.getMessage());
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }
            if (response != null) {
                querySet = QuerySetUtil.deserializeFromJSONString(response.body());
            }
        }
        // -- queryset query include filter
        // apply filter
        if (cmd.hasOption(
                "queryincludefilter")) {
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
        } else if (cmd.hasOption(
                "queryexcludefilter")) {
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
        // -- execution option
        if (cmd.hasOption("executionspec")) {
            execSpec = ExecutionSpecUtil.deserializeFromJSON(cmd.getOptionValue("executionspec"));
        } else if (cmd.hasOption("remoteexecutionspec")) {
            HttpRequest request = new JsonSpecHttpRequest(cmd.getOptionValue("remoteexecutionspec")).build();
            HttpResponse<String> response = null;
            try {
                response = client.send(request, BodyHandlers.ofString());
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            } catch (InterruptedException ex) {
                logger.error(ex.getMessage());
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }
            if (response != null) {
                execSpec = ExecutionSpecUtil.deserializeFromJSONString(response.body());
            }
        }

        // -- host option
        if (cmd.hasOption("hostconffile")) {
            host = HostUtil.deserializeFromJSON(cmd.getOptionValue("hostconffile"));
        } else if (cmd.hasOption("remotehostconffile")) {
            HttpRequest request = new JsonSpecHttpRequest(cmd.getOptionValue("remotehostconffile")).build();
            HttpResponse<String> response = null;
            try {
                response = client.send(request, BodyHandlers.ofString());
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            } catch (InterruptedException ex) {
                logger.error(ex.getMessage());
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }
            if (response != null) {
                host = HostUtil.deserializeFromJSONString(response.body());
            }
        }
        // -- report option
        if (cmd.hasOption("reportspec")) {
            rptSpec = ReportSpecUtil.deserializeFromJSON(cmd.getOptionValue("reportspec"));
        } else if (cmd.hasOption("remotereportspec")) {
            HttpRequest request = new JsonSpecHttpRequest(cmd.getOptionValue("remotereportspec")).build();
            HttpResponse<String> response = null;
            try {
                response = client.send(request, BodyHandlers.ofString());
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            } catch (InterruptedException ex) {
                logger.error(ex.getMessage());
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }
            if (response != null) {
                rptSpec = ReportSpecUtil.deserializeFromJSONString(response.body());
            }
        }
        // -- report source option
        if (cmd.hasOption("reportsource")) {
            rptSrcSpec = ReportSourceUtil.deserializeFromJSON(cmd.getOptionValue("reportsource"));
        } else if (cmd.hasOption("remotereportsource")) {
            HttpRequest request = new JsonSpecHttpRequest(cmd.getOptionValue("remotereportsource")).build();
            HttpResponse<String> response = null;
            try {
                response = client.send(request, BodyHandlers.ofString());
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            } catch (InterruptedException ex) {
                logger.error(ex.getMessage());
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }
            if (response != null) {
                rptSrcSpec = ReportSourceUtil.deserializeFromJSONString(response.body());
            }
        }
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
        Experiment experiment = null;
        try {
            experiment = new Experiment(sut, querySet, qif, geoDS, execSpec, rptSpec, rptSrcSpec, relReportPath, expdescription);
            // insert experiment unique id in report source DB
            experiment.setExpID(experiment.getRptSrcSpec().insExperiment(experiment));

            // Run, test or print queries of experiments
            // expect no args, use execSpec interface data member
            // to choose between run | print
            switch (execSpec.getAction()) {
                case RUN:
                    logger.info("Start " + experiment.getType());
                    experiment.getQrySet().setAction(Action.RUN);
                    experiment.run();
                    logger.info("End " + experiment.getType());
                    break;
                case PRINT:
                    System.out.println(experiment.getType());
                    System.out.println(querySet.getName() + "\n");
                    IQuerySet qs = experiment.getQrySet();
                    qs.setAction(Action.PRINT);
                    // print the common prefix header for all queries
                    System.out.println(qs.getNamespacePrefixHeader());
                    // print the main text for all queries
                    int qryNo = 0;
                    for (int qifNo = 0; qifNo < qs.getQueriesNum(); qifNo++) {
                        qryNo = qif[qifNo];
                        if (qs.getQuery(qryNo).getText() != null) {
                            System.out.println("\nQuery " + qryNo + " - " + qs.getQuery(qryNo).getLabel() + ":\n" + qs.getQuery(qryNo).getText());
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

        /* Removed the following because it is probable not needed and
        it interferes with JUnit tests.
        
        System.exit(0);
        
         */
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
