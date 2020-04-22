package no.idporten.minidplus.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import no.difi.validation.constraints.Ssn;
import no.idporten.minidplus.validator.ValidatorUtil;

import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
public class UserCredentials {

    @Ssn(message = "{no.idporten.module.minidplus.input.personalidnumber.error}")
    private String personalIdNumber;

    @Pattern(message = "{auth.ui.usererror.format.password}", regexp = ValidatorUtil.PWD_LEGAL_CHARS_REGEX)
    private String password;
}
