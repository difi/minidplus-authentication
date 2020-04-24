package no.idporten.minidplus.linkmobility;

import no.difi.validation.MobileValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LINKMobilityClient {

    private RestTemplate restTemplate;
    private LINKMobilityProperties linkMobilityProperties;

    @Autowired
    public LINKMobilityClient(@Qualifier("linkmobilityRestTemplate") RestTemplate restTemplate, LINKMobilityProperties linkMobilityProperties) {
        this.restTemplate = restTemplate;
        this.linkMobilityProperties = linkMobilityProperties;
    }

    public void sendSms(String mobile, String message) {
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                linkMobilityProperties.getUrl(),
                httpEntity(payload(linkMobilityProperties.getSender(), mobile, message)),
                String.class);
        if (responseEntity == null
                || responseEntity.getStatusCode() != HttpStatus.OK
                || responseEntity.getBody() == null
                || !responseEntity.getBody().contains("<STATUS>OK</STATUS>")) {
            throw new RuntimeException(String.format("SMS sending failed: %s", responseEntity != null ? responseEntity.getBody() : "<empty response>"));
        }
    }

    private <T> HttpEntity<T> httpEntity(T object) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.TEXT_XML_VALUE);
        return new HttpEntity(object, headers);
    }

    protected String payload(String from, String to, String text) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\"?><!DOCTYPE SESSION SYSTEM \"pswincom_submit.dtd\"><SESSION><CLIENT>");
        sb.append(linkMobilityProperties.getAccount());
        sb.append("</CLIENT><PW>");
        sb.append(linkMobilityProperties.getPassword());
        sb.append("</PW><MSGLST><MSG><TEXT>");
        sb.append(text);
        sb.append("</TEXT><RCV>");
        sb.append(MobileValidator.numberCleaner(to));
        sb.append("</RCV><SND>");
        sb.append(from);
        sb.append("</SND></MSG></MSGLST></SESSION>");
        return sb.toString();
    }

}
