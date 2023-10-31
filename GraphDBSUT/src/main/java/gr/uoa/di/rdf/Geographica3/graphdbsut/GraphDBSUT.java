/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
package gr.uoa.di.rdf.Geographica3.graphdbsut;

import gr.uoa.di.rdf.Geographica3.runtime.sut.impl.RDF4JBasedSUT;
import java.util.Map;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.Geographica3.runtime.executionspecs.IExecutionSpec;
import gr.uoa.di.rdf.Geographica3.runtime.hosts.IHost;
import gr.uoa.di.rdf.Geographica3.runtime.reportspecs.IReportSpec;

public class GraphDBSUT extends RDF4JBasedSUT {

    public GraphDBSUT(IHost host, Map<String, String> sutProperties,
            IReportSpec reportSpec, IExecutionSpec execSpec) {
        super(host, sutProperties, reportSpec, execSpec);
        logger = Logger.getLogger(GraphDBSUT.class.getSimpleName());
    }

    @Override
    public void initialize() {
        try {
            logger.info("Initializing..");
            // create an GraphDBSystem instance with default constructor
            this._sys = new GraphDBSystem(this.sysProperties);
        } catch (RuntimeException e) {
            logger.error("Cannot initialize " + this.getClass().getSimpleName());
            logger.error(e.toString());
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    /* GraphDB has the following issues:
       - geof:buffer does not work as expected
        * it syntactically accepts the GeoSPARQL standard 3 argument function
          but returns empty geometries!
        * it has it old version of 2 argument function which is missing the
          3rd argument (distance unit), but also the 2nd argument (distance) is
          measured in 100Km. Therefore:
            geof:buffer(?geom, 3000, uom:metre) ==> geof:buffer(?geom, 0.03)
            geof:buffer(?geom, 4, uom:metre) ==> geof:buffer(?geom, 0.00004)
     */
    @Override
    public String translateQuery(String query, String label) {
        String translatedQuery = null;
        translatedQuery = query;

        if (label.matches("Within_GeoNames_Point_Buffer")) {
            translatedQuery = translatedQuery.replaceAll("3000, uom:metre", "0.03");
        } else if (label.matches("Buffer_GeoNames")
                || label.matches("Buffer_LGD")) {
            translatedQuery = translatedQuery.replaceAll("4, uom:metre", "0.00004");
        } else if (label.matches("Area_CLC")) {
            translatedQuery = translatedQuery.replaceAll("strdf:area", "ext:area");
        } else if (label.indexOf("Synthetic_Selection_Distance") != -1) { // Synthetic_POIs experiment
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
            // In the SyntheticOnly experiment the radius is in meters therefore 
            // we have to divide by 100000 to convert it to degrees, because this
            // the analogy of Km/degree around the average latitude 45 and GraphDB
            // uses geof:buffer(geom, TOL) where TOL is expressed in the same units
            // as geom, which for the Synthetic_POIs dataset is in EPSG 4326, that 
            // is degrees!! The SyntheticGenerator especially for POIs returns
            // radious in Km, that is the cRadius is already divide by 1000
            cRadious = cRadious / 100000;
            // 5. create the new filter using the desired format
            String newFilter = String.format("FILTER(geof:sfWithin(%s, geof:buffer(\"POINT(45 45)\"^^<http://www.opengis.net/ont/geosparql#wktLiteral>, %d))).\n}\n", cGeom, cRadious);
            // 6. replace old with new filter
            translatedQuery = translatedQuery.substring(0, translatedQuery.indexOf("FILTER")) + newFilter;

        }

        return translatedQuery;
    }

}
