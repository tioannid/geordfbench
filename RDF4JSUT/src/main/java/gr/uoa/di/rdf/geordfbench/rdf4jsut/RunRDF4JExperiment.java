/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
package gr.uoa.di.rdf.geordfbench.rdf4jsut;

import static gr.uoa.di.rdf.geordfbench.runtime.hosts.IHost.SEP;
import gr.uoa.di.rdf.geordfbench.runtime.sys.interfaces.impl.RDF4JBasedGeographicaSystem;
import gr.uoa.di.rdf.geordfbench.runtime.runsut.RunSUTExperiment;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class RunRDF4JExperiment extends RunSUTExperiment {

    private static final Logger logger = Logger.getLogger(RunRDF4JExperiment.class.getSimpleName());

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
        this.relbasedir = cmd.getOptionValue("relbasedir").replace("/", SEP);
        // calculate the repository base directory by augmenting it
        // with the host repos base directory
        String basedir = this.host.getReposBaseDir().replace("/", SEP) + SEP + (relbasedir != null ? relbasedir : "");
        // get the repository name/id from the dataset
        String repository = this.geoDS.getName();
        // map variables to property map
        Map<String, String> mapProps = new HashMap<>();
        mapProps.put(RDF4JBasedGeographicaSystem.KEY_BASEDIR, basedir);
        mapProps.put(RDF4JBasedGeographicaSystem.KEY_REPOSITORYID, repository);
        // create the RDF4JSUT for the given host, report specification and
        // system properties
        this.sut = new RDF4JSUT(this.host, mapProps, this.rptSpec, this.execSpec);
        // read report related options that are also dependent on the RunSUT!
        relReportPath = getReportDir();
    }

    @Override
    protected String getReportDir() {
        return this.host.getReportsBaseDir().replace("/", SEP) + SEP
                + this.sut.getClass().getSimpleName() + SEP
                + cmd.getOptionValue("expdescription") + SEP
                + this.geoDS.getRelativeBaseDir().replace("/", SEP);
    }

    public static void main(String[] args) throws Exception {
        RunSUTExperiment runSUT = new RunRDF4JExperiment();
        runSUT.run(args);
    }
}
