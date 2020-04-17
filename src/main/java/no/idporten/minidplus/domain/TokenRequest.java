package no.idporten.minidplus.domain;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@AllArgsConstructor
@Builder
public class TokenRequest {

    @JsonProperty("grant_type")
    public String grantType;

    @JsonProperty("code")
    public String code;
}
