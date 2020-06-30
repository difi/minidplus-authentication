package no.idporten.minidplus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.idporten.minidplus.spring.ParamName;
import no.idporten.minidplus.validator.ValidAcr;
import no.idporten.minidplus.validator.ValidURI;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

import static no.idporten.minidplus.domain.MinidPlusSessionAttributes.*;

/**
 * Most of these parameters are being received and returned without being used
 * Should consider keeping the state in idporten instead.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationRequest implements Serializable {

    /**
     * Redirect url to something back in idporten...
     **/
    @NotEmpty
    @ValidURI(message = "{no.minidplus.validuri}")
    @ParamName(HTTP_SESSION_REDIRECT_URI)
    private String redirectUri = "";

    /**
     * Id to look up service provider
     **/
    @NotEmpty
    @ParamName(HTTP_SESSION_CLIENT_ID)
    @Pattern(regexp = "^[\\x20-\\x7E]+$", message = "invalid_request")
    @Size(max = 255, message = "Please enter at most {max} characters")
    private String spEntityId = "";

    @ParamName(HTTP_SESSION_RESPONSE_TYPE)
    @Pattern(regexp = "^[a-zA-Z_]*$", message = "invalid_request")
    private String responseType = "authorization_code";  //will always be code atm

    /**
     * Which security level that was requested
     **/
    @ValidAcr
    @ParamName(HTTP_SESSION_ACR_VALUES)
    private LevelOfAssurance acrValues = LevelOfAssurance.LEVEL4;

    @Pattern(regexp = "^[\\x20-\\x7E]+$", message = "invalid_request")
    @ParamName(HTTP_SESSION_CLIENT_STATE)
    private String state;

    /**
     * Redirect uri back to service provider, used internally in idporten
     **/
    @ValidURI(message = "{no.minidplus.validuri}")
    @ParamName(HTTP_SESSION_GOTO)
    private String gotoParam = "";


    /**
     * Is actually used here
     * Locale to use. Default is english.
     **/
    @ParamName(HTTP_SESSION_LOCALE)
    @Size(max = 2, message = "Please enter at most {max} characters")
    @Pattern(regexp = "^[a-zA-Z]*$", message = "invalid_request")
    private String locale = "en"; //uses browser default of not set

}
