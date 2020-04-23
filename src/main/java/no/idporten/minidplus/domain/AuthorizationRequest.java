package no.idporten.minidplus.domain;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import no.idporten.minidplus.validator.ValidURI;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * Most of these parameters are being received and returned without being used
 * Should consider keeping the state in idporten instead.
 */
@Data
@NoArgsConstructor
public class AuthorizationRequest implements Serializable {

    /**
     *   Redirect url to something back in idporten...
     **/
    @NotEmpty
    @ValidURI
    private String redirectUrl="";

    /**
     * Id to look up service provider
     **/
    @NotEmpty
    private String spEntityId = "";

    /**
     *   Name of login service
     **/
    @NotEmpty
    private String service="";

    /**
     *   Which security level selector that was used
     **/
    @NotEmpty
    private String startService ="";

    /**
     *   Redirect uri back to service provider
     **/
    @NotEmpty
    @ValidURI
    private String gotoParam="";

    /**
     *   wether to force reauthentication or not.
     *   Default is false
     **/
    @Getter
    private Boolean forceAuth=false;

    /**
     *   Charset to use. Default is UTF-8
     **/
    private String gx_charset="UTF-8";

    /**
     *   Is actually used here
     *   Locale to use. Default is english.
     **/
    private String locale="en"; //uses browser default of not set


    //because goto is a reserved word
    public void setGoto(String gotoValue){
        this.gotoParam = gotoValue;
    }

}
