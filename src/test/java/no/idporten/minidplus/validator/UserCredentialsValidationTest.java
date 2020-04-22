package no.idporten.minidplus.validator;

import no.idporten.minidplus.domain.UserCredentials;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
public class UserCredentialsValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeClass
    public static void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    public void correctInputValidates() {
        //given:
        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setPersonalIdNumber("23079422487");
        userCredentials.setPassword("password01");

        //when:
        Set<ConstraintViolation<UserCredentials>> violations
                = validator.validate(userCredentials);

        //then:
        assertTrue(violations.isEmpty());
    }

    @Test
    public void incorrectPidInputFailsValidation() {
        //given:
        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setPersonalIdNumber("2307942248");
        userCredentials.setPassword("password01");

        //when:
        Set<ConstraintViolation<UserCredentials>> violations
                = validator.validate(userCredentials);

        //then:
        assertFalse(violations.isEmpty());
    }

    @Test
    public void incorrectPasswordInputFailsValidation() {
        //given:
        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setPersonalIdNumber("23079422487");
        userCredentials.setPassword("iôhQná\"«-óÓSGÉH©®EqjË=«ÒquW6>\\Jò-§");

        //when:
        Set<ConstraintViolation<UserCredentials>> violations
                = validator.validate(userCredentials);

        //then:
        assertFalse(violations.isEmpty());
    }

    @AfterClass
    public static void close() {
        validatorFactory.close();
    }
}
