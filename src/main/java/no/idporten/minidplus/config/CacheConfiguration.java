package no.idporten.minidplus.config;

import no.idporten.minidplus.domain.Authorization;
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

    @Value("${minid-plus.cache.otp-ttl-in-s:600}")
    private int cacheTTL;

    @Bean
    public CacheManager cacheManager() {
        return CacheManagerBuilder.newCacheManagerBuilder()
                .withCache(SID_SSN,
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
                                ResourcePoolsBuilder.heap(100))
                                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(cacheTTL + 10L)))
                                .build())
                .withCache(SID_OTP,
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
                                ResourcePoolsBuilder.heap(100))
                                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(cacheTTL)))
                                .build())
                .withCache(SID_AUTHORIZATION,
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Authorization.class,
                                ResourcePoolsBuilder.heap(100))
                                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(cacheTTL)))
                                .build())
                .build(true);
    }

}
