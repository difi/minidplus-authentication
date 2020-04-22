package no.idporten.minidplus.exception.minid;

import no.idporten.minidplus.exception.IDPortenMessageID;

/**
 * Exception to be thrown when the user is not found in LDAP.
 *
 * @author Ruth Marie Jensen
 * @version 2.0
 */
public class MinIDUserNotFoundException extends MinIDAuthException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs error with a message key for displaying to the user.
     *
     * @param exceptionID exception id
     * @param msg         The internal text message
     */
    public MinIDUserNotFoundException(final IDPortenMessageID exceptionID, final String msg) {
        super(exceptionID, msg);
    }

}
