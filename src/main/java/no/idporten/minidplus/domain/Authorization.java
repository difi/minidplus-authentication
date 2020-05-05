package no.idporten.minidplus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Authorization {
    private String ssn;
    private LevelOfAssurance acrLevel;
    private long createdAtEpochMilli;
}
