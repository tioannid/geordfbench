/**
 * @author GeoRDFBench Creator <GeoRDFBench@Univ>
 */
package gr.uoa.di.rdf.Geographica3.runtime.experiments;

import org.apache.log4j.Logger;
import gr.uoa.di.rdf.Geographica3.runtime.reportsource.IReportSource;
import gr.uoa.di.rdf.Geographica3.runtime.reportspecs.IReportSpec;
import gr.uoa.di.rdf.Geographica3.runtime.sut.ISUT;
import gr.uoa.di.rdf.Geographica3.runtime.workloadspecs.IGeospatialWorkLoadSpec;

// <C> represents the connection class for each library (Sesame, RDF4J, Jena)
// IMPORTANT: Concrete subclass of Experiment should take care of appropriately
// instantiating the corresponding qrySet. This will definitely require at
// least an extra constructor parameter 'boolean usePred". This fact is also
// exhibited in the Experiment.run() method
public class ExperimentWorkload extends Experiment {

    // --- Static members -----------------------------
    static {
        logger = Logger.getLogger(ExperimentWorkload.class.getSimpleName());
    }

    // --- Data members ------------------------------
    protected IGeospatialWorkLoadSpec simpleGeospatialWL;

    // --- Constructors ------------------------------
    /**
     * Autonomous constructor: all data members receive user-provided, properly
     * instantiated objects
     *
     * @param simpleGeospatialWL
     * @param sut
     * @param qif
     * @param rptSpec
     * @param rptSrcSpec
     * @param logPath
     * @param description
     * @throws Exception
     */
    public ExperimentWorkload(IGeospatialWorkLoadSpec simpleGeospatialWL,
            ISUT sut, int[] qif, IReportSpec rptSpec, IReportSource rptSrcSpec,
            String logPath, String description) throws Exception {
        super(sut, simpleGeospatialWL.getGeospatialQueryset(), qif,
                simpleGeospatialWL.getGeospatialDataset(),
                simpleGeospatialWL.getGeospatialQueryset().getExecutionSpec(),
                rptSpec, rptSrcSpec, logPath, description);
        this.simpleGeospatialWL = simpleGeospatialWL;
    }

    // --- Data Accessors -----------------------------------
    public IGeospatialWorkLoadSpec getSimpleGeospatialWL() {
        return simpleGeospatialWL;
    }

    public void setSimpleGeospatialWL(IGeospatialWorkLoadSpec simpleGeospatialWL) {
        this.simpleGeospatialWL = simpleGeospatialWL;
    }

    @Override
    public String getType() {
        return simpleGeospatialWL.getName(); //To change body of generated methods, choose Tools | Templates.
    }

    
    // --- Methods -------------------------------------------
    public Experiment getExperiment(ExperimentWorkload experimentWL) throws Exception {
        return (Experiment) this;
    }
}
