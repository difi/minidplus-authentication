package no.idporten.minidplus.linkmobility;

import no.difi.resilience.spring.ResilientRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class LINKMobilityConfiguration {

    @Bean(name = "linkmobilityRestTemplate")
    @Autowired
    public RestTemplate restTemplate(LINKMobilityProperties linkMobilityProperties) {
        RestTemplate template = new ResilientRestTemplate("SMS_PSWinCom SMS Gateway");
        template.setRequestFactory(notifyUserHttpRequestFactory(linkMobilityProperties));
        return template;
    }


    protected ClientHttpRequestFactory notifyUserHttpRequestFactory(LINKMobilityProperties linkMobilityProperties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(linkMobilityProperties.getConnectTimeout());
        factory.setReadTimeout(linkMobilityProperties.getReadTimeout());
        return factory;
    }

}
