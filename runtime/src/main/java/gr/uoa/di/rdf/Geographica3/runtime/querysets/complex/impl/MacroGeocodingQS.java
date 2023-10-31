package gr.uoa.di.rdf.Geographica3.runtime.querysets.complex.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gr.uoa.di.rdf.Geographica3.runtime.executionspecs.IExecutionSpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.simple.IQuery;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public class MacroGeocodingQS extends MacroQS {

    // --- Static members -----------------------------
    Random rnd = new Random(0);
    static List<String> newYorkAddresses;
    static int qryExecuting = 0;

    static {
        logger = Logger.getLogger(MacroGeocodingQS.class.getSimpleName());
        String timestampsFile = "new-york-addresses.txt";
        newYorkAddresses = new ArrayList<String>();

        InputStream is = MacroGeocodingQS.class.getResourceAsStream("/" + timestampsFile);
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String givenTimestamp;
        try {
            while ((givenTimestamp = in.readLine()) != null) {
                newYorkAddresses.add(givenTimestamp);
            }
            in.close();
            in = null;
            is.close();
            is = null;
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    // --- Data members ------------------------------
    String streetNameTemplateName;
    String houseNoTemplateName;
    String zipTemplateName;
    String parityTemplateName;

    @JsonIgnore
    String streetname;
    @JsonIgnore
    String houseno;
    @JsonIgnore
    String zip;
    @JsonIgnore
    String parity;

    // --- Constructors ------------------------------
    // 1. Partial constructor:
    //      The query list is deserialized from a JSON file placed
    //      in the resources folder or the classpath in general
    //      Queries are placed on a map property
    public MacroGeocodingQS(String name, String relativeBaseDir,
            boolean hasPredicateQueriesAlso,
            Map<Integer, IQuery> mapQueries,
            Map<String, String> mapUsefulNamespacePrefixes,
            String streetNameTemplateName, String houseNoTemplateName,
            String zipTemplateName, String parityTemplateName) {
        super(name, relativeBaseDir, hasPredicateQueriesAlso,
                mapQueries, mapUsefulNamespacePrefixes);
        this.streetNameTemplateName = streetNameTemplateName;
        this.houseNoTemplateName = houseNoTemplateName;
        this.zipTemplateName = zipTemplateName;
        this.parityTemplateName = parityTemplateName;
    }

    public MacroGeocodingQS() {
    }

    // --- Data Accessors ----------------------------
    public String getStreetNameTemplateName() {
        return streetNameTemplateName;
    }

    public void setStreetNameTemplateName(String streetNameTemplateName) {
        this.streetNameTemplateName = streetNameTemplateName;
    }

    public String getHouseNoTemplateName() {
        return houseNoTemplateName;
    }

    public void setHouseNoTemplateName(String houseNoTemplateName) {
        this.houseNoTemplateName = houseNoTemplateName;
    }

    public String getZipTemplateName() {
        return zipTemplateName;
    }

    public void setZipTemplateName(String zipTemplateName) {
        this.zipTemplateName = zipTemplateName;
    }

    public String getParityTemplateName() {
        return parityTemplateName;
    }

    public void setParityTemplateName(String parityTemplateName) {
        this.parityTemplateName = parityTemplateName;
    }

    public void setNewYorkAddress(String newYorkAddress) {
        this.streetname = newYorkAddress.split("\t")[0];
        this.houseno = newYorkAddress.split("\t")[1];
        this.zip = newYorkAddress.split("\t")[2];
        this.parity = newYorkAddress.split("\t")[3];
    }

    // --- Methods ----------------------------    
    @Override
    public IQuery getQuery(int position) {
        // retrieve the query from the map
        IQuery qs = super.getQuery(position);
        // translate or construct a ground query only if in RUN mode
        if (this.action.equals(IExecutionSpec.Action.RUN)) {
            // replace the template parameters for the literal values retrieved
            // by the this.setNewYorkAddress(newYorkAddress)
            qs.setText(qs.getText().replace(this.streetNameTemplateName, this.streetname));
            qs.setText(qs.getText().replace(this.houseNoTemplateName, this.houseno));
            qs.setText(qs.getText().replace(this.zipTemplateName, this.zip));
            qs.setText(qs.getText().replace(this.parityTemplateName, this.parity));
        }
        qryExecuting = position;
        return qs;
    }

    @Override
    public void initializeQuerysetIteration() {
        super.initializeQuerysetIteration();
        // retrieve a random timestamp
        String newYorkAddress = newYorkAddresses.get(rnd.nextInt(newYorkAddresses.size()));
        // update both data members with one setter operation
        this.setNewYorkAddress(newYorkAddress);
    }

}
