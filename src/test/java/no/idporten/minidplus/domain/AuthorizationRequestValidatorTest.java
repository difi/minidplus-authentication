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
        ar.setRedirectUrl("http://localhost");
        ar.setService("IDPorten");
        ar.setStartService("IdportenLevel4List");
        ar.setGx_charset("UTF-8");
        ar.setLocale("nb");
        ar.setGotoParam("http://digir.test.no");
        ar.setForceAuth(true);
        Set<ConstraintViolation<AuthorizationRequest>> violations = validator.validate(ar);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void authorization_request_is_invalid() {
        AuthorizationRequest ar = new AuthorizationRequest();
        ar.setRedirectUrl("biff");
        ar.setService("IDPorten");
        ar.setStartService("IdportenLevel4List");
        ar.setGx_charset("UTF-8");
        ar.setLocale("nb");
        ar.setGotoParam("poteter");
        ar.setForceAuth(false);
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
