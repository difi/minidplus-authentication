package no.idporten.minidplus.notification;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "email")
public class Notification {

    private String url;
    private int readTimeout;
    private int connectTimeout;
}