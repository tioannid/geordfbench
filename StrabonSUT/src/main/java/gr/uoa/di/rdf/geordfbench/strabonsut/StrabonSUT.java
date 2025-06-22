/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
package gr.uoa.di.rdf.geordfbench.strabonsut;

import java.util.Map;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.geordfbench.runtime.executionspecs.IExecutionSpec;
import gr.uoa.di.rdf.geordfbench.runtime.hosts.IHost;
import gr.uoa.di.rdf.geordfbench.runtime.reportspecs.IReportSpec;
import gr.uoa.di.rdf.geordfbench.runtime.sut.impl.SesamePostGISBasedSUT;

public class StrabonSUT extends SesamePostGISBasedSUT {

    public StrabonSUT(IHost host, Map<String, String> sutProperties,
            IReportSpec reportSpec, IExecutionSpec execSpec) {
        super(host, sutProperties, reportSpec, execSpec);
        logger = Logger.getLogger(StrabonSUT.class.getSimpleName());
    }

    @Override
    public void initialize() throws Exception {
        try {
            logger.info("Initializing..");
            // create an StrabonSystem instance with default constructor
            this._sys = new StrabonSystem(this.sysProperties);
        } catch (RuntimeException e) {
            logger.fatal("Cannot initialize " + this.getClass().getSimpleName());
            logger.fatal(e.toString());
        }
        if (this._sys == null) { // fatal error occurred
            throw new Exception("Internal system of " + this.getClass().getSimpleName() + " was not initialized!");
        }
    }

    @Override
    public String translateQuery(String query, String label) {
        String translatedQuery = query;

        // replace GeoSPARQL aggregate geof:union with StSPARQL strdf:union
        translatedQuery = translatedQuery.replaceAll("geof:union", "strdf:union");

        if (label.matches("Area_CLC")) {
            translatedQuery = translatedQuery.replaceAll("strdf:area", "geof:area");
        } else if (label.indexOf("Synthetic_Selection_Distance") != -1) {
            // convert this: FILTER ( bif:st_within(?geo1, bif:st_point(45, 45), 5000.000000)) 
            // .....to this: FILTER ( geof:sfWithin(?geo1, geof:buffer("POINT(23.71622 37.97945)"^^<http://www.opengis.net/ont/geosparql#wktLiteral>, 5000, <http://www.opengis.net/def/uom/OGC/1.0/metre>)))
            // 1. locate the last part of the query which starts with FILTER
            String cGeom = "";
            long cRadious = 0;
            String oldFilter = translatedQuery.substring(translatedQuery.indexOf("FILTER"));
            // 2. split to 4 parts using the comma as delimiter
            String[] oldfilterPart = oldFilter.split(",");
            // 3. split part-0 using the ( as delimiter
            //    ?geo1 is portion-2 of part-0
            cGeom = oldfilterPart[0].split("\\(")[2];
            // 4. split part-3 using the ) as delimiter
            //    RADIOUS is portion-0 of part-3 converted to long 
            cRadious = (long) Float.parseFloat(oldfilterPart[3].split("\\)")[0]);
            // 5. create the new filter using the desired format
            String newFilter = String.format("FILTER(geof:sfWithin(%s, geof:buffer(\"POINT(45 45)\"^^<http://www.opengis.net/ont/geosparql#wktLiteral>, %d, <http://www.opengis.net/def/uom/OGC/1.0/metre>))).\n}\n", cGeom, cRadious);
            // 6. replace old with new filter
            translatedQuery = translatedQuery.substring(0, translatedQuery.indexOf("FILTER")) + newFilter;
        }
        return translatedQuery;
    }
}
