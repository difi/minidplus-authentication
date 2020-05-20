package no.idporten.minidplus.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import no.difi.validation.constraints.Ssn;
import no.idporten.minidplus.validator.Password;

@Data
@NoArgsConstructor
public class UserCredentials implements ModelAttribute {

    @Ssn(message = "{auth.ui.usererror.format.ssn}")
    private String personalIdNumber;

    @Password(message = "{auth.ui.usererror.format.password}")
    private String password;

    public void clearValues() {
        this.personalIdNumber = "";
        this.password = "";
    }
}
