package no.idporten.minidplus.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import no.difi.resilience.CorrelationId;

@SuppressWarnings("unused")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ErrorResponse {

    @JsonProperty(value = "error")
    private String error;
    @JsonProperty(value = "error_description")
    private String errorDescription;
    @JsonProperty(value = "state")
    private String state;

    @Builder
    ErrorResponse(String error, String errorDescription, String state) {
        this.error = error;
        this.errorDescription = (errorDescription == null ? "" : errorDescription + " ") + "(correlation id: " + CorrelationId.get() + ")";
        this.state = state;
    }

}
