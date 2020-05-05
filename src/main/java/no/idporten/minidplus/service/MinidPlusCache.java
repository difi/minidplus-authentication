package no.idporten.minidplus.service;

import lombok.RequiredArgsConstructor;
import no.idporten.minidplus.domain.Authorization;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.springframework.stereotype.Component;

import static no.idporten.minidplus.config.CacheConfiguration.*;

@Component
@RequiredArgsConstructor
public class MinidPlusCache {

    private final CacheManager cacheManager;

    public String getSSN(String sid) {
        Cache<String, String> sidSSNCache = cacheManager.getCache(SID_SSN, String.class, String.class);
        return sidSSNCache.get(sid);
    }

    public void putSSN(String sid, String ssn) {
        Cache<String, String> sidSSNCache = cacheManager.getCache(SID_SSN, String.class, String.class);
        sidSSNCache.put(sid, ssn);
    }

    public void removeSSN(String sid) {
        Cache<String, String> otpCache = cacheManager.getCache(SID_SSN, String.class, String.class);
        otpCache.remove(sid);
    }

    public String getOTP(String sid){
        Cache<String, String> otpCache = cacheManager.getCache(SID_OTP, String.class, String.class);
        return otpCache.get(sid);
    }

    public void putOTP(String sid, String otp) {
        Cache<String, String> otpCache = cacheManager.getCache(SID_OTP, String.class, String.class);
        otpCache.put(sid, otp);
    }

    public void removeOTP(String sid) {
        Cache<String, String> otpCache = cacheManager.getCache(SID_OTP, String.class, String.class);
        otpCache.remove(sid);
    }

    public Authorization getAuthorization(String sid) {
        Cache<String, Authorization> otpCache = cacheManager.getCache(SID_AUTHORIZATION, String.class, Authorization.class);
        return otpCache.get(sid);
    }

    public void putAuthorization(String sid, Authorization otp) {
        Cache<String, Authorization> otpCache = cacheManager.getCache(SID_AUTHORIZATION, String.class, Authorization.class);
        otpCache.put(sid, otp);
    }

    public void removeAuthorization(String sid) {
        Cache<String, Authorization> otpCache = cacheManager.getCache(SID_AUTHORIZATION, String.class, Authorization.class);
        otpCache.remove(sid);
    }

    public void removeSession(String sid) {
        removeSSN(sid);
        removeOTP(sid);
        removeAuthorization(sid);
    }

}

