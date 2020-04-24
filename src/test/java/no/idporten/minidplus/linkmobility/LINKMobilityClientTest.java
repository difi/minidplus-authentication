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

@SpringBootTest
public class LINKMobilityClientTest {

    @MockBean(name = "linkmobilityRestTemplate")
    private RestTemplate restTemplate;

    private LINKMobilityClient linkMobilityClient;

    @Autowired
    LINKMobilityProperties linkMobilityProperties;

    @BeforeEach
    public void setUp() {
        reset(restTemplate);
        linkMobilityClient = new LINKMobilityClient(restTemplate, linkMobilityProperties);
    }

    @Test
    @DisplayName("the configured endpoint url is used")
    public void testSendSmsToEndpointUrl() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(ResponseEntity.ok("<STATUS>OK</STATUS>"));
        linkMobilityClient.sendSms("", "");
        verify(restTemplate).postForEntity(eq(linkMobilityProperties.getUrl()), any(HttpEntity.class), eq(String.class));
    }
}
