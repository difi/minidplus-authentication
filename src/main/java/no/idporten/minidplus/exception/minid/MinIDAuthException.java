package no.idporten.minidplus.exception.minid;

import no.idporten.minidplus.exception.IDPortenMessageID;

/**
 * Exception modeling a general authentication exception.
 *
 * @author jonathan.scudder
 * @version 1.0
 */
public class MinIDAuthException extends Exception {

    private static final long serialVersionUID = 1L;

    private IDPortenMessageID exceptionID;

    /**
     * Constructs error with a message key for displaying to the user.
     *
     * @param msg the message
     */
    public MinIDAuthException(final String msg) {
        super(msg);
    }

    public MinIDAuthException(final IDPortenMessageID exceptionID, final String msg) {
        super(msg);
        this.exceptionID = exceptionID;
    }

    public IDPortenMessageID getExceptionID() {
        return this.exceptionID;
    }

}
