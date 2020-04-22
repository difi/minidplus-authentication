package no.idporten.minidplus.exception.minid;

import no.idporten.minidplus.exception.IDPortenMessageID;

/**
 * Exception thrown if an invalid credential is supplied to MinID. Invalid and incorrect credentials are not identical;
 * incorrect credentials do not match with the expected value, whilst invalid credentials do not meet requirements for
 * the value.
 *
 * @author jonathan.scudder
 * @version 1.0
 */
public class MinIDInvalidCredentialException extends MinIDAuthException {

    private static final long serialVersionUID = 1L;


    /**
     * Constructs error with a message key for displaying to the user.
     *
     * @param exceptionID exception id
     * @param msg         The internal text message
     */
    public MinIDInvalidCredentialException(final IDPortenMessageID exceptionID, final String msg) {
        super(exceptionID, msg);
    }

}
