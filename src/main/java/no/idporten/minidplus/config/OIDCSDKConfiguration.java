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
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Configuration
@Data
@Slf4j
@ConfigurationProperties(prefix = "oidc-sdk")
public class OIDCSDKConfiguration {

    private URI issuer;
    private List<ClientMetadata> clients;

    private final KeyStoreProvider keyStoreProvider;
    private final JWKConfig jwkConfig;

    public OIDCSDKConfiguration(KeyStoreProvider keyStoreProvider, JWKConfig jwkConfig) {
        this.keyStoreProvider = keyStoreProvider;
        this.jwkConfig = jwkConfig;
    }

    @Bean
    //TODO: Fyll ut hardkodede verdier med riktig konfig
    public OpenIDConnectIntegration openIDConnectIntegrationSpi(MinidPlusCache cache, ResourceLoader resourceLoader) throws Exception {
        ClientMetadata openamMetadata = ClientMetadata.builder().
                clientId("openam")
                .clientSecret("clientsecret")
                .scope("openid")
                .scope("profile")
                .redirectUri("https://eid-atest-web01.dmz.local:443/opensso/UI/minideksternresponse")
                .build();
        ClientMetadata oidcClientMetadata = ClientMetadata.builder().
                clientId("testid")
                .clientSecret("clientsecret")
                .scope("openid")
                .scope("profile")
                .redirectUri("http://localhost:8888/idporten-oidc-client/authorize/response")
                .build();
        OpenIDConnectSdkConfiguration spiConfiguration = OpenIDConnectSdkConfiguration.builder()
                .issuer(issuer)
                .pushedAuthorizationRequestEndpoint(UriComponentsBuilder.fromUri(issuer).path("/par").build().toUri())
                .authorizationEndpoint(UriComponentsBuilder.fromUri(issuer).path("/authorize").build().toUri())
                .tokenEndpoint(UriComponentsBuilder.fromUri(issuer).path("/token").build().toUri())
                .jwksUri(UriComponentsBuilder.fromUri(issuer).path("/jwks").build().toUri())
//                .clients(clients)
                .client(openamMetadata)
                .client(oidcClientMetadata)
                .responseMode("form_post")
                .responseMode("query")
                .acrValue("Level3")
                .uiLocale("nb")
                .uiLocale("nn")
                .uiLocale("en")
                .uiLocale("se")
                .cache(cache)
                .keystore(keyStoreProvider.keyStore(),
                        jwkConfig.getKeystore().getKeyAlias(),
                        jwkConfig.getKeystore().getKeyPassword())
                .build();
        return new OpenIDConnectIntegrationImpl(spiConfiguration);
    }


}


