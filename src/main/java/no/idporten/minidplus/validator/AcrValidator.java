package no.idporten.minidplus.validator;

import no.idporten.minidplus.domain.LevelOfAssurance;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class AcrValidator implements ConstraintValidator<ValidAcr, LevelOfAssurance> {
    @Override
    public void initialize(ValidAcr validAcr) {

    }

    @Override
    public boolean isValid(LevelOfAssurance levelOfAssurance, ConstraintValidatorContext constraintValidatorContext) {
        return levelOfAssurance != LevelOfAssurance.UNKNOWN;
    }
}
