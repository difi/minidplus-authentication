package no.idporten.minidplus.service;

import no.idporten.domain.sp.ServiceProvider;
import no.idporten.domain.user.MinidUser;
import no.idporten.domain.user.MobilePhoneNumber;
import no.idporten.domain.user.PersonNumber;
import no.idporten.log.audit.AuditLogger;
import no.idporten.minidplus.domain.LevelOfAssurance;
import no.idporten.minidplus.exception.minid.MinIDIncorrectCredentialException;
import no.idporten.minidplus.exception.minid.MinIDInvalidAcrLevelException;
import no.idporten.minidplus.exception.minid.MinIDSystemException;
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

import static junit.framework.TestCase.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AuthenticationServiceTest {

    private static final String sid = "123";
    private static final String pid = "23079422487";
    private static final String password = "password123";
    private static final String otp = "abc123";
    public static final String MINID_ON_THE_FLY_PASSPORT = "minid-on-the-fly-passport";
    private final ServiceProvider sp = new ServiceProvider("idporten");
    private final String minidplusSource = "minid-on-the-fly-passport";

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
        minidUser.setSource(MINID_ON_THE_FLY_PASSPORT);
        when(minIDService.findByPersonNumber(eq(personNumber))).thenReturn(minidUser);
        when(minIDService.validateUserPassword(eq(personNumber), eq(password))).thenReturn(true);
        try {
            authenticationService.authenticateUser(sid, pid, password, eq(sp), LevelOfAssurance.LEVEL4);
        } catch (Exception e) {
            fail("Should not have thrown exception " + e);
        }
    }

    @Test
    public void testAuthenticationFailedPidDoesNotExist() {
        when(minidPlusCache.getOTP(eq(sid))).thenReturn(otp);
        try {
            authenticationService.authenticateUser(sid, pid, password, eq(sp), LevelOfAssurance.LEVEL4);
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
        minidUser.setSource(MINID_ON_THE_FLY_PASSPORT);
        when(minIDService.findByPersonNumber(eq(personNumber))).thenReturn(minidUser);
        when(minIDService.validateUserPassword(eq(personNumber), eq(password))).thenReturn(false);
        try {
            authenticationService.authenticateUser(sid, pid, password, eq(sp), LevelOfAssurance.LEVEL4);
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
        minidUser.setSource(MINID_ON_THE_FLY_PASSPORT);
        when(minIDService.findByPersonNumber(eq(personNumber))).thenReturn(minidUser);
        when(minIDService.validateUserPassword(eq(personNumber), eq(password))).thenReturn(true);
        try {
            authenticationService.authenticateUser(sid, pid, password, eq(sp), LevelOfAssurance.LEVEL4);
            fail("should have failed");
        } catch (Exception e) {
            assertTrue(e instanceof MinIDInvalidAcrLevelException);
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

    @Test
    public void test_source_starts_with_minid_on_the_fly_gives_level4_no_matter() throws MinIDInvalidAcrLevelException, MinIDSystemException {
        assertEquals(LevelOfAssurance.LEVEL4, authenticationService.getLevelOfAssurance(minidplusSource, LevelOfAssurance.LEVEL4));
        assertEquals(LevelOfAssurance.LEVEL4, authenticationService.getLevelOfAssurance(minidplusSource, LevelOfAssurance.LEVEL3));
    }

    @Test
    public void test_source_doesnt_start_with_minid_on_the_fly_should_not_allow_level4() throws MinIDInvalidAcrLevelException {
        try {
            authenticationService.getLevelOfAssurance("minid-pinbrev", LevelOfAssurance.LEVEL4);
            fail("Should have thrown exception");
        } catch (Exception e) {
            assertTrue(e instanceof MinIDInvalidAcrLevelException);
        }
    }


    @Test
    public void test_source_doesnt_start_with_minid_on_the_fly_should_allow_level3() throws MinIDInvalidAcrLevelException, MinIDSystemException {
        assertEquals(LevelOfAssurance.LEVEL3, authenticationService.getLevelOfAssurance("minid-pinbrev", LevelOfAssurance.LEVEL3));
    }
}
