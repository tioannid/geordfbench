package gr.uoa.di.rdf.geordfbench.runtime.querysets.partofworkload.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gr.uoa.di.rdf.geordfbench.runtime.executionspecs.IExecutionSpec;
import gr.uoa.di.rdf.geordfbench.runtime.executionspecs.IExecutionSpec.Action;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.geordfbench.runtime.querysets.simple.IQuery;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public class MacroMapSearchQS extends MacroQS {

    // --- Static members -----------------------------
    Random rnd = new Random(0);
    static List<String> toponyms;
    static int qryExecuting = 0;

    static {
        logger = Logger.getLogger(MacroMapSearchQS.class.getSimpleName());
        String geonamesFile = "geonames.txt";
        toponyms = new ArrayList<String>();

        InputStream is = MacroMapSearchQS.class.getResourceAsStream("/" + geonamesFile);
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String name;
        try {
            while ((name = in.readLine()) != null) {
                toponyms.add(name);
            }
            in.close();
            in = null;
            is.close();
            is = null;
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(MacroMapSearchQS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // --- Data members ------------------------------
    String toponymTemplateName;
    String rectangleTemplateName;
    @JsonIgnore
    String lastToponymPoint = null;
    @JsonIgnore
    String lastToponymRectangleBBox = null;
    // --- Constructors ------------------------------
    // 1. Partial constructor:
    //      The query list is deserialized from a JSON file placed
    //      in the resources folder or the classpath in general
    //      Queries are placed on a map property

    public MacroMapSearchQS(IExecutionSpec executionSpec, String name,
            String relativeBaseDir, boolean hasPredicateQueriesAlso,
            Map<Integer, IQuery> mapQueries,
            Map<String, String> mapUsefulNamespacePrefixes,
            String toponymTemplateName, String rectangleTemplateName) {
        super(executionSpec, name, relativeBaseDir, hasPredicateQueriesAlso,
                mapQueries, mapUsefulNamespacePrefixes);
        this.toponymTemplateName = toponymTemplateName;
        this.rectangleTemplateName = rectangleTemplateName;
    }

    public MacroMapSearchQS() {
    }

    // --- Data Accessors ----------------------------
    public String getToponymTemplateName() {
        return toponymTemplateName;
    }

    public void setToponymTemplateName(String toponymTemplateName) {
        this.toponymTemplateName = toponymTemplateName;
    }

    public String getRectangleTemplateName() {
        return rectangleTemplateName;
    }

    public void setRectangleTemplateName(String rectangleTemplateName) {
        this.rectangleTemplateName = rectangleTemplateName;
    }

    public void setLastToponymPoint(String lastToponymPoint) {
        this.lastToponymPoint = lastToponymPoint;
        /* Commented out because it works only when lastToponymPoint is in
                   the form POINT(xxx.xx yyy.yy), but fails when the CRS URI is
                   included!!
		String[] temp = lastToponymPoint.split("[( )]");
         */
        String[] temp = lastToponymPoint.split("POINT")[1].split("[( )]");

        double x = Double.parseDouble(temp[1]);
        double y = Double.parseDouble(temp[2]);
        double xd = 0.03;
        double yd = 0.02;

        double x_min = x - xd;
        double x_max = x + xd;
        double y_min = y - yd;
        double y_max = y + yd;

        this.lastToponymRectangleBBox = "POLYGON((" + x_min + " " + y_min + ", "
                + "" + x_min + " " + y_max + ", "
                + "" + x_max + " " + y_max + ", "
                + "" + x_max + " " + y_min + ", "
                + "" + x_min + " " + y_min + "))";
        logger.debug("PP: " + this.lastToponymPoint);
        logger.debug("BB: " + this.lastToponymRectangleBBox);
    }

    // --- Methods ----------------------------    
    @Override
    public IQuery getQuery(int position) {
        // retrieve the query from the map
        IQuery qs = super.getQuery(position);
        // translate or construct a ground query only if in RUN mode
        if (this.getAction().equals(Action.RUN)) {
            switch (position) {
                case 0: // replace the template parameter of the first query (#0)
                    // with the last toponym value. The remaining 2 queries with be
                    // replaced AFTER each execution of the first query!
                    qs.setText(qs.getText().replace(this.toponymTemplateName,
                            toponyms.get(rnd.nextInt(toponyms.size()))));
                    break;
                case 1:
                case 2: // PREREQUISITE: the corresponding experiment MUST have updated
                    // the lastRectangleValue in order for this translation to succeed
                    qs.setText(qs.getText().replace(this.rectangleTemplateName, this.lastToponymRectangleBBox));
                    break;
            }
        }
        qryExecuting = position;
        return qs;
    }

    @Override
    public void setFirstBindingSetValues(Map<String, String> firstBindingSetValues) {
        // update the first bindingset values only when we are still in
        // query #0
        if (qryExecuting == 0) {
            super.setFirstBindingSetValues(firstBindingSetValues);
            if (this.firstBindingSetValues != null) {
                setLastToponymPoint(this.firstBindingSetValues.get("wkt"));
            }
        }
    }
}
