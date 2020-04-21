package no.idporten.minidplus.exception;

/**
 * Thrown on errors writing log.
 * @author she
 */
public class IDPortenLogException extends Exception {

    
    private static final long serialVersionUID = 1L;

    /**
     * Exception id
     */
    private IDPortenMessageID exceptionID;
    
    /**
     * Exception for logging operations.
     * 
     * @param exceptionID exception id
     * @param cause Originating exception
     */
    public IDPortenLogException(final IDPortenMessageID exceptionID, final Exception cause) {
        super(cause);
        this.exceptionID = exceptionID;
    }

    
    /**
     * Exception for logging operations.
     * 
     * @param exceptionID exception id
     * @param message Logger message
     */
    public IDPortenLogException(final IDPortenMessageID exceptionID, final String message) {
        super(message);
        this.exceptionID = exceptionID;
    }

    /**
     * Exception for logging operations.
     * 
     * @param exceptionID exception id
     * @param message Logger message
     * @param cause Originating exception
     */
    public IDPortenLogException(final IDPortenMessageID exceptionID, final String message, final Exception cause) {
        super(message, cause);
        this.exceptionID = exceptionID;
    }
    
    /**
     * Gets exception id.
     */
    public IDPortenMessageID getExceptionID() {
        return this.exceptionID;
    }
    
}
