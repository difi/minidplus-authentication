package no.idporten.minidplus.linkmobility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {"minid-plus.sms-filter.filename=difi-mobile-numbers-test.txt"})
public class LINKMobilityClientTest {

    @MockBean(name = "linkmobilityRestTemplate")
    private RestTemplate restTemplate;

    private LINKMobilityClient linkMobilityClient;

    @Autowired
    LINKMobilityProperties linkMobilityProperties;

    @Autowired
    SmsAllowedFilter smsAllowedFilter;

    @BeforeEach
    public void setUp() {
        reset(restTemplate);
        linkMobilityClient = new LINKMobilityClient(restTemplate, linkMobilityProperties, smsAllowedFilter);
    }

    @Test
    @DisplayName("the configured endpoint url is used")
    public void testSendSmsToEndpointUrl() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(ResponseEntity.ok("<STATUS>OK</STATUS>"));
        linkMobilityClient.sendSms("40436656", "test");
        verify(restTemplate).postForEntity(eq(linkMobilityProperties.getUrl()), any(HttpEntity.class), eq(String.class));
    }

    @Test
    @DisplayName("a number not in the whitelist is ignored")
    public void testSendSmsToNotAllowedNumber() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(ResponseEntity.ok("<STATUS>OK</STATUS>"));
        linkMobilityClient.sendSms("99286853", "test");
        verifyNoInteractions(restTemplate);
    }
}
