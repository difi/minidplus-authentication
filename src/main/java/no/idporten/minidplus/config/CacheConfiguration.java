package no.idporten.minidplus.config;

import no.idporten.minidplus.domain.Authorization;
import no.idporten.sdk.oidcserver.protocol.PushedAuthorizationRequest;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConditionalOnExpression("'${spring.cache.type}'!='none'")
public class CacheConfiguration {

    public static final String SID_OTP = "sidOTP";
    public static final String SID_SSN = "sidSSN";
    public static final String SID_AUTHORIZATION = "sidAuth";
    public static final String SDK_AUTHORIZATION = "sdkAuth";
    public static final String SDK_AUTHORIZATION_REQUEST = "sdkAuthRequest";

    @Value("${minid-plus.cache.otp-ttl-in-s:600}")
    private int otpCacheTTL;

    @Value("${minid-plus.cache.session-ttl-in-s:1800}")
    private int sessionCacheTTL;

    @Bean
    public CacheManager cacheManager() {
        return CacheManagerBuilder.newCacheManagerBuilder()
                .withCache(SID_SSN,
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
                                ResourcePoolsBuilder.heap(100))
                                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(sessionCacheTTL)))
                                .build())
                .withCache(SID_OTP,
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
                                ResourcePoolsBuilder.heap(100))
                                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(otpCacheTTL)))
                                .build())
                .withCache(SID_AUTHORIZATION,
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Authorization.class,
                                ResourcePoolsBuilder.heap(100))
                                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(sessionCacheTTL)))
                                .build())
                .withCache(SDK_AUTHORIZATION,
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, no.idporten.sdk.oidcserver.protocol.Authorization.class,
                                ResourcePoolsBuilder.heap(100))
                                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(sessionCacheTTL)))
                                .build())
                .withCache(SDK_AUTHORIZATION_REQUEST,
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, PushedAuthorizationRequest.class,
                                ResourcePoolsBuilder.heap(100))
                                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(sessionCacheTTL)))
                                .build())
                .build(true);
    }

}
