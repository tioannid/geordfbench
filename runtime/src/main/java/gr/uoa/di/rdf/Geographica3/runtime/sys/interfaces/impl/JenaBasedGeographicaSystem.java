package gr.uoa.di.rdf.Geographica3.runtime.sys.interfaces.impl;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.tdb.TDBFactory;

/**
 * A Jena base extention of the {@link AbstractGeographicaSystem<C>} abstract
 * implementation, which adds Jena specific repository management (TDB2)
 * capabilities.
 * <c> = <org.apache.jena.rdf.model.Model>
 * This class should be subclassed to generate Jena based systems such as
 * JenaGeoSPARQL or OpenLink Virtuoso.
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 * @since 20/03/2023
 */
public class JenaBasedGeographicaSystem extends AbstractGeographicaSystem<Model> {

    // --- Static members -----------------------------
    static public final String KEY_BASEDIR = "basedir";
    static public final String KEY_REPOSITORYID = "repositoryid";
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
        // initialize the map of useful JenaBasedGeographicaSystem related RDF prefixes
        // Empty by intention. It is a reminder that subclasses should use the
        // static initialization block!
        logger = Logger.getLogger(JenaBasedGeographicaSystem.class.getSimpleName());
    }

    /**
     * Construct the system properties map for a Jena based system, which
     * has/should have the following list of properties. It is dependent to the
     * list of static final strings KEY_XXXXX above.
     *
     * @param baseDir the base directory of the RDF4J server repos, e.g.
     * "RDF4J_3.4.3_Repos/server"
     * @param repositoryId the repository name
     * @return
     */
    public static Map<String, String> constructSysPropsMap(String baseDir,
            String repositoryId) {
        Map<String, String> sysMap = new HashMap<>();
        sysMap.put(KEY_BASEDIR, baseDir);
        sysMap.put(KEY_REPOSITORYID, repositoryId);
        return sysMap;
    }

    // --- Data members ------------------------------
    protected String baseDir;
    protected String repositoryId;
    protected Dataset repository; // Dataset is Jena's respective concept to RDF4J's repository

    // --- Constructors ------------------------------    
    public JenaBasedGeographicaSystem() {
    }

    /**
     * Constructs a Jena based system connected to a specific
     * repository/dataset, using a map of system properties in order to get
     * properly initialized with the init() function.
     *
     * @param sysProps a map of system properties
     */
    public JenaBasedGeographicaSystem(Map<String, String> sysProps) throws Exception {
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
                && sysProps.containsKey(KEY_BASEDIR)
                && sysProps.containsKey(KEY_REPOSITORYID);
        if (!validates) { // exit immediately if invalid
            return validates;
        }
        // assign map values to data members
        try {
            // mandatory fields
            this.baseDir = sysProps.get(KEY_BASEDIR);
            this.repositoryId = sysProps.get(KEY_REPOSITORYID);
        } catch (Exception e) {
            validates = false;
            throw new RuntimeException("System properties validation failed!");
        }
        return validates;
    }

    /**
     * Initializes the JenaBasedGeographicaSystem using the system properties,
     * the last step of which is to set the {@link initialized} property to
     * {@code true}. It uses the mandatory fields {@link baseDir} and
     * {@link repositoryId} to create/connect to a repository/dataset. Each
     * subclass should define in more detail the steps to properly initialize
     * the specific type of system, the main work being creating and setting the
     * connection property based on the appropriately initialized/set sysProps
     * map structure. BEWARE: Jena TDB works with transactions and subclasses
     * ARE REQUIRED to close the transaction that begins by this ancestor class.
     */
    @Override
    public void init() throws Exception {
        // Make a TDB-backed dataset
        String directory = baseDir + "/" + repositoryId;
        repository = TDBFactory.createDataset(directory);
        repository.begin(ReadWrite.READ);
        // Get model inside the transaction
        connection = repository.getDefaultModel();
        super.init();
    }

    // Commit transactions, close connection, shutdown undelying repository
    @Override
    public void close() {
        logger.info("Closing connection...");
        //connection.commit();
        repository.end();
        logger.info("Repository closed.");
        super.close();
    }

    public Dataset getRepository() {
        return repository;
    }
    
    
}
