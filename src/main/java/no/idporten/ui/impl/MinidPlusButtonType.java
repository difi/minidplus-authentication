package no.idporten.ui.impl;

/**
 * Button types.
 */
public enum MinidPlusButtonType {

    NEXT,
    CLOSE, // contact info module close info-lightbox button
    CANCEL; // cancel button

    public String id() {
        return "minidplus.inputbutton." + name();
    }

}
