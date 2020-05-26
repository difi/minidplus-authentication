package no.idporten.minidplus.linkmobility;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "minid-plus.sms-filter")
@Data
@Validated
public class FilterConfig {
    private String filename;
    private boolean enabled = true;
}
