package no.idporten.minidplus.exception.minid;

import no.idporten.minidplus.exception.IDPortenException;
import no.idporten.minidplus.exception.IDPortenMessageID;

/**
 * An unchecked exception for the MinID application.
 *
 * @author astridm
 * @version 1.0
 *
 */
public class MinIDSystemException extends IDPortenException {

    private static final long serialVersionUID = 1L;

    public MinIDSystemException(final IDPortenMessageID exceptionID, final String msg) {
        super(exceptionID, msg);
    }
    
    public MinIDSystemException(final IDPortenMessageID exceptionID, final String msg, final Throwable rootCause) {
        super(exceptionID, msg, rootCause);
    }
    
}
