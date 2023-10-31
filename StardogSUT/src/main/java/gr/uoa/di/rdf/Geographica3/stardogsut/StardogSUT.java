/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
package gr.uoa.di.rdf.Geographica3.stardogsut;

import gr.uoa.di.rdf.Geographica3.runtime.sut.impl.RDF4JBasedSUT;
import java.util.Map;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.Geographica3.runtime.executionspecs.IExecutionSpec;
import gr.uoa.di.rdf.Geographica3.runtime.hosts.IHost;
import gr.uoa.di.rdf.Geographica3.runtime.reportspecs.IReportSpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StardogSUT extends RDF4JBasedSUT {

    public final static Properties Properties;
    final private static String PROPERTIES_FILE_NAME = "stardog.properties";
    static String SYSCMD_STARDOG_STOP;
    static String SYSCMD_STARDOG_START;
    static String SYSCMD_STARDOG_RESTART;
    static String SYSCMD_STARDOG_STATUS;

    static {
        InputStream is = StardogSUT.class.getResourceAsStream("/" + PROPERTIES_FILE_NAME);
        logger.info("Reading " + StardogSUT.class.getSimpleName() + " properties from file : " + StardogSUT.class.getResource("/" + PROPERTIES_FILE_NAME));
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        // load the properties
        Properties = new Properties();
        try {
            Properties.load(in);
        } catch (IOException ex) {
            logger.fatal("Exception during static initialization of " + StardogSUT.class.getSimpleName());
        }
        // read all values
        SYSCMD_STARDOG_STOP = Properties.getProperty("STARDOG_STOP");
        SYSCMD_STARDOG_START = Properties.getProperty("STARDOG_START");
        SYSCMD_STARDOG_RESTART = Properties.getProperty("STARDOG_RESTART");
        SYSCMD_STARDOG_STATUS = Properties.getProperty("STARDOG_STATUS");
    }

    public StardogSUT(IHost host, Map<String, String> sutProperties,
            IReportSpec reportSpec, IExecutionSpec execSpec) {
        super(host, sutProperties, reportSpec, execSpec);
        logger = Logger.getLogger(StardogSUT.class.getSimpleName());
    }

    @Override
    public void initialize() {
        try {
            logger.info("Initializing..");
            // create a StardogSystem instance with default constructor
            this._sys = new StardogSystem(this.sysProperties);
        } catch (RuntimeException e) {
            logger.error("Cannot initialize " + this.getClass().getSimpleName());
            logger.error(e.toString());
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    /* Stardog amongst many it also has the following issue:
       - the GeoSPARQL geof:sfIntersects is not implemented directly
     */
    @Override
    public String translateQuery(String query, String label) {
        String translatedQuery = null;
        translatedQuery = query;
        String SF_INTERSECTS = "sfIntersects";
        String SF_WITHIN = "sfWithin";
        String SF_EQUALS = "sfEquals";
        String RELATE = "relate";
        String GEOF_SF_INTERSECTS = "geof:" + SF_INTERSECTS;
        String GEOF_SF_WITHIN = "geof:" + SF_WITHIN;
        String GEOF_SF_EQUALS = "geof:" + SF_EQUALS;
        String PATTERN_SF_INTERSECTS = "geof:sfIntersects\\(.*\\)";
        String PATTERN_SF_WITHIN = "geof:sfWithin\\(.*\\)";
        String PATTERN_SF_EQUALS = "geof:sfEquals\\(.*\\)";
        String PATTERΝ_REPLACE_COMMA_RELATE = "\\(\\?\\w*,";

        // RULE-1: translate GeoSPARQL queries that have FILTER clauses with
        //      geof:sfIntersects and/ot geof:sfWithin
        boolean containsFILTER = query.toUpperCase().contains("FILTER");
        boolean containsFILTERsfIntersects = false, 
                containsFILTERsfWithin = false,
                containsFILTERsfEquals = false;
        if (containsFILTER) {
            containsFILTERsfIntersects = query.contains(GEOF_SF_INTERSECTS);
            containsFILTERsfWithin = query.contains(GEOF_SF_WITHIN);
            containsFILTERsfEquals = query.contains(GEOF_SF_EQUALS);
        }
        if (containsFILTERsfIntersects) { // REPLACE sfIntersects in FILTER clause
            String[] qryLines = query.split("\n");
            boolean found = false;
            int posFilter = 0;
            for (int i = 0; i < qryLines.length; i++) {
                found = (qryLines[i].toUpperCase().contains("FILTER"))
                        && (qryLines[i].contains(GEOF_SF_INTERSECTS));
                if (found) {
                    posFilter = i;
                    break;
                }
            }
            if (found) { // Contains geof:sfIntersects in a FILTER clause
                // keep all query before the FILTER
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < posFilter; i++) {
                    sb.append(qryLines[i]).append("\n");
                }
                // add the new triple pattern
                // ?relation geof:relate(?o1, ?o2) .
                Pattern pattern = Pattern.compile(PATTERN_SF_INTERSECTS);
                Matcher matcher
                        = pattern.matcher(qryLines[posFilter]);
                String newTriplePattern = "?relation ";
                if (matcher.find()) {
                    String sfint = qryLines[posFilter].substring(matcher.start(), matcher.end() - 1);
                    sfint = sfint.replace(SF_INTERSECTS, RELATE);
                    // remove the comma between the arguments of geof:relate
                    Pattern pattern2 = Pattern.compile(PATTERΝ_REPLACE_COMMA_RELATE);
                    Matcher matcher2
                            = pattern2.matcher(sfint);
                    if (matcher2.find()) {
                        String srelate = sfint.substring(0, matcher2.end() - 1)
                                + " " + sfint.substring(matcher2.end() + 1, sfint.length());
                        newTriplePattern += srelate;
                    }
                }
                sb.append(newTriplePattern).append(" .\n");
                // add a new FILTER clause
                sb.append("FILTER(?relation != geo:disjoint) .").append("\n");
                // add remaining query after the initial FILTER
                for (int i = posFilter + 1; i < qryLines.length; i++) {
                    sb.append(qryLines[i]).append("\n");
                }
                translatedQuery = sb.toString();
                //System.out.println(translatedQuery);
            } else { // skip translation

            }
        } else if (containsFILTERsfWithin) { // REPLACE sfWithin in FILTER clause
            String[] qryLines = query.split("\n");
            boolean found = false;
            int posFilter = 0;
            for (int i = 0; i < qryLines.length; i++) {
                found = (qryLines[i].toUpperCase().contains("FILTER"))
                        && (qryLines[i].contains(GEOF_SF_WITHIN));
                if (found) {
                    posFilter = i;
                    break;
                }
            }
            if (found) { // Contains geof:sfWithin in a FILTER clause
                // keep all query before the FILTER
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < posFilter; i++) {
                    sb.append(qryLines[i]).append("\n");
                }
                // add the new triple pattern
                // ?relation geof:relate(?o1, ?o2) .
                Pattern pattern = Pattern.compile(PATTERN_SF_WITHIN);
                Matcher matcher
                        = pattern.matcher(qryLines[posFilter]);
                String newTriplePattern = "?relation ";
                if (matcher.find()) {
                    String sfint = qryLines[posFilter].substring(matcher.start(), matcher.end() - 1);
                    sfint = sfint.replace(SF_WITHIN, RELATE);
                    // remove the comma between the arguments of geof:relate
                    Pattern pattern2 = Pattern.compile(PATTERΝ_REPLACE_COMMA_RELATE);
                    Matcher matcher2
                            = pattern2.matcher(sfint);
                    if (matcher2.find()) {
                        String srelate = sfint.substring(0, matcher2.end() - 1)
                                + " " + sfint.substring(matcher2.end() + 1, sfint.length());
                        newTriplePattern += srelate;
                    }
                }
                sb.append(newTriplePattern).append(" .\n");
                // add a new FILTER clause
                sb.append("FILTER(?relation = geo:within) .").append("\n");
                // add remaining query after the initial FILTER
                for (int i = posFilter + 1; i < qryLines.length; i++) {
                    sb.append(qryLines[i]).append("\n");
                }
                translatedQuery = sb.toString();
                //System.out.println(translatedQuery);
            } else { // skip translation

            }
        } else if (containsFILTERsfEquals) { // REPLACE sfEquals in FILTER clause
            String[] qryLines = query.split("\n");
            boolean found = false;
            int posFilter = 0;
            for (int i = 0; i < qryLines.length; i++) {
                found = (qryLines[i].toUpperCase().contains("FILTER"))
                        && (qryLines[i].contains(GEOF_SF_EQUALS));
                if (found) {
                    posFilter = i;
                    break;
                }
            }
            if (found) { // Contains geof:sfEquals in a FILTER clause
                // keep all query before the FILTER
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < posFilter; i++) {
                    sb.append(qryLines[i]).append("\n");
                }
                // add the new triple pattern
                // ?relation geof:relate(?o1, ?o2) .
                Pattern pattern = Pattern.compile(PATTERN_SF_EQUALS);
                Matcher matcher
                        = pattern.matcher(qryLines[posFilter]);
                String newTriplePattern = "?relation ";
                if (matcher.find()) {
                    String sfint = qryLines[posFilter].substring(matcher.start(), matcher.end() - 1);
                    sfint = sfint.replace(SF_EQUALS, RELATE);
                    // remove the comma between the arguments of geof:relate
                    Pattern pattern2 = Pattern.compile(PATTERΝ_REPLACE_COMMA_RELATE);
                    Matcher matcher2
                            = pattern2.matcher(sfint);
                    if (matcher2.find()) {
                        String srelate = sfint.substring(0, matcher2.end() - 1)
                                + " " + sfint.substring(matcher2.end() + 1, sfint.length());
                        newTriplePattern += srelate;
                    }
                }
                sb.append(newTriplePattern).append(" .\n");
                // add a new FILTER clause
                sb.append("FILTER(?relation = geo:equals) .").append("\n");
                // add remaining query after the initial FILTER
                for (int i = posFilter + 1; i < qryLines.length; i++) {
                    sb.append(qryLines[i]).append("\n");
                }
                translatedQuery = sb.toString();
                //System.out.println(translatedQuery);
            } else { // skip translation

            }
        }
        return translatedQuery;
    }

    /**
     * StardogSUT clears caches by calling operating system (JVM) caches to
     * clear and by restarting the Stardog server which is usually an HTTP
     * server.
     */
    @Override
    public void clearCaches() {
        String[] stop_stardogserver = {"/bin/sh", "-c", SYSCMD_STARDOG_STOP};
        String[] start_stardogserver = {"/bin/sh", "-c", SYSCMD_STARDOG_START};
        Process pr;

        // stopServer Stardog
        try {
            logger.info("Stopping Stardog server...");
            pr = Runtime.getRuntime().exec(stop_stardogserver);
            pr.waitFor();
            if (pr.exitValue() != 0) {
                logger.error("Something went wrong while stopping Stardog server");
                logger.error("... with command " + Arrays.toString(stop_stardogserver));
            }
        } catch (Exception e) {
            logger.fatal("Cannot stop Stardog server");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stacktrace = sw.toString();
            logger.fatal(stacktrace);
        }
        // RDF4JBasedSUT clear caches (OS/JVM clear caches)
        super.clearCaches();
        // startServer Stardog
        try {
            logger.info("Starting Stardog server...");
            pr = Runtime.getRuntime().exec(start_stardogserver);
            pr.waitFor();
            if (pr.exitValue() != 0) {
                logger.error("Something went wrong while starting Stardog server");
                logger.error("... with command " + Arrays.toString(start_stardogserver));
            }
        } catch (Exception e) {
            logger.fatal("Cannot start Stardog server");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stacktrace = sw.toString();
            logger.fatal(stacktrace);
        }
    }

}
