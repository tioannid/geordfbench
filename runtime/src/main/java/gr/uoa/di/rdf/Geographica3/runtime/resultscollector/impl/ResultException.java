package gr.uoa.di.rdf.Geographica3.runtime.resultscollector.impl;

/**
 * Contains the literals that represent query repetition result exceptions. Can
 * be used with the {@link QueryRepResult} members {@link noResults} and
 * {@link noScanErrors}.
 *
 * @author tioannid
 */
public enum ResultException {
    NONE(0, "NONE"),
    TIMEDOUT(-1, "TIMEDOUT"),
    UNSUPPORTED_OPERATOR(-2, "UNSUPPORTED_OPERATOR"),
    SYNTAX_ERROR(-3, "SYNTAX_ERROR"),
    UNSUPPORTED_QUERYLANGUAGE(-4, "UNSUPPORTED_QUERYLANGUAGE"),
    REPOSITORY_API_ERROR(-5, "REPOSITORY_API_ERROR"),
    QUERY_EVALUATION_FAILED(-6, "QUERY_EVALUATION_FAILED"),
    QUERY_EVALUATION_RUNTIME_ERROR(-7, "QUERY_EVALUATION_RUNTIME_ERROR"),
    QUERY_EVALUATION_UNKNOWN_ERROR(-8, "QUERY_EVALUATION_UNKNOWN_ERROR"),
    INVALID_GEOMETRY(-9, "INVALID_GEOMETRY");

    // --- Data members ------------------------------
    long resultException;
    String name;

    // --- Constructors ------------------------------
    ResultException(long resultException, String name) {
        this.resultException = resultException;
        this.name = name;
    }

    // --- Data Accessors -----------------------------------
    public long getResultException() {
        return resultException;
    }

    public String getName() {
        return name;
    }

    // --- Methods -----------------------------------
    /**
     * Checks if a query result value defines some error condition
     *
     * @param result a long representing the number of results in a query
     * @return <tt>true</tt> if it different from NONE.resultException
     * values
     */
    public boolean hasException() {
        return (resultException != NONE.resultException);
    }

    /**
     * Checks if a query result value represents a TIMEOUT ResultException.
     *
     * @param result a long representing the number of results in a query
     * @return <tt>true</tt> if it maps to TIMEDOUT ResultException
     */
    public static boolean isTimedOut(long result) {
        return (result == TIMEDOUT.resultException);
    }

    public static boolean isTimedOut(ResultException resException) {
        return (resException.equals(TIMEDOUT));
    }

    /**
     * Checks if a query result value represents an UNSUPPORTED ResultException.
     *
     * @param result a long representing the number of results in a query
     * @return <tt>true</tt> if it maps to UNSUPPORTED ResultException
     */
    public static boolean isUnsupportedOperator(long result) {
        return (result == UNSUPPORTED_OPERATOR.resultException);
    }

    /**
     * Checks if a query result value is an exception
     *
     * @param result a long representing the number of results in a query
     * @return <tt>true</tt> if it maps to one of the enum ResultException
     * values
     */
    public static boolean isException(long result) {
        return (isTimedOut(result) || isUnsupportedOperator(result));
    }
}
