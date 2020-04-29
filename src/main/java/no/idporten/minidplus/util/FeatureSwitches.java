package no.idporten.minidplus.util;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Holds the values of feature switches.
 */
@Data
@Component
public class FeatureSwitches {

    //TODO: denne skal vere default true, hugs å endre i puppet og
    @Value("${features.security-level-check:false}")
    private boolean requestObjectEnabled = false;

}
