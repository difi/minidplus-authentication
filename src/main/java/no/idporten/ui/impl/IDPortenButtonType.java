package no.idporten.ui.impl;

/**
 * Button types.
 */
public enum IDPortenButtonType {

    NEXT,
    CLOSE, // contact info module close info-lightbox button
    CANCEL; // cancel button

    public String id() {
        return "idporten.inputbutton." + name();
    }

}
