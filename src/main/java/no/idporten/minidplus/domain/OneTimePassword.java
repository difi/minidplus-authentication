package no.idporten.minidplus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OneTimePassword {

    @Size(min=5, max=5, message = "{auth.ui.usererror.format.otc}")
    private String otpCode;

}
