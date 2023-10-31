/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 * @since 20/03/2023
 */
package gr.uoa.di.rdf.Geographica3.jenageosparqlsut;

import java.util.Map;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.Geographica3.runtime.executionspecs.IExecutionSpec;
import gr.uoa.di.rdf.Geographica3.runtime.hosts.IHost;
import gr.uoa.di.rdf.Geographica3.runtime.reportspecs.IReportSpec;
import gr.uoa.di.rdf.Geographica3.runtime.sut.impl.JenaBasedSUT;
import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;

public class JenaGeoSPARQLSUT extends JenaBasedSUT {
     
    public JenaGeoSPARQLSUT(IHost host, Map<String, String> sutProperties, 
            IReportSpec reportSpec, IExecutionSpec execSpec) {
        super(host, sutProperties, reportSpec, execSpec);
        logger = Logger.getLogger(JenaGeoSPARQLSUT.class.getSimpleName());
    }

    @Override
    public void clearCaches() {
        GeoSPARQLConfig.reset();
        super.clearCaches();
        GeoSPARQLConfig.setupMemoryIndex();
    }
    
    
}
