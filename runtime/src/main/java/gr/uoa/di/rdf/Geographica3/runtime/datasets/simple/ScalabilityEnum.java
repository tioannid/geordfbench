package gr.uoa.di.rdf.Geographica3.runtime.datasets.simple;

/**
 * Enumeration for encoding the labels for all the Scalability datasets.
 * 
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public enum ScalabilityEnum {
    K10("10K"), 
    K100("100K"), 
    M1("1M"), 
    M10("10M"), 
    M100("100M"), 
    M500("500M");

    
    // --- Data members ------------------------------
    private final String label;

    // --- Constructor -----------------------------------
    ScalabilityEnum(String label) {
        this.label = label;
    }

    // --- Data Accessors -----------------------------------
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
