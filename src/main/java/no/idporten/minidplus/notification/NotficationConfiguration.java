package no.idporten.minidplus.notification;

import no.difi.kontaktinfo.web.util.CustomHttpHeaderNames;
import no.difi.kontaktinfo.web.util.KontaktinfoClientId;
import no.difi.resilience.spring.ResilientRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Configuration
public class NotficationConfiguration {

    @Bean(name = "notifyUserRestTemplate")
    @Autowired
    public RestTemplate notifyUserRestTemplate(Notification notification) {
        RestTemplate template = new ResilientRestTemplate("KR_Kontaktregisteret");
        template.setInterceptors(Collections.singletonList(clientHttpRequestInterceptor()));
        template.setRequestFactory(notifyUserHttpRequestFactory(notification));
        return template;
    }

    protected ClientHttpRequestInterceptor clientHttpRequestInterceptor() {
        return (request, body, execution) -> {
            request.getHeaders().add(CustomHttpHeaderNames.CLIENT_ID, KontaktinfoClientId.MinID.name());
            return execution.execute(request, body);
        };
    }

    protected ClientHttpRequestFactory notifyUserHttpRequestFactory(Notification notification) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(notification.getConnectTimeout());
        factory.setReadTimeout(notification.getReadTimeout());
        return factory;
    }

}
