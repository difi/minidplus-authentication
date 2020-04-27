package no.idporten.minidplus.logging.audit;

public enum AuditID {
    UNKNOWN(0),
    TOKEN_CREATED(1),
    PASSWORD_CHANGED(2);

    static final String AUDIT_ID_FORMAT = "MINIDPLUS-%d-%s";
    String stringId;
    int numericId;

    AuditID(int numericId) {
        this.stringId = String.format(AUDIT_ID_FORMAT, numericId, this.name().replaceAll("_", "-"));
        this.numericId = numericId;
    }

    public int id() {
        return numericId;
    }

    public String auditId() {
        return stringId;
    }
}
