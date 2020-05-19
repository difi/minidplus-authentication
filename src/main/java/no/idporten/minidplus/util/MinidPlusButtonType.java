package no.idporten.minidplus.util;

/**
 * Button types.
 */
public enum MinidPlusButtonType {

    NEXT,
    CANCEL; // cancel button

    public String id() {
        return name().toLowerCase();
    }

}
