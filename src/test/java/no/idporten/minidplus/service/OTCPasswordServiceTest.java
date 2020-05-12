package no.idporten.minidplus.service;

import no.idporten.domain.sp.ServiceProvider;
import no.idporten.domain.user.MinidUser;
import no.idporten.domain.user.PersonNumber;
import no.idporten.minidplus.exception.IDPortenExceptionID;
import no.idporten.minidplus.exception.minid.MinIDPincodeException;
import no.idporten.minidplus.linkmobility.LINKMobilityClient;
import no.minid.exception.MinidUserNotFoundException;
import no.minid.service.MinIDService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Test
    public void checkOTCCodePositiveTest() throws MinidUserNotFoundException, MinIDPincodeException {
        MinidUser user = new MinidUser();
        user.setCredentialErrorCounter(0);
        user.setPersonNumber(new PersonNumber(pid));
        String sessionId = "123";

        when(minidPlusCache.getSSN(anyString())).thenReturn(pid);
        when(minidPlusCache.getOTP(anyString())).thenReturn("otctest");
        when(minIDService.findByPersonNumber(any())).thenReturn(user);

        assertTrue(otcPasswordService.checkOTCCode(sessionId, "otctest"));
        assert (user.getCredentialErrorCounter() == 0);

    }

    @Test
    public void checkOTCCodeNegativeTest() throws MinidUserNotFoundException, MinIDPincodeException {
        MinidUser user = new MinidUser();
        user.setQuarantineCounter(0);
        user.setPersonNumber(new PersonNumber(pid));
        String sessionId = "123";

        when(minidPlusCache.getSSN(anyString())).thenReturn(pid);
        when(minidPlusCache.getOTP(anyString())).thenReturn("otctest");
        when(minIDService.findByPersonNumber(any())).thenReturn(user);
        assertFalse(otcPasswordService.checkOTCCode("otctestWrong", sessionId));
        assert (user.getQuarantineCounter() == 1);
    }

    @Test
    public void checkOTCCodeNegativeTestLastTry() throws MinidUserNotFoundException, MinIDPincodeException {
        MinidUser user = new MinidUser();
        user.setQuarantineCounter(1);
        user.setPersonNumber(new PersonNumber(pid));
        String sessionId = "123";

        when(minidPlusCache.getSSN(anyString())).thenReturn(pid);
        when(minidPlusCache.getOTP(anyString())).thenReturn("otctest");
        when(minIDService.findByPersonNumber(any())).thenReturn(user);

        assertFalse(otcPasswordService.checkOTCCode("otctestWrong", sessionId));
        assert (user.getQuarantineCounter() == 2);

    }

    @Test
    public void checkOTCCodMaxErrors() throws MinidUserNotFoundException {
        MinidUser user = new MinidUser();
        user.setQuarantineCounter(3);
        user.setPersonNumber(new PersonNumber(pid));

        String sessionId = "123";

        when(minidPlusCache.getSSN(anyString())).thenReturn(pid);
        when(minidPlusCache.getOTP(anyString())).thenReturn("otctest");
        when(minIDService.findByPersonNumber(any())).thenReturn(user);

        try {
            otcPasswordService.checkOTCCode("otctest", sessionId);
            fail("Should have thrown MinIdPincodeException");
        } catch (MinIDPincodeException e) {
            assertEquals(IDPortenExceptionID.IDENTITY_PINCODE_LOCKED, e.getExceptionID());
            assertTrue(user.isOneTimeCodeLocked());
        }
    }

    @Test
    public void checkOTCCodeLocked() throws MinidUserNotFoundException {
        MinidUser user = new MinidUser();
        user.setQuarantineCounter(0);
        user.setQuarantineExpiryDate(Date.from(Clock.systemUTC().instant()));
        user.setOneTimeCodeLocked(true);
        user.setPersonNumber(new PersonNumber(pid));
        String sessionId = "123";

        when(minidPlusCache.getSSN(anyString())).thenReturn(pid);
        when(minidPlusCache.getOTP(anyString())).thenReturn("otctest");
        when(minIDService.findByPersonNumber(any())).thenReturn(user);

        try {
            otcPasswordService.checkOTCCode("otctest", sessionId);
        } catch (MinIDPincodeException e) {
            assertEquals(IDPortenExceptionID.IDENTITY_PINCODE_LOCKED, e.getExceptionID());
            assertTrue(user.isOneTimeCodeLocked());
        }
    }


}
