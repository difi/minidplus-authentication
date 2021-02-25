package no.idporten.minidplus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Authorization implements Serializable {
    private String ssn;
    private LevelOfAssurance acrLevel;
    private long createdAtEpochMilli;
}
