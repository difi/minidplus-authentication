package no.idporten.minidplus.service;

import no.idporten.domain.sp.ServiceProvider;
import no.idporten.domain.user.MinidUser;
import no.idporten.domain.user.MobilePhoneNumber;
import no.idporten.domain.user.PersonNumber;
import no.idporten.log.audit.AuditLogger;
import no.idporten.minidplus.exception.minid.MinIDIncorrectCredentialException;
import no.idporten.minidplus.exception.minid.MinIDInvalidCredentialException;
import no.idporten.minidplus.linkmobility.LINKMobilityClient;
import no.idporten.minidplus.logging.audit.AuditID;
import no.idporten.minidplus.util.FeatureSwitches;
import no.minid.exception.MinidUserNotFoundException;
import no.minid.service.MinIDService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
    LINKMobilityClient linkMobilityClient;

    @MockBean
    FeatureSwitches featureSwitches;

    @MockBean
    AuditLogger auditLogger;

    @Test
    public void testAuthentication() {
        when(minidPlusCache.getOTP(eq(sid))).thenReturn(otp);
        PersonNumber personNumber = new PersonNumber(pid);
        MinidUser minidUser = new MinidUser(personNumber);
        minidUser.setSecurityLevel("4");
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
            assertTrue(e instanceof MinidUserNotFoundException);
        }
    }

    @Test
    public void testAuthenticationFailedWrongPassword() {
        when(minidPlusCache.getOTP(eq(sid))).thenReturn(otp);
        PersonNumber personNumber = new PersonNumber(pid);
        MinidUser minidUser = new MinidUser(personNumber);
        minidUser.setPersonNumber(personNumber);
        minidUser.setSecurityLevel("4");
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
    public void testAuthenticationWrongSecurityLevel() {
        when(minidPlusCache.getOTP(eq(sid))).thenReturn(otp);
        when(featureSwitches.isRequestObjectEnabled()).thenReturn(true);
        PersonNumber personNumber = new PersonNumber(pid);
        MinidUser minidUser = new MinidUser(personNumber);
        minidUser.setSecurityLevel("3");
        minidUser.setPhoneNumber(new MobilePhoneNumber("123456789"));
        when(minIDService.findByPersonNumber(eq(personNumber))).thenReturn(minidUser);
        when(minIDService.validateUserPassword(eq(personNumber), eq(password))).thenReturn(true);
        try {
            authenticationService.authenticateUser(sid, pid, password, eq(sp));
            fail("should have failed");
        } catch (Exception e) {
            assertTrue(e instanceof MinIDInvalidCredentialException);
        }
    }

    @Test
    public void testChangePassword() throws MinidUserNotFoundException {
        when(minidPlusCache.getSSN(eq(sid))).thenReturn(pid);
        PersonNumber personNumber = new PersonNumber(pid);
        MinidUser minidUser = new MinidUser(personNumber);
        minidUser.setSecurityLevel("4");
        minidUser.setPhoneNumber(new MobilePhoneNumber("123456789"));
        when(minIDService.findByPersonNumber(eq(personNumber))).thenReturn(minidUser);
        try {
            assertTrue(authenticationService.changePassword(sid, "daWør6"));
        } catch (Exception e) {
            fail("Should not have thrown exception " + e);
        }
        verify(auditLogger).log(
                eq(AuditID.PASSWORD_CHANGED.auditId()),
                isNull(),
                any());
    }

    @Test
    public void testChangePasswordFailed() throws MinidUserNotFoundException {
        when(minidPlusCache.getSSN(eq(sid))).thenReturn(pid);
        PersonNumber personNumber = new PersonNumber(pid);
        MinidUser minidUser = new MinidUser(personNumber);
        minidUser.setSecurityLevel("4");
        minidUser.setPhoneNumber(new MobilePhoneNumber("123456789"));
        when(minIDService.findByPersonNumber(eq(personNumber))).thenReturn(minidUser);
        doThrow(new MinidUserNotFoundException("bruker finnes ikke")).when(minIDService).updatePassword(eq(personNumber), eq(password));
        try {
            authenticationService.changePassword(sid, password);
            fail("Should  have thrown exception MinidUserNotFoundException");
        } catch (Exception e) {
            assertTrue(e instanceof MinidUserNotFoundException);
        }
        verifyNoInteractions(auditLogger);
    }


}
