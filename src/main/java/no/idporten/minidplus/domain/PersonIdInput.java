package no.idporten.minidplus.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import no.difi.validation.constraints.Ssn;

@Data
@NoArgsConstructor
public class PersonIdInput implements ModelAttribute {

    @Ssn(message = "{auth.ui.usererror.format.ssn}")
    private String personalIdNumber;

    public void clearValues() {
        this.personalIdNumber = "";
    }

}
