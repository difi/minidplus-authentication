package no.idporten.minidplus.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.resilience.CorrelationId;
import no.idporten.minidplus.domain.Authorization;
import no.idporten.minidplus.domain.ErrorResponse;
import no.idporten.minidplus.domain.TokenRequest;
import no.idporten.minidplus.domain.TokenResponse;
import no.idporten.minidplus.logging.audit.AuditID;
import no.idporten.minidplus.logging.audit.AuditMessage;
import no.idporten.minidplus.service.MinidPlusCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Validerer code challenge og returnerer token med
 *  - fødselsnummer
 *  - acrLevel
 *  - expiry
 */
@RestController
@RequestMapping(value = "/token")
@Slf4j
@RequiredArgsConstructor
public class MinIdPlusTokenController {

    private static final String INVALID_GRANT = "invalid_grant";
    private final MinidPlusCache minidPlusCache;

    @Value("${minidplus.token-lifetime-seconds:600}")
    private int tokenExpirySeconds;

    @Value("${minid-plus.cache.code-ttl-in-s:60}")
    private int codeExpirySeconds;

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @AuditMessage(AuditID.TOKEN_CREATED)
    public ResponseEntity handleAuthorizationCodeGrant(TokenRequest tokenRequest) {
        String sid = tokenRequest.getCode();

        Authorization authorization = minidPlusCache.getAuthorization(sid);

        if (authorization == null || hasExpired(authorization)) {
            warn("Code not found or expired for code=" + sid);
            return createErrorResponseEntitiy(HttpStatus.BAD_REQUEST, INVALID_GRANT, "The provided authorization grant is invalid or expired");
        }

        TokenResponse tokenResponse = TokenResponse.builder()
                .ssn(authorization.getSsn())
                .acrLevelExternalName(authorization.getAcrLevel().getExternalName())
                .expiresIn(tokenExpirySeconds)
                .build();

        minidPlusCache.removeSession(sid);
        return ResponseEntity.ok(tokenResponse);
    }

    private boolean hasExpired(Authorization authorization) {
        long timestamp = Instant.now().toEpochMilli();
        return authorization.getCreatedAtEpochMilli() > timestamp && (timestamp > authorization.getCreatedAtEpochMilli() + codeExpirySeconds * 1000);
    }

    private void warn(String message) {
        log.warn(CorrelationId.get() + " " + message);
    }

    private ResponseEntity<ErrorResponse> createErrorResponseEntitiy(HttpStatus httpStatus, String error, String errorMessage) {
        return ResponseEntity
                .status(httpStatus)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.builder()
                        .error(error)
                        .errorDescription(errorMessage)
                        .build());
    }
}
