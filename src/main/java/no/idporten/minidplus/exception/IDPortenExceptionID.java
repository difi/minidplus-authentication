package no.idporten.minidplus.exception;

public enum IDPortenExceptionID implements IDPortenMessageID {

    // IDs for Identity
    IDENTITY_PASSWORD_INCORRECT("I-1003", "The identity entered an invalid password"),
    IDENTITY_PINCODE_LOCKED("I-1008", "Identity pincode locked"),
    IDENTITY_INVALID_SECURITY_LEVEL("I-1010", "Identity not authorized for security level 4."),
    IDENTITY_QUARANTINED("I-1011", "Identity quarantined."),
    IDENTITY_QUARANTINED_ONE_HOUR("I-1012", "Identity quarantined one hour."),
    IDENTITY_CLOSED("I-1013", "Identity quarantined."),
    // IDs for LDAP
    LDAP_ATTRIBUTE_MISSING("L-1105", "Entry is missing an LDAP attribute"),
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
