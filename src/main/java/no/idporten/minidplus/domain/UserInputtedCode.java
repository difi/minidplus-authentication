package no.idporten.minidplus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInputtedCode implements ModelAttribute {

    @Size(min = 5, max = 5, message = "{auth.ui.usererror.format.otc}")
    private String otpCode;

    private int pinCodeNumber;

    public void clearValues() {
        this.otpCode = "";
    }
}
