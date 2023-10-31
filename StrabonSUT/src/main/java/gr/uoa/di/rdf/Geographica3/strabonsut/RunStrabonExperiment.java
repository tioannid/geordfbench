/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
package gr.uoa.di.rdf.Geographica3.strabonsut;

import gr.uoa.di.rdf.Geographica3.runtime.runsut.RunSUTExperiment;
import gr.uoa.di.rdf.Geographica3.runtime.sys.interfaces.impl.SesamePostGISBasedGeographicaSystem;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class RunStrabonExperiment extends RunSUTExperiment {

    static {
        // replace the parent class logger with a more specific one
        logger = Logger.getLogger(RunStrabonExperiment.class.getSimpleName());
    }

    // --- Data members ------------------------------
    String dbhost;
    String dbport;
    String dbname;
    String dbuser;
    String dbpasswd;

    // add system related options
    @Override
    protected void addOptions() {
        super.addOptions();

        options.addOption("dbh", "dbhost", true, "Database Host");
        options.addOption("dbp", "dbport", true, "Database Port");
        options.addOption("dbn", "dbname", true, "Database Name");
        options.addOption("dbu", "dbuser", true, "Database User");
        options.addOption("dbps", "dbpasswd", true, "Database User Password");
    }

    // log system related options
    @Override
    protected void logOptions() {
        super.logOptions();
        logger.info("|==> System options");
        logger.info("Server:\t" + cmd.getOptionValue("dbhost"));
        logger.info("Database:\t" + cmd.getOptionValue("dbname"));
        logger.info("Port:\t" + cmd.getOptionValue("dbport"));
        logger.info("Username:\t" + cmd.getOptionValue("dbuser"));
        logger.info("Password:\t" + cmd.getOptionValue("dbpasswd"));
    }

    @Override
    protected void initSystemUnderTest() {
        super.initSystemUnderTest();

        // defaults: assume DB is PostgreSQL, post is 5432, runs locally 
        dbhost = (cmd.getOptionValue("dbhost") != null ? cmd.getOptionValue("dbhost") : "localhost");
        dbport = (cmd.getOptionValue("dbport") != null ? cmd.getOptionValue("dbport") : "5432");
        dbname = cmd.getOptionValue("dbname");
        dbuser = cmd.getOptionValue("dbuser");
        dbpasswd = cmd.getOptionValue("dbpasswd");

        // create and initialize all basic properties of a SesamePostGISBasedGeographicaSystem
        Map<String, String> mapProps = new HashMap<>();
        mapProps.put(SesamePostGISBasedGeographicaSystem.KEY_DBHOST, dbhost);
        mapProps.put(SesamePostGISBasedGeographicaSystem.KEY_DBPORT, dbport);
        mapProps.put(SesamePostGISBasedGeographicaSystem.KEY_DBNAME, dbname);
        mapProps.put(SesamePostGISBasedGeographicaSystem.KEY_DBUSER, dbuser);
        mapProps.put(SesamePostGISBasedGeographicaSystem.KEY_DBPASSWD, dbpasswd);

        sut = new StrabonSUT(this.host, mapProps, this.rptSpec, this.execSpec);

        // read report related options that are also dependent on the RunSUT!
        relReportPath = getReportDir();

    }

    @Override
    protected String getReportDir() {
        return this.host.getReportsBaseDir() + "/"
                + this.sut.getClass().getSimpleName() + "/"
                + cmd.getOptionValue("expdescription") + "/"
                + this.geoDS.getRelativeBaseDir();
    }

    public static void main(String[] args) throws Exception {
        RunSUTExperiment runSUT = new RunStrabonExperiment();
        runSUT.run(args);
    }
}
