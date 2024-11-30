/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
package gr.uoa.di.rdf.geordfbench.rdf4jsut;

import gr.uoa.di.rdf.geordfbench.runtime.sut.impl.RDF4JBasedSUT;
import java.util.Map;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.geordfbench.runtime.executionspecs.IExecutionSpec;
import gr.uoa.di.rdf.geordfbench.runtime.hosts.IHost;
import gr.uoa.di.rdf.geordfbench.runtime.reportspecs.IReportSpec;

public class RDF4JSUT extends RDF4JBasedSUT {
     
    public RDF4JSUT(IHost host, Map<String, String> sutProperties, 
            IReportSpec reportSpec, IExecutionSpec execSpec) {
        super(host, sutProperties, reportSpec, execSpec);
        logger = Logger.getLogger(RDF4JSUT.class.getSimpleName());
    }
    
}
