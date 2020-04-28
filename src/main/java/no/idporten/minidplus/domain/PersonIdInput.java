package no.idporten.minidplus.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import no.difi.validation.constraints.Ssn;

@Data
@NoArgsConstructor
public class PersonIdInput {

    @Ssn(message = "{no.idporten.module.minidplus.input.personalidnumber.error}")
    private String personalIdNumber;

}
