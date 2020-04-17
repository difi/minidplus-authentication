package no.idporten.minidplus.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.idporten.minidplus.domain.TokenRequest;
import no.idporten.minidplus.domain.TokenResponse;
import no.idporten.minidplus.service.MinidPlusCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Validerer code challenge og returnerer token med fødselsnummer
 */
@RestController
@RequestMapping(value = "/token")
@Slf4j
@RequiredArgsConstructor
public class MinidPlusTokenController {

    private final MinidPlusCache minidPlusCache;

    @Value("${minidplus.token-lifetime-seconds:600}")
    private int expirySeconds;

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity handleAuthorizationCodeGrant(TokenRequest tokenRequest) {
        String sid = tokenRequest.getCode();

        String ssn = minidPlusCache.getSSN(sid);

        if (ssn == null) {
            return ResponseEntity.notFound().build();
        }

        TokenResponse tokenResponse = new TokenResponse(ssn, expirySeconds);

        minidPlusCache.removeSession(sid);
        return ResponseEntity.ok(tokenResponse);
    }

}
