package no.idporten.minidplus.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import no.idporten.minidplus.validator.Password;
import no.idporten.minidplus.validator.ValidatorUtil;

import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
public class PasswordChange implements ModelAttribute {

    @Pattern(message = "{auth.ui.usererror.format.newpassword}", regexp = ValidatorUtil.PWD_LEGAL_CHARS_REGEX)
    @Password
    private String newPassword;

    @Pattern(message = "{auth.ui.usererror.format.newpassword}", regexp = ValidatorUtil.PWD_LEGAL_CHARS_REGEX)
    private String reenterPassword;

    public void clearValues() {
        this.newPassword = "";
        this.reenterPassword = "";
    }
}
