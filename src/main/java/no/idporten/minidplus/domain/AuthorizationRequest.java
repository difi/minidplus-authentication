package no.idporten.minidplus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.idporten.minidplus.spring.ParamName;
import no.idporten.minidplus.validator.ValidAcr;
import no.idporten.minidplus.validator.ValidURI;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
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
     *   Redirect url to something back in idporten...
     **/
    @NotEmpty
    @ValidURI
    @ParamName(HTTP_SESSION_REDIRECT_URI)
    private String redirectUri = "";

    /**
     * Id to look up service provider
     **/
    @NotEmpty
    @ParamName(HTTP_SESSION_CLIENT_ID)
    private String spEntityId = "";

    @ParamName(HTTP_SESSION_RESPONSE_TYPE)
    private String responseType = "authorization_code";  //will always be code atm

    /**
     *   Which security level that was requested
     **/
    @ValidAcr
    @ParamName(HTTP_SESSION_ACR_VALUES)
    private LevelOfAssurance acrValues = LevelOfAssurance.LEVEL3;

    @Pattern(regexp = "^[\\x20-\\x7E]+$", message = "invalid_request")
    @ParamName(HTTP_SESSION_CLIENT_STATE)
    private String state;

    /**
     *   Redirect uri back to service provider
     **/
    @NotEmpty
    @ValidURI
    @ParamName(HTTP_SESSION_GOTO)
    private String gotoParam="";


    /**
     *   Is actually used here
     *   Locale to use. Default is english.
     **/
    @ParamName(HTTP_SESSION_LOCALE)
    private String locale = "en_gb"; //uses browser default of not set

}
