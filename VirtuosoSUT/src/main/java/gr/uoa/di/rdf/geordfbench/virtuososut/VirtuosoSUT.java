/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
package gr.uoa.di.rdf.geordfbench.virtuososut;

import gr.uoa.di.rdf.geordfbench.runtime.sut.impl.RDF4JBasedSUT;
import java.util.Map;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.geordfbench.runtime.executionspecs.IExecutionSpec;
import gr.uoa.di.rdf.geordfbench.runtime.hosts.IHost;
import gr.uoa.di.rdf.geordfbench.runtime.reportspecs.IReportSpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Properties;

public class VirtuosoSUT extends RDF4JBasedSUT {

    public final static Properties Properties;
    final private static String PROPERTIES_FILE_NAME = "virtuoso.properties";
    static String SYSCMD_VIRTUOSO_STOP;
    static String SYSCMD_VIRTUOSO_START;
    static String SYSCMD_VIRTUOSO_DELAY;

    static {
        InputStream is = VirtuosoSUT.class.getResourceAsStream("/" + PROPERTIES_FILE_NAME);
        logger.info("Reading " + VirtuosoSUT.class.getSimpleName() + " properties from file : " + VirtuosoSUT.class.getResource("/" + PROPERTIES_FILE_NAME));
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        // load the properties
        Properties = new Properties();
        try {
            Properties.load(in);
        } catch (IOException ex) {
            logger.fatal("Exception during static initialization of " + VirtuosoSUT.class.getSimpleName());
        }
        // read all values
        SYSCMD_VIRTUOSO_STOP = Properties.getProperty("VIRTUOSO_STOP");
        SYSCMD_VIRTUOSO_START = Properties.getProperty("VIRTUOSO_START");
        SYSCMD_VIRTUOSO_DELAY = Properties.getProperty("VIRTUOSO_DELAY");
    }

    public VirtuosoSUT(IHost host, Map<String, String> sutProperties,
            IReportSpec reportSpec, IExecutionSpec execSpec) {
        super(host, sutProperties, reportSpec, execSpec);
        // have to prepend the full path for the virtuoso-t binary in order to
        // work properly
        SYSCMD_VIRTUOSO_START = "cd " + sysProperties.get(VirtuosoSystem.KEY_BASEDIR) + "/"
                + sysProperties.get(VirtuosoSystem.KEY_REPOSITORYID) + " && "
                + sysProperties.get(VirtuosoSystem.KEY_BASEDIR) + "/../bin/"
                + SYSCMD_VIRTUOSO_START + " && sleep " + SYSCMD_VIRTUOSO_DELAY;
        SYSCMD_VIRTUOSO_STOP = SYSCMD_VIRTUOSO_STOP + " && sleep " + SYSCMD_VIRTUOSO_DELAY;
        logger = Logger.getLogger(VirtuosoSUT.class.getSimpleName());
    }

    @Override
    public void initialize() {
        try {
            logger.info("Initializing..");
            // startServer Virtuoso
            startServer();
            // create a VirtuosoSystem instance with default constructor
            this._sys = new VirtuosoSystem(this.sysProperties);
        } catch (RuntimeException e) {
            logger.error("Cannot initialize " + this.getClass().getSimpleName());
            logger.error(e.toString());
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    @Override
    public String translateQuery(String query, String label) {

        String translatedQuery = query;

//		givenPoint = "bif:st_point(23.71622, 37.97945)";
//		givenRadius = "2.93782";
        if (label.equals("Intersects_GeoNames_Point_Buffer")) {
            translatedQuery = translatedQuery.replace("geof:sfWithin", "bif:st_within");
            translatedQuery = translatedQuery.replace("geof:buffer(\"POINT(23.71622 37.97945)\"^^<http://www.opengis.net/ont/geosparql#wktLiteral>, 3000, <http://www.opengis.net/def/uom/OGC/1.0/metre>)", "bif:st_point(23.71622, 37.97945), 2.93782");
        } else if (label.equals("Intersects_GeoNames_Point_Distance")) {
            translatedQuery = translatedQuery.replace("geof:distance", "bif:st_distance");
            translatedQuery = translatedQuery.replace(", <http://www.opengis.net/def/uom/OGC/1.0/metre>:", "");
        } else if (label.equals("Area_CLC")) {
            translatedQuery = null;
        } else if (label.equals("Equals_GeoNames_DBPedia")) {
            translatedQuery = translatedQuery.replace("geof:sfEquals(?o1, ?o2)", "bif:st_within(?o1, ?o2, 0)");
        } else if (label.contains("Synthetic_Join_Distance_")
                || label.contains("Synthetic_Selection_Distance_")) {
            translatedQuery = translatedQuery.replace("http://www.opengis.net/ont/geosparql#wktLiteral", "http://www.openlinksw.com/schemas/virtrdf#Geometry");
            translatedQuery = translatedQuery.replace("geof:distance", "bif:st_distance");
            translatedQuery = translatedQuery.replace(", <http://www.opengis.net/def/uom/OGC/1.0/metre>", "");

            String[] querySplitted = translatedQuery.split("<= ");
            if (querySplitted.length == 2) {
                String distanceString = query.split("<= ")[1];
                distanceString = distanceString.split("\\)")[0];
                double distanceInMeter = Double.parseDouble(distanceString);
                double distanceInKm = distanceInMeter / 1000;
                translatedQuery = translatedQuery.replace(distanceString, String.format("%f", distanceInKm));
            }
        }

        return translatedQuery;
    }

    @Override
    public void stopServer() {
        String[] stop_virtuososerver = {"/bin/sh", "-c", SYSCMD_VIRTUOSO_STOP};
        Process pr;

        // stopServer Virtuoso
        try {
            logger.info("Stopping Virtuoso server...");
            pr = Runtime.getRuntime().exec(stop_virtuososerver);
            pr.waitFor();
            // The following block is not needed because the server is killed
            // therefore the exit value will probably be something like 143
            // representing the SIGTERM signal sent to the process.
           
//            if (pr.exitValue() != 0) {
//                logger.error("Something went wrong while stopping Virtuoso server");
//                logger.error("... with command " + Arrays.toString(stop_virtuososerver));
//            }
        } catch (IOException | InterruptedException e) {
            logger.fatal("Cannot stop Virtuoso server");
        }
    }

    @Override
    public void startServer() {
        String[] start_virtuososerver = {"/bin/sh", "-c", SYSCMD_VIRTUOSO_START};
        Process pr;

        // startServer Virtuoso
        try {
            logger.info("Starting Virtuoso server...");
            pr = Runtime.getRuntime().exec(start_virtuososerver);
            pr.waitFor();
            if (pr.exitValue() != 0) {
                logger.error("Something went wrong while starting Virtuoso server");
                logger.error("... with command " + Arrays.toString(start_virtuososerver));
            }
        } catch (IOException | InterruptedException e) {
            logger.fatal("Cannot start Virtuoso server");

        }
    }

    @Override
    public void close() {
        super.close();
        // stopServer Virtuoso
        stopServer();
    }

}
