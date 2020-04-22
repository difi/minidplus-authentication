package no.idporten.minidplus.service;

import no.idporten.domain.user.MinidUser;
import no.idporten.minidplus.config.CacheConfiguration;
import no.minid.exception.MinidUserNotFoundException;
import no.minid.service.MinIDService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {"spring.cache.type=jcache"})
public class AuthenticationServiceTest {

    @MockBean
    private MinidPlusCache minidPlusCache;

    @MockBean
    private MinIDService minIDService;

    @Autowired
    private AuthenticationService authenticationService;

    @Test
    public void checkOTCCodePositiveTest() throws MinidUserNotFoundException {
        MinidUser user = new MinidUser();
        user.setCredentialErrorCounter(0);
        String sessionId = "123";

        when(minidPlusCache.getSSN(anyString())).thenReturn("12345678910");
        when(minidPlusCache.getOTP(anyString())).thenReturn("otctest");
        when(minIDService.findByPersonNumber(any())).thenReturn(user);

        String result = authenticationService.checkOTCCode("otctest", sessionId);
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
}
