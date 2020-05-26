package no.idporten.minidplus.linkmobility;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "linkmobility")
@Data
@Validated
@Component
public class LINKMobilityProperties {

    private String url;
    private String account;
    private String password;
    private int readTimeout;
    private int connectTimeout;
    private String sender;
    private int ttl;
}