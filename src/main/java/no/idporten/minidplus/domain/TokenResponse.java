package no.idporten.minidplus.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@SuppressWarnings("unused")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@AllArgsConstructor
@Builder
public class TokenResponse {
    @JsonProperty("ssn")
    private String ssn;

    //seconds
    @JsonProperty("expires_in")
    private long expiresIn;

    @JsonProperty("acr_level")
    private String acrLevelExternalName;

    @JsonProperty("auth_type")
    private String authType;

}
