package no.idporten.minidplus.exception;

public enum IDPortenExceptionID implements IDPortenMessageID {
    // IDs for SMS sending
    SMS_PSWINCOM_SEND_FAILED                            ("P-1000",  "Failed to send SMS"),
    SMS_PSWINCOM_GATEWAY_UNREACHABLE                    ("P-1001",  "Cannot send SMS, cannot reach SMS gateway"),
    AUDITLOG_WRITE_FAILED                               ("A-1000",  "Failed to write to audit log")
    ;
    
    /**
     * Exception ID
     */
    private String exceptionID;
    
    /**
     * Description
     */
    private String description;

    IDPortenExceptionID(final String exceptionID) {
        this(exceptionID, null);
    }
    
    /**
     * Creates enum instance with id and description.
     * 
     * @param exceptionID
     * @param description
     */

    IDPortenExceptionID(String exceptionID, String description) {
        this.exceptionID = exceptionID;
        this.description = description;
    }

    
    @Override
    public String getMessageID() {
        return this.exceptionID;
    }
    
    @Override
    public String getDescription() {
        return this.description == null ? "" : this.description;
    }


}
