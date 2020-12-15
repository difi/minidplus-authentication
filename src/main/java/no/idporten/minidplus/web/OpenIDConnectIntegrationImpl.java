package no.idporten.minidplus.web;

import lombok.extern.slf4j.Slf4j;
import no.idporten.sdk.oidcserver.OpenIDConnectIntegrationBase;
import no.idporten.sdk.oidcserver.client.ClientMetadata;
import no.idporten.sdk.oidcserver.config.OpenIDConnectSdkConfiguration;
import no.idporten.sdk.oidcserver.protocol.PushedAuthorizationRequest;

@Slf4j
public class OpenIDConnectIntegrationImpl extends OpenIDConnectIntegrationBase {
    public OpenIDConnectIntegrationImpl(OpenIDConnectSdkConfiguration sdkConfiguration) {
        super(sdkConfiguration);
    }

    @Override
    public void validate(PushedAuthorizationRequest authorizationRequest, ClientMetadata clientMetadata) {
        super.validate(authorizationRequest, clientMetadata);
        //TODO: valider resten
    }

}
