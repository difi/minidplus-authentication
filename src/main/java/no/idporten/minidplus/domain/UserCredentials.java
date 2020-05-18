package no.idporten.minidplus.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import no.difi.validation.constraints.Ssn;
import no.idporten.minidplus.validator.Password;
import no.idporten.minidplus.validator.ValidatorUtil;

import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
public class UserCredentials {

    @Ssn(message = "{auth.ui.usererror.format.ssn}")
    private String personalIdNumber;

    @Password(message = "{auth.ui.usererror.format.password}")
    private String password;
}
