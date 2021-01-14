package no.idporten.minidplus.web;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import no.idporten.sdk.oidcserver.OpenIDConnectIntegration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/v2")
@Controller
@CrossOrigin(origins = "*")
@AllArgsConstructor
@NoArgsConstructor
public class OpenIDMetadataController {

    @Autowired
    OpenIDConnectIntegration openIDConnectIntegration;

    @RequestMapping(value = "/.well-known/openid-configuration", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity openIDConfiguration() {
        return ResponseEntity.ok().body(openIDConnectIntegration.getOpenIDProviderMetadata());
    }

    @RequestMapping(value = {"/jwk", "/jwks", "/.well-known/jwks.json"}, produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> jwks() {
        return ResponseEntity.ok().body(openIDConnectIntegration.getPublicJWKSet().toString());
    }
}
