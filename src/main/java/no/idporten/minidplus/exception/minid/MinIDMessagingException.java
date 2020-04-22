package no.idporten.minidplus.exception.minid;

import no.idporten.minidplus.exception.IDPortenMessageID;

/**
 * Exception representing a messaging failure. Used as an abstraction for the underlying messaging exceptions. A
 * messaging exception potentially contains critical information about whether the failure was permanent or transient -
 * ie: whether to try again or give up.
 *
 * @author jonathan.scudder
 * @version 1.0
 */
public final class MinIDMessagingException extends Exception {

    private static final long serialVersionUID = 1L;
    /**
     * Failure type.
     */
    private FailureType failureType = FailureType.NOT_AVAILABLE;
    /**
     * Exception ID
     */
    private IDPortenMessageID exceptionID;

    /**
     * Constructor with the possibility to set the root exception and failure type.
     *
     * @param msg       The error message
     * @param rootCause The nested exception
     * @param fail      The failure type
     */
    public MinIDMessagingException(final IDPortenMessageID exceptionID, final String msg, final Throwable rootCause, final FailureType fail) {
        super(msg, rootCause);
        this.failureType = fail;
        this.exceptionID = exceptionID;
    }

    /**
     * Get the failure type.
     *
     * @return Returns the failureType.
     */
    public FailureType getFailureType() {
        return failureType;
    }

    /**
     * Gets exception id.
     */
    public IDPortenMessageID getExceptionID() {
        return this.exceptionID;
    }

    /**
     * Type of failure.
     */
    public enum FailureType {

        /**
         * Failure type for permanent errors.
         */
        PERMANENT,
        /**
         * Failure type for transient errors.
         */
        TRANSIENT,
        /**
         * Default failure type.
         */
        NOT_AVAILABLE
    }

}
