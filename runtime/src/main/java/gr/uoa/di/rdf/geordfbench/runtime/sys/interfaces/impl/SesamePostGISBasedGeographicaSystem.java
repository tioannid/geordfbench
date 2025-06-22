package gr.uoa.di.rdf.geordfbench.runtime.sys.interfaces.impl;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.openrdf.repository.sail.SailRepositoryConnection;

public class SesamePostGISBasedGeographicaSystem extends AbstractGeographicaSystem<SailRepositoryConnection> {

    // --- Static members -----------------------------
    static public final String KEY_DBHOST = "dbhost";
    static public final String KEY_DBPORT = "dbport";
    static public final String KEY_DBNAME = "dbname";
    static public final String KEY_DBUSER = "dbuser";
    static public final String KEY_DBPASSWD = "dbpasswd";

    private static String[] validationQueries = new String[]{
        // Q1: returns the number of triples
        "SELECT (count(*) as ?count) WHERE { ?x ?p ?y . } ",
        // Q2: returns graph and the number of triples they contain
        "SELECT ?g (count(*) as ?count) WHERE { GRAPH ?g { ?x ?p ?y . } } GROUP BY ?g ORDER BY DESC(?count) ",
        // Q3: returns geometries and wkt that intersect with a point
        "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n"
        + "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n"
        + "PREFIX lgd: <http://data.linkedeodata.eu/ontology#>\n"
        + " SELECT ?s1 ?o1 WHERE {\n"
        + " ?s1 geo:asWKT ?o1 .\n"
        + "  FILTER(geof:sfIntersects(?o1, \"POINT (-3.9468805 51.618055)\"^^<http://www.opengis.net/ont/geosparql#wktLiteral>)).\n"
        + "}",
        // Q4: returns geometries and wkt that their buffer intersects with a point
        "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n"
        + "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n"
        + "PREFIX lgd: <http://data.linkedeodata.eu/ontology#>\n"
        + " SELECT ?s1 ?o1 WHERE {\n"
        + " ?s1 geo:asWKT ?o1 .\n"
        + "  FILTER(geof:sfIntersects(geof:buffer(?o1,0,<http://www.opengis.net/def/uom/OGC/1.0/metre>), \"POINT (-3.9468805 51.618055)\"^^<http://www.opengis.net/ont/geosparql#wktLiteral>)).\n"
        + "}",
        // Q5: returns the buffer of a point
        "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n"
        + "PREFIX geo: <http://www.opengis.net/ont/geosparql#>  \n"
        + "PREFIX lgd: <http://data.linkedeodata.eu/ontology#>\n"
        + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
        + "SELECT (geof:buffer(\"POINT(23.708496093749996 37.95719224376526)\"^^<http://www.opengis.net/ont/geosparql#wktLiteral>, 1, <http://www.opengis.net/def/uom/OGC/1.0/metre>) as ?o2)\n"
        + "WHERE { }"
    };

    // --- Static block/clause -----------------------
    static {
        // initialize the map of useful SesamePostGISBasedGeographicaSystem related RDF prefixes
        // Empty by intention. It is a reminder that subclasses should use the
        // static initialization block!
        logger = Logger.getLogger(SesamePostGISBasedGeographicaSystem.class.getSimpleName());
    }

    public static Map<String, String> constructSysPropsMap(String dbhost,
            String dbport, String dbname, String dbuser, String dbpasswd) {
        Map<String, String> sysMap = new HashMap<>();
        sysMap.put(KEY_DBHOST, dbhost);
        sysMap.put(KEY_DBPORT, dbport);
        sysMap.put(KEY_DBNAME, dbname);
        sysMap.put(KEY_DBUSER, dbuser);
        sysMap.put(KEY_DBPASSWD, dbpasswd);
        return sysMap;
    }

    // --- Data members ------------------------------
    protected String dbhost;
    protected String dbport;
    protected String dbname;
    protected String dbuser;
    protected String dbpasswd;

    // --- Constructors ------------------------------    
    public SesamePostGISBasedGeographicaSystem() {

    }

    public SesamePostGISBasedGeographicaSystem(Map<String, String> sysProps) throws Exception {
        this.setSysProps(sysProps);
    }

    // --- Methods -----------------------------------
    /**
     * Validates map values before they can be copied to object properties.
     *
     * @param sysProps A Map with system properties
     * @return a {@code true} value means that the provided properties are
     * accepted by the system, a {@code false} value means that some required
     * property by the system is missing or does not have an acceptable value.
     */
    @Override
    protected boolean validateSysProps(Map<String, String> sysProps) {
        boolean validates = true;
        // check if the mandatory properties for this class
        // exist in the sysPros
        validates = validates
                && sysProps.containsKey(KEY_DBHOST)
                && sysProps.containsKey(KEY_DBPORT)
                && sysProps.containsKey(KEY_DBNAME)
                && sysProps.containsKey(KEY_DBUSER)
                && sysProps.containsKey(KEY_DBPASSWD);
        if (!validates) { // exit immediately if invalid
            return validates;
        }
        // assign map values to data members
        try {
            // mandatory fields
            this.dbhost = sysProps.get(KEY_DBHOST);
            this.dbport = sysProps.get(KEY_DBPORT);
            this.dbname = sysProps.get(KEY_DBNAME);
            this.dbuser = sysProps.get(KEY_DBUSER);
            this.dbpasswd = sysProps.get(KEY_DBPASSWD);
        } catch (Exception e) {
            validates = false;
            throw new RuntimeException("System properties validation failed!");
        }
        return validates;
    }

}
