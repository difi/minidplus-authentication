package no.idporten.minidplus.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import no.idporten.minidplus.validator.ValidatorUtil;

import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
public class PasswordChange {

    @Pattern(message = "{auth.ui.usererror.format.password}", regexp = ValidatorUtil.PWD_LEGAL_CHARS_REGEX)
    private String newPassword;

    @Pattern(message = "{auth.ui.usererror.format.password}", regexp = ValidatorUtil.PWD_LEGAL_CHARS_REGEX)
    private String reenterPassword;
}
