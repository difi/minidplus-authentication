package no.idporten.minidplus.service;

import no.idporten.domain.auth.AuthType;
import no.idporten.minidplus.config.CacheConfiguration;
import no.idporten.minidplus.domain.Authorization;
import no.idporten.minidplus.domain.LevelOfAssurance;
import no.idporten.sdk.oidcserver.protocol.PushedAuthorizationRequest;
import org.ehcache.CacheManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {"spring.cache.type=jcache"})
@ContextConfiguration(classes = {CacheConfiguration.class})
public class MinidPlusCacheTest {

    @Autowired
    private CacheManager cacheManager;

    @Test
    public void getSidSSN() {
        MinidPlusCache minidPlusCache = new MinidPlusCache(cacheManager);
        minidPlusCache.putSSN("123", "cacheHit");
        String cacheHit = minidPlusCache.getSSN("123");
        assertEquals("cacheHit", cacheHit);
    }

    @Test
    public void getSidOTP() {
        MinidPlusCache minidPlusCache = new MinidPlusCache(cacheManager);
        minidPlusCache.putOTP("123", "cacheHit");
        String cacheHit = minidPlusCache.getOTP("123");
        assertEquals("cacheHit", cacheHit);
    }

    @Test
    public void getAuthorizationRequest() {
        MinidPlusCache minidPlusCache = new MinidPlusCache(cacheManager);
        PushedAuthorizationRequest pushedAuthorizationRequest = new PushedAuthorizationRequest(new org.springframework.mock.web.MockHttpServletRequest());
        minidPlusCache.putAuthorizationRequest("uri:test:requestUri", pushedAuthorizationRequest);
        PushedAuthorizationRequest cacheHit = minidPlusCache.getAuthorizationRequest("uri:test:requestUri");
        assertEquals(pushedAuthorizationRequest, cacheHit);
    }

    @Test
    public void getAuthorization() {
        MinidPlusCache minidPlusCache = new MinidPlusCache(cacheManager);
        no.idporten.sdk.oidcserver.protocol.Authorization authorization = new no.idporten.sdk.oidcserver.protocol.Authorization();
        minidPlusCache.putAuthorization("code", authorization);
        no.idporten.sdk.oidcserver.protocol.Authorization cacheHit = minidPlusCache.getAuthorization("code");
        assertEquals(authorization, cacheHit);
    }

    @Test
    public void testRemoveSession() {
        MinidPlusCache minidPlusCache = new MinidPlusCache(cacheManager);
        String sid = "123";

        String ssn = "13094812345";
        String otp = "12345";
        Authorization auth = new Authorization(ssn, LevelOfAssurance.LEVEL4, AuthType.MINID_OTC, 1000);

        minidPlusCache.putSSN(sid, ssn);
        minidPlusCache.putOTP(sid, otp);
        minidPlusCache.putAuthorizationOtp(sid, auth);

        assertEquals(ssn, minidPlusCache.getSSN(sid));
        assertEquals(otp, minidPlusCache.getOTP(sid));
        assertEquals(auth, minidPlusCache.getAuthorizationOtp(sid));

        minidPlusCache.removeSession(sid);

        assertNull(minidPlusCache.getSSN(sid));
        assertNull(minidPlusCache.getOTP(sid));
        assertNull(minidPlusCache.getAuthorizationOtp(sid));
    }
}
