package no.idporten.minidplus.exception.minid;

import no.idporten.minidplus.exception.IDPortenMessageID;

/**
 * Exception to be thrown when user's account is locked.
 */
public class MinIDQuarantinedUserException extends MinIDAuthException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs error with a message key for displaying to the user.
     *
     * @param exceptionID exception id
     * @param msg         The internal text message
     */
    public MinIDQuarantinedUserException(final IDPortenMessageID exceptionID, final String msg) {
        super(exceptionID, msg);
    }

}
