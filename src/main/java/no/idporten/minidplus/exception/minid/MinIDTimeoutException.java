package no.idporten.minidplus.exception.minid;

import no.idporten.minidplus.exception.IDPortenExceptionID;

/**
 * Exception thrown if an invalid securitylevel is supplied to MinID.
 */
public class MinIDTimeoutException extends MinIDAuthException {

    private static final long serialVersionUID = 1L;


    /**
     * Constructs error with a message key for displaying to the user.
     */
    public MinIDTimeoutException(final String msg) {
        super(IDPortenExceptionID.IDENTITY_INVALID_SECURITY_LEVEL, msg);
    }

}
