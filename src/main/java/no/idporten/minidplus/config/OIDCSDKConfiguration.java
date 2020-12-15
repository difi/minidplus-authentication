package no.idporten.minidplus.config;

import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import no.idporten.minidplus.service.MinidPlusCache;
import no.idporten.sdk.oidcserver.OpenIDConnectIntegration;
import no.idporten.sdk.oidcserver.OpenIDConnectIntegrationBase;
import no.idporten.sdk.oidcserver.client.ClientMetadata;
import no.idporten.sdk.oidcserver.config.OpenIDConnectSdkConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class OIDCSDKConfiguration {

    @Bean
    //TODO: Fyll ut hardkodede verdier med riktig konfig
    public OpenIDConnectIntegration openIDConnectIntegrationSpi(MinidPlusCache cache) throws Exception {
        ClientMetadata openamMetadata = ClientMetadata.builder().
                clientId("openam")
                .clientSecret("clientsecret")
                .scope("openid")
                .redirectUri("http://localhost:8888/idporten-oidc-client/authorize/response")
                .build();
        ClientMetadata oidcClientMetadata = ClientMetadata.builder().
                clientId("openam")
                .clientSecret("clientsecret")
                .scope("openid")
                .redirectUri("http://localhost:8888/idporten-oidc-client/authorize/response")
                .build();
        RSAKey jwk = new RSAKeyGenerator(2048)
                .keyUse(KeyUse.SIGNATURE)
                .keyID(UUID.randomUUID().toString())
                .generate();
        OpenIDConnectSdkConfiguration spiConfiguration = OpenIDConnectSdkConfiguration.builder()
                .issuer("http://localhost:7071/testid/")
                .client(openamMetadata)
                .client(oidcClientMetadata)
                .cache(cache)
                .jwk(jwk)
                .build();
        return new OpenIDConnectIntegrationBase(spiConfiguration);
    }

}
