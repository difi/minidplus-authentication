package no.idporten.minidplus.service;

import lombok.RequiredArgsConstructor;
import no.idporten.minidplus.domain.Authorization;
import no.idporten.sdk.oidcserver.cache.CacheSpi;
import no.idporten.sdk.oidcserver.protocol.PushedAuthorizationRequest;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.springframework.stereotype.Component;

import static no.idporten.minidplus.config.CacheConfiguration.*;

@Component
@RequiredArgsConstructor
public class MinidPlusCache implements CacheSpi {

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

    @Override
    public void putAuthorizationRequest(String requestUri, PushedAuthorizationRequest pushedAuthorizationRequest) {
        Cache<String, PushedAuthorizationRequest> authReqCache = cacheManager.getCache(SDK_AUTHORIZATION_REQUEST, String.class, PushedAuthorizationRequest.class);
        authReqCache.put(requestUri, pushedAuthorizationRequest);
    }

    @Override
    public PushedAuthorizationRequest getAuthorizationRequest(String requestUri) {
        Cache<String, PushedAuthorizationRequest> authReqCache = cacheManager.getCache(SDK_AUTHORIZATION_REQUEST, String.class, PushedAuthorizationRequest.class);
        return authReqCache.get(requestUri);
    }

    @Override
    public void removeAuthorizationRequest(String requestUri) {
        Cache<String, PushedAuthorizationRequest> authReqCache = cacheManager.getCache(SDK_AUTHORIZATION_REQUEST, String.class, PushedAuthorizationRequest.class);
        authReqCache.remove(requestUri);
    }

    @Override
    public void putAuthorization(String code, no.idporten.sdk.oidcserver.protocol.Authorization authorization) {
        Cache<String, no.idporten.sdk.oidcserver.protocol.Authorization> authCache = cacheManager.getCache(SDK_AUTHORIZATION, String.class, no.idporten.sdk.oidcserver.protocol.Authorization.class);
        authCache.put(code, authorization);
    }

    @Override
    public no.idporten.sdk.oidcserver.protocol.Authorization getAuthorization(String code) {
        Cache<String, no.idporten.sdk.oidcserver.protocol.Authorization> authCache = cacheManager.getCache(SDK_AUTHORIZATION, String.class, no.idporten.sdk.oidcserver.protocol.Authorization.class);
        return authCache.get(code);
    }

    @Override
    public void removeAuthorization(String code) {
        Cache<String, no.idporten.sdk.oidcserver.protocol.Authorization> authCache = cacheManager.getCache(SDK_AUTHORIZATION, String.class, no.idporten.sdk.oidcserver.protocol.Authorization.class);
        authCache.remove(code);

    }

    public no.idporten.minidplus.domain.Authorization getAuthorizationOtp(String sid) {
        Cache<String, Authorization> otpCache = cacheManager.getCache(SID_AUTHORIZATION, String.class, Authorization.class);
        return otpCache.get(sid);
    }

    public void putAuthorizationOtp(String sid, Authorization otp) {
        Cache<String, Authorization> otpCache = cacheManager.getCache(SID_AUTHORIZATION, String.class, Authorization.class);
        otpCache.put(sid, otp);
    }

    public void removeAuthorizationOtp(String sid) {
        Cache<String, Authorization> otpCache = cacheManager.getCache(SID_AUTHORIZATION, String.class, Authorization.class);
        otpCache.remove(sid);
    }

    public void removeSession(String sid) {
        removeSSN(sid);
        removeOTP(sid);
        removeAuthorizationOtp(sid);
    }

}

