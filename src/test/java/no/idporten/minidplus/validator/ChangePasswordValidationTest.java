package no.idporten.minidplus.validator;

import no.idporten.minidplus.domain.PasswordChange;
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

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

@RunWith(SpringRunner.class)
public class ChangePasswordValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeClass
    public static void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    public void testEmptyInput() {
        //given
        PasswordChange passwordChange = new PasswordChange();
        passwordChange.setNewPassword(null);

        //when:
        Set<ConstraintViolation<PasswordChange>> violations
                = validator.validate(passwordChange);

        //then:
        assertFalse(violations.isEmpty());

    }

    @Test
    public void testValidInput() {
        //given
        PasswordChange passwordChange = new PasswordChange();
        passwordChange.setNewPassword("password01");

        //when:
        Set<ConstraintViolation<PasswordChange>> violations
                = validator.validate(passwordChange);

        //then:
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testInvalidInput() {
        //given
        PasswordChange passwordChange = new PasswordChange();
        passwordChange.setNewPassword("pw0rd");

        //when:
        Set<ConstraintViolation<PasswordChange>> violations
                = validator.validate(passwordChange);

        //then:
        assertFalse(violations.isEmpty());
    }

    @AfterClass
    public static void close() {
        validatorFactory.close();
    }
}
