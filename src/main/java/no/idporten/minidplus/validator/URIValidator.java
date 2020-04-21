package no.idporten.minidplus.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.net.URI;
import java.net.URISyntaxException;

public class URIValidator implements ConstraintValidator<ValidURI, String> {
    private boolean allowFragments;

    public void initialize(ValidURI constraint) {
        allowFragments = constraint.allowsFragments();
    }

    public boolean isValid(String obj, ConstraintValidatorContext context) {
        if (obj == null || obj.isEmpty()) {
            return true;
        }
        try {
            URI uri = new URI(obj);
            if (allowFragments || uri.getFragment() == null) {
                return uri.isAbsolute();
            } else {
                return false;
            }
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
