/**
 * Runs RDF4J experiment through the workload interface
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
package gr.uoa.di.rdf.Geographica3.rdf4jsut;

import static gr.uoa.di.rdf.Geographica3.runtime.hosts.IHost.SEP;
import gr.uoa.di.rdf.Geographica3.runtime.sys.interfaces.impl.RDF4JBasedGeographicaSystem;
import gr.uoa.di.rdf.Geographica3.runtime.runsut.RunSUTExperimentWorkload;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class RunRDF4JExperimentWorkload extends RunSUTExperimentWorkload {

    static {
        logger = Logger.getLogger(RunRDF4JExperimentWorkload.class.getSimpleName());
    }

    // --- Data members ------------------------------
    String relbasedir;

    // add system related options
    @Override
    protected void addOptions() {
        super.addOptions();

        // these are system related options
        // -- repository/database specific
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
        String basedir = this.host.getReposBaseDir().replace("/", SEP) + "/" + (relbasedir != null ? relbasedir : "");
        // get the repository name/id from the dataset
        String repository = this.workLoadSpec.getGeospatialDataset().getName();
        // map variables to property map
        Map<String, String> mapProps = new HashMap<>();
        mapProps.put(RDF4JBasedGeographicaSystem.KEY_BASEDIR, basedir);
        mapProps.put(RDF4JBasedGeographicaSystem.KEY_REPOSITORYID, repository);
        // create the RDF4JSUT for the given host, report specification and
        // system properties
        this.sut = new RDF4JSUT(this.host, mapProps, this.rptSpec, this.workLoadSpec.getGeospatialQueryset().getExecutionSpec());
        // read report related options that are also dependent on the RunSUT!
        relReportPath = getReportDir();
    }

    @Override
    protected String getReportDir() {
        return this.host.getReportsBaseDir().replace("/", SEP) + "/"
                + this.sut.getClass().getSimpleName() + "/"
                + cmd.getOptionValue("expdescription") + "/"
                + this.workLoadSpec.getGeospatialDataset().getRelativeBaseDir().replace("/", SEP);
    }

    public static void main(String[] args) throws Exception {
        RunSUTExperimentWorkload runSUT = new RunRDF4JExperimentWorkload();
        runSUT.run(args);
    }
}
