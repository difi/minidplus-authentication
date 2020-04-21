package no.idporten.minidplus.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Property file resource wrapper for MinID.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "sms")
@Component
public class SmsProperties {

    private String sendernumber;
    private int onetimepasswordTtl;
    private String authorizedSendersList;

    private Pswincom pswincom;

    @Data
    public static class Pswincom {
        private String username;
        private String password;
        private String url;

    }

    // Used to keep track of last login
    public static final String HTTP_SESSION_ATTR_MINID_LASTLOGIN = "minid.lastlogin";

}
