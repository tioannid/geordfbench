package gr.uoa.di.rdf.geordfbench.runtime.os;

/**
 * An interface that represents a target operating system for GeoRDFBench
 * experiments.
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Univ>
 * @creationdate 09/12/2019
 */
public interface IOS {

    /**
     * Clears system caches and use the appropriate delay for it to take effect.
     *
     * @param delay_mSecs
     */
    void clearCaches(int delay_mSecs); // clear cashes

    String getName();

    void initializeAfterDeserialization();

}
