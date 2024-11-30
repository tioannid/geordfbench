package gr.uoa.di.rdf.geordfbench.runtime.resultscollector.impl;

/**
 * Contains the literals that represent query evaluation stage flags. Can be
 * used with the {@link QueryRepResult} members {@link noResults} and
 * {@link noScanErrors}.
 *
 * @author tioannid
 */
public enum QueryEvaluationFlag {
    NOTSTARTED(-1, "NOTSTARTED"),
    STARTED(-2, "STARTED"),
    EVALUATED(-3, "EVALUATED"),
    SCANNED(-4, "SCANNED"),
    COMPLETED(-5, "COMPLETED"),
    EVALUATION_ERROR(-6, "EVALUATION_ERROR"),
    SCANNING(-7, "SCANNING"),
    PREPARETUPLE_ERROR(-8, "PREPARETUPLE_ERROR"),
    PREPARING(-9, "PREPARING"),
    EVALUATING(-10, "EVALUATING"),
    EVALUATION_TIMEDOUT(-11, "EVALUATION_TIMEDOUT");

    // --- Data members ------------------------------
    long evalFlag;
    String name;

    // --- Constructors ------------------------------
    QueryEvaluationFlag(long resultException, String name) {
        this.evalFlag = resultException;
        this.name = name;
    }

    // --- Data Accessors -----------------------------------
    public String getName() {
        return name;
    }

    public long getEvalFlag() {
        return evalFlag;
    }

    // --- Methods -----------------------------------
    /**
     * Checks if a query execution has COMPLETED.
     *
     * @param evaluation flag from a QueryRepResult
     * @return <tt>true</tt> if it maps to COMPLETED QueryEvaluationFlag
     */
    public static boolean hasCOMPLETED(QueryEvaluationFlag evalFlag) {
        return (evalFlag.equals(COMPLETED));
    }
}
