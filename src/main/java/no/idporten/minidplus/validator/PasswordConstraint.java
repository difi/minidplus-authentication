package no.idporten.minidplus.validator;

import no.idporten.validation.PasswordValidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordConstraint implements ConstraintValidator<Password, String> {

    public void initialize(Password constraintAnnotation) {
    }

    public boolean isValid(String password, ConstraintValidatorContext constraintContext) {
        return PasswordValidator.validatePassword(password);
    }

}