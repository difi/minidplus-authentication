package no.idporten.minidplus.domain;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static junit.framework.TestCase.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class AuthorizationRequestValidatorTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeClass
    public static void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    public void authorization_request_is_valid() {
        AuthorizationRequest ar = new AuthorizationRequest();
        ar.setRedirectUri("http://localhost");
        ar.setLocale("nb");
        ar.setSpEntityId("NAV");
        ar.setGotoParam("http://digir.test.no");
        ar.setState("123abc");
        ar.setAcrValues(LevelOfAssurance.LEVEL4);
        ar.setResponseType("authorization_code");
        Set<ConstraintViolation<AuthorizationRequest>> violations = validator.validate(ar);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void authorization_request_is_invalid() {
        AuthorizationRequest ar = new AuthorizationRequest();
        ar.setRedirectUri("biff");
        ar.setState("123abc");
        ar.setAcrValues(LevelOfAssurance.LEVEL4);
        ar.setResponseType("authorization_code");
        ar.setLocale("nb");
        ar.setGotoParam("poteter");
        ar.setSpEntityId("NAV");
        Set<ConstraintViolation<AuthorizationRequest>> violations = validator.validate(ar);
        assertFalse(violations.isEmpty());
        assertEquals(2, violations.size());
    }

    @Test
    public void should_return_violation() {
        AuthorizationRequest ar = new AuthorizationRequest();
        Set<ConstraintViolation<AuthorizationRequest>> violations = validator.validate(ar);
        assertFalse(violations.isEmpty());
    }

    @AfterClass
    public static void close() {
        validatorFactory.close();
    }
}
