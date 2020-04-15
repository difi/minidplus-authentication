package no.idporten.minidplus.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class UserCredentials {

    @Size(min=11, max = 11, message = "{no.idporten.module.minidplus.input.personalidnumber.error}")
    private String personalIdNumber;

    @NotEmpty(message = "{no.idporten.module.minidplus.input.password.empty.error}")
    private String password;
}
