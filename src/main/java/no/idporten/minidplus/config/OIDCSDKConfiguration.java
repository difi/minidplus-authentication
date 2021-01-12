package no.idporten.minidplus.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import no.idporten.minidplus.service.MinidPlusCache;
import no.idporten.minidplus.web.OpenIDConnectIntegrationImpl;
import no.idporten.sdk.oidcserver.OpenIDConnectIntegration;
import no.idporten.sdk.oidcserver.client.ClientMetadata;
import no.idporten.sdk.oidcserver.config.OpenIDConnectSdkConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Configuration
@Data
@Slf4j
@ConfigurationProperties(prefix = "oidc-sdk")
public class OIDCSDKConfiguration {

    private URI issuer;
    private String acr;
    private int parLifetimeSeconds;
    private int authorizationLifetimeSeconds;
    private List<ClientMetadata> clients;

    private final KeyStoreProvider keyStoreProvider;
    private final JWKConfig jwkConfig;

    public OIDCSDKConfiguration(KeyStoreProvider keyStoreProvider, JWKConfig jwkConfig) {
        this.keyStoreProvider = keyStoreProvider;
        this.jwkConfig = jwkConfig;
    }

    @Bean
    public OpenIDConnectIntegration openIDConnectIntegrationSpi(MinidPlusCache cache)  {
        OpenIDConnectSdkConfiguration spiConfiguration = OpenIDConnectSdkConfiguration.builder()
                .issuer(issuer)
                .pushedAuthorizationRequestEndpoint(UriComponentsBuilder.fromUri(issuer).path("/par").build().toUri())
                .authorizationEndpoint(UriComponentsBuilder.fromUri(issuer).path("/authorize").build().toUri())
                .tokenEndpoint(UriComponentsBuilder.fromUri(issuer).path("/token").build().toUri())
                .jwksUri(UriComponentsBuilder.fromUri(issuer).path("/jwks").build().toUri())
                .authorizationRequestLifetimeSeconds(parLifetimeSeconds)
                .authorizationLifetimeSeconds(authorizationLifetimeSeconds)
                .clients(clients)
                .responseMode("query")
                .acrValue(acr)
                .uiLocales(getLocales())
                .cache(cache)
                .keystore(keyStoreProvider.keyStore(),
                        jwkConfig.getKeystore().getKeyAlias(),
                        jwkConfig.getKeystore().getKeyPassword())
                .build();
        return new OpenIDConnectIntegrationImpl(spiConfiguration);
    }

    private List<String> getLocales() {
        return Arrays.asList("nb", "nn", "en", "se");
    }

}


