package no.idporten.minidplus.logging.event;

import no.idporten.domain.log.LogEntry;
import no.idporten.log.event.EventLogger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DisplayName("When event logging")
public class EventServiceTest {

    @MockBean
    private EventLogger eventLogger;

    @Autowired
    private EventService eventService;

    @Captor
    private ArgumentCaptor<LogEntry> logEntryCaptor;

    @Test
    @DisplayName("authentication of user then person identifier is logged")
    public void testLogAuthenticateUser() {
        String personIdentifier = "xxx";
        String serviceprovider = "Martas Corona Utsalg";
        int authlevel = 3;
        eventService.logUserAuthenticated(serviceprovider, authlevel, personIdentifier);
        verify(eventLogger).log(logEntryCaptor.capture());
        assertEquals(EventService.MINIDPLUS_AUTHENTICATE_USER, logEntryCaptor.getValue().getLogType());
        assertEquals(personIdentifier, logEntryCaptor.getValue().getPersonIdentifierString());
        assertEquals(serviceprovider, logEntryCaptor.getValue().getIssuer());
        assertEquals(authlevel, logEntryCaptor.getValue().getAuthLevel().getLevel());
    }

    @Test
    @DisplayName("complete password changed then person identifier is logged")
    public void testLogCompletePasswordChange() {
        String personIdentifier = "xxx";
        eventService.logUserPasswordChanged(personIdentifier);
        verify(eventLogger).log(logEntryCaptor.capture());
        assertEquals(EventService.MINIDPLUS_PASSWORD_CHANGED, logEntryCaptor.getValue().getLogType());
        assertEquals(personIdentifier, logEntryCaptor.getValue().getPersonIdentifierString());
        assertEquals("MinID-Plus", logEntryCaptor.getValue().getIssuer());
    }

}
