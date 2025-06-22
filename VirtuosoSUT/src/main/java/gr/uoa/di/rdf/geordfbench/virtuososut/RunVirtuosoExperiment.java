/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
package gr.uoa.di.rdf.geordfbench.virtuososut;

import gr.uoa.di.rdf.geordfbench.runtime.runsut.RunSUTExperiment;
import java.util.Map;

import org.apache.log4j.Logger;

public class RunVirtuosoExperiment extends RunSUTExperiment {

    static {
        logger = Logger.getLogger(RunVirtuosoExperiment.class.getSimpleName());
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
    }

    // log system related options
    @Override
    protected void logOptions() {
        super.logOptions();
        logger.info("|==> Repository/Store options");
        logger.info("Virtuoso Server endpoint URL:\t" + cmd.getOptionValue("srvurl"));
        logger.info("Virtuoso server username:\t" + cmd.getOptionValue("srvuser"));
        logger.info("Virtuoso server password:\t" + cmd.getOptionValue("srvpassword"));
        logger.info("|==> System options");
        logger.info("BaseDir:\t" + cmd.getOptionValue("relbasedir"));
    }

    @Override
    protected void initSystemUnderTest() {
        super.initSystemUnderTest();
        // read system option values to variables
        relbasedir = cmd.getOptionValue("relbasedir");
        // calculate the repository base directory by augmenting it
        // with the host repos base directory
        String basedir = host.getReposBaseDir() + "/" + (relbasedir != null ? relbasedir : "");
        // get the repository name/id from the dataset
        String repository = geoDS.getName();
        // map variables to property map  ... RDF4J and Virtuoso specific       
        Map<String, String> mapProps = VirtuosoSystem.constructSysPropsMap(
                basedir, repository, "", false,
                cmd.getOptionValue("srvurl"), cmd.getOptionValue("srvuser"),
                cmd.getOptionValue("srvpassword"),
                (int) this.execSpec.getMaxDurationSecsPerQueryRep());
        // create the VirtuosoSUT for the given host, report specification and
        // system properties
        this.sut = new VirtuosoSUT(host, mapProps, rptSpec, execSpec);
        // read report related options that are also dependent on the RunSUT!
        relReportPath = getReportDir();
    }

    @Override
    protected String getReportDir() {
        return this.host.getReportsBaseDir() + "/"
                + this.sut.getClass().getSimpleName() + "/"
                + cmd.getOptionValue("expdescription") + "/"
                + geoDS.getRelativeBaseDir();
    }

    public static void main(String[] args) throws Exception {
        RunSUTExperiment runSUTExperiment = new RunVirtuosoExperiment();
        runSUTExperiment.run(args);
    }
}
