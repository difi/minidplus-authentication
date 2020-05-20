package no.idporten.minidplus.util;

/**
 * Button types.
 */
public enum MinIdPlusButtonType {

    NEXT,
    CANCEL; // cancel button

    public String id() {
        return name().toLowerCase();
    }

}
