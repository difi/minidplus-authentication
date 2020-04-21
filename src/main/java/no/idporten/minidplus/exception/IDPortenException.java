package no.idporten.minidplus.exception;

public class IDPortenException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -2889208732803005754L;
    private IDPortenMessageID exceptionID;

 
    public IDPortenException(final IDPortenMessageID exceptionId) {
        super();
        this.exceptionID = exceptionId;
    }

    public IDPortenException(final IDPortenMessageID exceptionId, final String message, final Throwable t) {
        super(message, t);
        this.exceptionID = exceptionId;
    }

    public IDPortenException(final IDPortenMessageID exceptionId, final String message) {
        super(message);
        this.exceptionID = exceptionId;
    }

    public IDPortenException(final IDPortenMessageID exceptionId, final Throwable t) {
        super(t);
        this.exceptionID = exceptionId;
    }
    

    public IDPortenMessageID getExceptionId() {
        return this.exceptionID;
    }
    
    
    
}
