package no.idporten.minidplus.service;

import no.idporten.domain.sp.ServiceProvider;
import no.idporten.domain.user.MinidUser;
import no.idporten.domain.user.MobilePhoneNumber;
import no.idporten.domain.user.PersonNumber;
import no.idporten.minidplus.config.SmsProperties;
import no.idporten.minidplus.exception.minid.MinIDIncorrectCredentialException;
import no.idporten.minidplus.exception.minid.MinIDUserNotFoundException;
import no.minid.exception.MinidUserNotFoundException;
import no.minid.service.MinIDService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AuthenticationServiceTest {

    private static final String sid = "123";
    private static final String pid = "23079422487";
    private static final String password = "password123";
    private static final String otp = "abc123";
    private final ServiceProvider sp = new ServiceProvider("idporten");

    @Value("${minid-plus.credential-error-max-number}")
    private int MAX_NUMBER_OF_CREDENTIAL_ERRORS;

    @MockBean
    private MinidPlusCache minidPlusCache;

    @MockBean
    private MinIDService minIDService;

    @Autowired
    private AuthenticationService authenticationService;

    @MockBean
    OTCPasswordService otcPasswordService;

    @MockBean
    SmsService smsService;

    @MockBean
    SmsProperties smsProperties;

    @Test
    public void testAuthentication() {
        when(minidPlusCache.getOTP(eq(sid))).thenReturn(otp);
        PersonNumber personNumber = new PersonNumber(pid);
        MinidUser minidUser = new MinidUser(personNumber);
        minidUser.setPhoneNumber(new MobilePhoneNumber("123456789"));
        when(minIDService.findByPersonNumber(eq(personNumber))).thenReturn(minidUser);
        when(minIDService.validateUserPassword(eq(personNumber), eq(password))).thenReturn(true);
        try {
            authenticationService.authenticateUser(sid, pid, password, eq(sp));
        } catch (Exception e) {
            fail("Should not have thrown exception " + e);
        }
    }

    @Test
    public void testAuthenticationFailedPidDoesNotExist() {
        when(minidPlusCache.getOTP(eq(sid))).thenReturn(otp);
        try {
            authenticationService.authenticateUser(sid, pid, password, eq(sp));
            fail("should have failed");
        } catch (Exception e) {
            assertTrue(e instanceof MinIDUserNotFoundException);
        }
    }

    @Test
    public void testAuthenticationFailedWrongPassword() {
        when(minidPlusCache.getOTP(eq(sid))).thenReturn(otp);
        PersonNumber personNumber = new PersonNumber(pid);
        MinidUser minidUser = new MinidUser(personNumber);
        minidUser.setPhoneNumber(new MobilePhoneNumber("123456789"));
        when(minIDService.findByPersonNumber(eq(personNumber))).thenReturn(minidUser);
        when(minIDService.validateUserPassword(eq(personNumber), eq(password))).thenReturn(false);
        try {
            authenticationService.authenticateUser(sid, pid, password, eq(sp));
            fail("should have failed");
        } catch (Exception e) {
            assertTrue(e instanceof MinIDIncorrectCredentialException);
        }
    }

    @Test
    public void checkOTCCodePositiveTest() throws MinidUserNotFoundException {
        MinidUser user = new MinidUser();
        user.setCredentialErrorCounter(0);
        String sessionId = "123";

        when(minidPlusCache.getSSN(anyString())).thenReturn("12345678910");
        when(minidPlusCache.getOTP(anyString())).thenReturn("otctest");
        when(minIDService.findByPersonNumber(any())).thenReturn(user);

        String result = authenticationService.checkOTCCode(sessionId, "otctest");
        assert (user.getCredentialErrorCounter() == 0);
        assertEquals("Success", result);
    }

    @Test
    public void checkOTCCodeNegativeTest() throws MinidUserNotFoundException {
        MinidUser user = new MinidUser();
        user.setCredentialErrorCounter(0);
        String sessionId = "123";

        when(minidPlusCache.getSSN(anyString())).thenReturn("12345678910");
        when(minidPlusCache.getOTP(anyString())).thenReturn("otctest");
        when(minIDService.findByPersonNumber(any())).thenReturn(user);

        String result = authenticationService.checkOTCCode("otctestWrong", sessionId);
        assert (user.getCredentialErrorCounter() == 1);
        assertEquals("Error", result);
    }

    @Test
    public void checkOTCCodeNegativeTestLastTry() throws MinidUserNotFoundException {
        MinidUser user = new MinidUser();
        user.setCredentialErrorCounter(1);
        String sessionId = "123";

        when(minidPlusCache.getSSN(anyString())).thenReturn("12345678910");
        when(minidPlusCache.getOTP(anyString())).thenReturn("otctest");
        when(minIDService.findByPersonNumber(any())).thenReturn(user);

        String result = authenticationService.checkOTCCode("otctestWrong", sessionId);
        assert (user.getCredentialErrorCounter() == 2);
        assertEquals("Error, last chance", result);
    }

    @Test
    public void checkOTCCodMaxErrors() throws MinidUserNotFoundException {
        MinidUser user = new MinidUser();
        user.setCredentialErrorCounter(MAX_NUMBER_OF_CREDENTIAL_ERRORS);
        String sessionId = "123";

        when(minidPlusCache.getSSN(anyString())).thenReturn("12345678910");
        when(minidPlusCache.getOTP(anyString())).thenReturn("otctest");
        when(minIDService.findByPersonNumber(any())).thenReturn(user);

        String result = authenticationService.checkOTCCode("otctest", sessionId);
        assertEquals("Error, pin code locked", result);
    }

    @Test
    public void checkOTCCodeLocked() throws MinidUserNotFoundException {
        MinidUser user = new MinidUser();
        user.setCredentialErrorCounter(0);
        user.setOneTimeCodeLocked(true);
        String sessionId = "123";

        when(minidPlusCache.getSSN(anyString())).thenReturn("12345678910");
        when(minidPlusCache.getOTP(anyString())).thenReturn("otctest");
        when(minIDService.findByPersonNumber(any())).thenReturn(user);

        String result = authenticationService.checkOTCCode("otctest", sessionId);
        assertEquals("Error, pin code locked", result);
    }

    @Test
    public void checkLoginWithOTCLocked() throws MinIDIncorrectCredentialException, MinIDUserNotFoundException{
        MinidUser user = new MinidUser();
        user.setCredentialErrorCounter(0);
        user.setOneTimeCodeLocked(true);
        String sessionId = "123";

        when(minIDService.findByPersonNumber(any())).thenReturn(user);

        boolean result = authenticationService.authenticateUser("testSessionId","12345678910", "test", new ServiceProvider());
        assertFalse(result);
    }
}
