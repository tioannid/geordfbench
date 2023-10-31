/**
 * Runs JenaGeoSPARQL experiment through the workload interface
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 * @since 20/03/2023
 */
package gr.uoa.di.rdf.Geographica3.jenageosparqlsut;

import gr.uoa.di.rdf.Geographica3.runtime.sys.interfaces.impl.RDF4JBasedGeographicaSystem;
import gr.uoa.di.rdf.Geographica3.runtime.runsut.RunSUTExperimentWorkload;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class RunJenaGeoSPARQLExperimentWorkload extends RunSUTExperimentWorkload {

    static {
        logger = Logger.getLogger(RunJenaGeoSPARQLExperimentWorkload.class.getSimpleName());
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
        // -- repository initialization
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
        String basedir = this.host.getReposBaseDir() + "/" + (relbasedir != null ? relbasedir : "");
        // get the repository name/id from the dataset
        String repository = this.workLoadSpec.getGeospatialDataset().getName();
        // map variables to property map
        Map<String, String> mapProps = new HashMap<>();
        mapProps.put(RDF4JBasedGeographicaSystem.KEY_BASEDIR, basedir);
        mapProps.put(RDF4JBasedGeographicaSystem.KEY_REPOSITORYID, repository);
        // create the RDF4JSUT for the given host, report specification and
        // system properties
        this.sut = new JenaGeoSPARQLSUT(this.host, mapProps, this.rptSpec, this.workLoadSpec.getGeospatialQueryset().getExecutionSpec());
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
        RunSUTExperimentWorkload runSUT = new RunJenaGeoSPARQLExperimentWorkload();
        runSUT.run(args);
    }
}
