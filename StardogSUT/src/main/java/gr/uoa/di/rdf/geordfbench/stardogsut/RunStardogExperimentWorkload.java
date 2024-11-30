/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
package gr.uoa.di.rdf.geordfbench.stardogsut;

import gr.uoa.di.rdf.geordfbench.runtime.sys.interfaces.impl.RDF4JBasedGeographicaSystem;
import gr.uoa.di.rdf.geordfbench.runtime.runsut.RunSUTExperimentWorkload;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class RunStardogExperimentWorkload extends RunSUTExperimentWorkload {

    static {
        logger = Logger.getLogger(RunStardogExperimentWorkload.class.getSimpleName());
    }

    // --- Data members ------------------------------
    String relbasedir;

    // add system related options
    @Override
    protected void addOptions() {
        super.addOptions();

        // these are system related options
        // -- repository/database specific
        options.addOption("surl", "srvurl", true, "Server endpoint URL");
        options.addOption("susr", "srvuser", true, "Server username");
        options.addOption("spwd", "srvpassword", true, "Server password");
        // -- repository location
        options.addOption("rbd", "relbasedir", true, "Relative to host repo base directory");
        // -- repository configuration
//        options.addOption("lc", "haslucene", true, "HasLucene");
//        options.addOption("idx", "indexes", true, "Indexes");
//        options.addOption("wktidx", "wktindexes", true, "WKT Index List");
//        // -- repository initialization
//        options.addOption("cr", "create", true, "CreateRepository");
    }

    // log system related options
    @Override
    protected void logOptions() {
        super.logOptions();
        logger.info("|==> Repository/Store options");
        logger.info("Server endpoint URL:\t" + cmd.getOptionValue("srvurl"));
        logger.info("Stradog server username:\t" + cmd.getOptionValue("srvuser"));
        logger.info("Strardog server password:\t" + cmd.getOptionValue("srvpassword"));
        logger.info("|==> System options");
        logger.info("BaseDir:\t" + cmd.getOptionValue("relbasedir"));
    }

    @Override
    protected void initSystemUnderTest() {
        super.initSystemUnderTest();
        // read system option values to variables
        this.relbasedir = cmd.getOptionValue("relbasedir");
        // calculate the repository base directory by augmenting it
        // with the host repos base directory
        String basedir = this.host.getReposBaseDir() + "/" + (relbasedir != null ? relbasedir : "");
        // get the repository name/id from the dataset
        String repository = this.workLoadSpec.getGeospatialDataset().getName();
        // map variables to property map
        Map<String, String> mapProps = new HashMap<>();
        //  ... RDF4J specific first
        mapProps.put(RDF4JBasedGeographicaSystem.KEY_BASEDIR, basedir);
        mapProps.put(RDF4JBasedGeographicaSystem.KEY_REPOSITORYID, repository);
        //  ... Stardog specific second
        mapProps.put(StardogSystem.KEY_SERVER_URL, cmd.getOptionValue("srvurl"));
        mapProps.put(StardogSystem.KEY_USERNAME, cmd.getOptionValue("srvuser"));
        mapProps.put(StardogSystem.KEY_USERPASSWORD, cmd.getOptionValue("srvpassword"));
        // create the RDF4JSUT for the given host, report specification and
        // system properties
        this.sut = new StardogSUT(this.host, mapProps, this.rptSpec, this.workLoadSpec.getGeospatialQueryset().getExecutionSpec());
        // read report related options that are also dependent on the RunSUT!
        relReportPath = getReportDir();
    }

    @Override
    protected String getReportDir() {
        return this.host.getReportsBaseDir() + "/"
                + this.sut.getClass().getSimpleName() + "/"
                + cmd.getOptionValue("expdescription") + "/"
                + this.workLoadSpec.getGeospatialDataset().getRelativeBaseDir();
    }

    public static void main(String[] args) throws Exception {
        RunSUTExperimentWorkload runSUTExperiment = new RunStardogExperimentWorkload();
        runSUTExperiment.run(args);
    }
}
