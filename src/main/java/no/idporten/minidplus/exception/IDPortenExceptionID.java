package no.idporten.minidplus.exception;

public enum IDPortenExceptionID implements IDPortenMessageID {
    // IDs for SMS sending
    SMS_PSWINCOM_SEND_FAILED                            ("P-1000",  "Failed to send SMS"),
    SMS_PSWINCOM_GATEWAY_UNREACHABLE                    ("P-1001",  "Cannot send SMS, cannot reach SMS gateway"),
    AUDITLOG_WRITE_FAILED("A-1000", "Failed to write to audit log"),

    // IDs for Identity
    IDENTITY_MISSING("I-1000", "Identity object was missing"),
    IDENTITY_UNKOWN_STATE("I-1001", "Identity is in an unknown state"),
    IDENTITY_PASSWORD_EXPIRED("I-1002", "The identity's password has expired"),
    IDENTITY_PASSWORD_INCORRECT("I-1003", "The identity entered an invalid password"),
    IDENTITY_PASSWORD_EMPTY("I-1004", "The identity did not enter a password"),
    IDENTITY_AUTHENTICATION_FAILED("I-1005", "The identity was not authenticated"),
    IDENTITY_INVALID_PIN("I-1006", "The identity entered an invalid pin code"),
    IDENTITY_PINCODE_NULL("I-1007", "Identity missing pincodes"),
    IDENTITY_PINCODE_LOCKED("I-1008", "Identity pincode locked"),
    // IDs for LDAP
    LDAP_CONNECTION_FAILED("L-1000", "Failed to connect to LDAP"),
    LDAP_NO_CONNECTION("L-1001", "No LDAP connection"),

    LDAP_MODIFY_FAILED("L-1101", "Failed to modify LDAP entry"),
    LDAP_ENTRY_NOT_FOUND("L-1102", "Entry (identity/principal) not found in LDAP"),

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
