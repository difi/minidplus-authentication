package no.idporten.minidplus.service;

import no.idporten.domain.sp.ServiceProvider;
import no.idporten.domain.user.MinidUser;
import no.idporten.domain.user.PersonNumber;
import no.idporten.minidplus.exception.IDPortenExceptionID;
import no.idporten.minidplus.exception.minid.MinIDPincodeException;
import no.idporten.minidplus.exception.minid.MinIDQuarantinedUserException;
import no.idporten.minidplus.exception.minid.MinIDTimeoutException;
import no.idporten.minidplus.linkmobility.LINKMobilityClient;
import no.idporten.minidplus.logging.event.EventService;
import no.minid.exception.MinidUserNotFoundException;
import no.minid.service.MinIDService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Clock;
import java.util.Date;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(SpringRunner.class)
public class OTCPasswordServiceTest {

    private static final String sid = "123";
    private static final String pid = "23079422487";
    private static final String password = "password123";
    private static final String otp = "abc123";
    private final ServiceProvider sp = new ServiceProvider("idporten");

    @MockBean
    private MinidPlusCache minidPlusCache;

    @MockBean
    private MinIDService minIDService;

    @Autowired
    private OTCPasswordService otcPasswordService;

    @MockBean
    LINKMobilityClient linkMobilityClient;

    @MockBean
    EventService eventService;

    @Test
    public void checkOTCCodePositiveTest() throws MinidUserNotFoundException, MinIDPincodeException, MinIDTimeoutException, MinIDQuarantinedUserException {
        MinidUser user = new MinidUser();
        user.setCredentialErrorCounter(0);
        user.setPersonNumber(new PersonNumber(pid));
        user.setState(MinidUser.State.NORMAL);
        String sessionId = "123";

        when(minidPlusCache.getSSN(anyString())).thenReturn(pid);
        when(minidPlusCache.getOTP(anyString())).thenReturn("otctest");
        when(minIDService.findByPersonNumber(any())).thenReturn(user);

        assertTrue(otcPasswordService.checkOTCCode(sessionId, "otctest"));
        assert (user.getCredentialErrorCounter() == 0);

    }

    @Test
    public void checkOTCCodeNegativeTest() throws MinidUserNotFoundException, MinIDPincodeException, MinIDTimeoutException, MinIDQuarantinedUserException {
        MinidUser user = new MinidUser();
        user.setQuarantineCounter(0);
        user.setPersonNumber(new PersonNumber(pid));
        user.setState(MinidUser.State.NORMAL);
        String sessionId = "123";

        when(minidPlusCache.getSSN(anyString())).thenReturn(pid);
        when(minidPlusCache.getOTP(anyString())).thenReturn("otctest");
        when(minIDService.findByPersonNumber(any())).thenReturn(user);
        assertFalse(otcPasswordService.checkOTCCode(sessionId, "otctestWrong"));
        assert (user.getQuarantineCounter() == 1);
    }

    @Test
    public void checkOTCCodeNegativeTestLastTry() throws MinidUserNotFoundException, MinIDPincodeException, MinIDTimeoutException, MinIDQuarantinedUserException {
        MinidUser user = new MinidUser();
        user.setQuarantineCounter(1);
        user.setPersonNumber(new PersonNumber(pid));
        user.setState(MinidUser.State.NORMAL);
        String sessionId = "123";

        when(minidPlusCache.getSSN(anyString())).thenReturn(pid);
        when(minidPlusCache.getOTP(anyString())).thenReturn("otctest");
        when(minIDService.findByPersonNumber(any())).thenReturn(user);

        assertFalse(otcPasswordService.checkOTCCode(sessionId, "otctestWrong"));
        assert (user.getQuarantineCounter() == 2);

    }

    @Test
    public void checkOTCCodMaxErrors() throws MinidUserNotFoundException, MinIDQuarantinedUserException {
        MinidUser user = new MinidUser();
        user.setQuarantineCounter(3);
        user.setPersonNumber(new PersonNumber(pid));
        user.setState(MinidUser.State.NORMAL);

        String sessionId = "123";

        when(minidPlusCache.getSSN(anyString())).thenReturn(pid);
        when(minidPlusCache.getOTP(anyString())).thenReturn("otctest");
        when(minIDService.findByPersonNumber(any())).thenReturn(user);

        try {
            otcPasswordService.checkOTCCode(sessionId, "otctest");
            fail("Should have thrown MinIdPincodeException");
        } catch (MinIDPincodeException | MinIDTimeoutException | MinIDQuarantinedUserException e) {
            assertEquals(IDPortenExceptionID.IDENTITY_QUARANTINED, e.getExceptionID());
        }
    }

    @Test
    public void checkOTCCodInvalidSetIntoQuarantine() throws MinidUserNotFoundException, MinIDQuarantinedUserException {
        MinidUser user = new MinidUser();
        user.setQuarantineCounter(2);
        user.setPersonNumber(new PersonNumber(pid));
        user.setState(MinidUser.State.NORMAL);

        String sessionId = "123";

        when(minidPlusCache.getSSN(anyString())).thenReturn(pid);
        when(minidPlusCache.getOTP(anyString())).thenReturn("otctest");
        when(minIDService.findByPersonNumber(any())).thenReturn(user);

        try {
            otcPasswordService.checkOTCCode(sessionId, "otctext");
            fail("Should have thrown MinIdPincodeException");
        } catch (MinIDPincodeException | MinIDTimeoutException | MinIDQuarantinedUserException e) {
            assertEquals(MinidUser.State.QUARANTINED, user.getState());
            assertEquals(0, user.getQuarantineCounter().intValue());
            assertEquals(IDPortenExceptionID.IDENTITY_PINCODE_LOCKED, e.getExceptionID());
        }
    }

    @Test
    public void checkOTCCodeLocked() throws MinidUserNotFoundException, MinIDQuarantinedUserException {
        MinidUser user = new MinidUser();
        user.setQuarantineCounter(0);
        user.setQuarantineExpiryDate(Date.from(Clock.systemUTC().instant()));
        user.setOneTimeCodeLocked(true);
        user.setPersonNumber(new PersonNumber(pid));
        user.setState(MinidUser.State.NORMAL);
        String sessionId = "123";

        when(minidPlusCache.getSSN(anyString())).thenReturn(pid);
        when(minidPlusCache.getOTP(anyString())).thenReturn("otctest");
        when(minIDService.findByPersonNumber(any())).thenReturn(user);

        try {
            otcPasswordService.checkOTCCode(sessionId, "otctest");
        } catch (MinIDPincodeException | MinIDTimeoutException | MinIDQuarantinedUserException e) {
            assertEquals(IDPortenExceptionID.IDENTITY_PINCODE_LOCKED, e.getExceptionID());
            assertTrue(user.isOneTimeCodeLocked());
        }
    }

    @Test
    public void checkOTCQuarantinedButExpired() throws MinidUserNotFoundException {
        MinidUser user = new MinidUser();
        user.setCredentialErrorCounter(3);
        user.setQuarantineExpiryDate(Date.from(Clock.systemUTC().instant()));
        user.setOneTimeCodeLocked(false);
        user.setPersonNumber(new PersonNumber(pid));
        user.setState(MinidUser.State.QUARANTINED);
        String sessionId = "123";

        when(minidPlusCache.getSSN(anyString())).thenReturn(pid);
        when(minidPlusCache.getOTP(anyString())).thenReturn("otctest");
        when(minIDService.findByPersonNumber(any())).thenReturn(user);

        try {
            assertTrue(otcPasswordService.checkOTCCode(sessionId, "otctest"));
        } catch (MinIDPincodeException | MinIDTimeoutException | MinIDQuarantinedUserException e) {
            fail("Should ignore quarantine status if expired");
        }
    }

    @Test
    public void checkOTCQuarantinedAndNotExpired() throws MinidUserNotFoundException {
        MinidUser user = new MinidUser();
        user.setCredentialErrorCounter(0);
        user.setQuarantineExpiryDate(Date.from(Clock.systemUTC().instant().plusSeconds(3600)));
        user.setOneTimeCodeLocked(false);
        user.setPersonNumber(new PersonNumber(pid));
        user.setState(MinidUser.State.QUARANTINED);
        String sessionId = "123";

        when(minidPlusCache.getSSN(anyString())).thenReturn(pid);
        when(minidPlusCache.getOTP(anyString())).thenReturn("otctest");
        when(minIDService.findByPersonNumber(any())).thenReturn(user);

        try {
            otcPasswordService.checkOTCCode(sessionId, "otctest");
            fail("Should not ignore quarantine status if not expired");
        } catch (MinIDPincodeException | MinIDTimeoutException | MinIDQuarantinedUserException e) {
            assertEquals(IDPortenExceptionID.IDENTITY_QUARANTINED, e.getExceptionID());
        }
    }
}
