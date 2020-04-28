package no.idporten.minidplus.notification;

import no.difi.eid.integration.dto.MailMessageResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificationClient {

    private RestTemplate restTemplate;

    @Autowired
    private Notification notification;

    @Autowired
    public NotificationClient(@Qualifier("notifyUserRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendEmail(String to, String subject, String message) {
        restTemplate.put(notification.getUrl(), httpEntity(new MailMessageResource(to, subject, message)));
    }

    private <T> HttpEntity<T> httpEntity(T object) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        return new HttpEntity(object, headers);
    }

}
