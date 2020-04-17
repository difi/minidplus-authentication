package no.idporten.minidplus.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class AuthorizationRequest implements Serializable {

    //todo sjekk kriterier for parametre
    @NotEmpty
    private String redirectUrl="";
    private String forceAuth="";
    private String gx_charset="";
    private String locale="";
    private String gotoParam="";
    private String service="";
    private String startService ="";

    //because goto is a reserved word
    public void setGoto(String gotoValue){
        this.gotoParam = gotoValue;
    }

}
