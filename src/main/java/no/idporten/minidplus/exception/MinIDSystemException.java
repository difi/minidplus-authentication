package no.idporten.minidplus.exception;

/**
 * An unchecked exception for the MinID application.
 *
 * @author astridm
 * @version 1.0
 *
 */
public class MinIDSystemException extends IDPortenException {

    public MinIDSystemException(final IDPortenMessageID exceptionID, final String msg) {
        super(exceptionID, msg);
    }
    
    public MinIDSystemException(final IDPortenMessageID exceptionID, final String msg, final Throwable rootCause) {
        super(exceptionID, msg, rootCause);
    }
    
}
