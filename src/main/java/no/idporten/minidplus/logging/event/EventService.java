package no.idporten.minidplus.logging.event;

import lombok.RequiredArgsConstructor;
import no.idporten.domain.log.LogEntry;
import no.idporten.domain.log.LogEntryData;
import no.idporten.domain.log.LogEntryLogType;
import no.idporten.log.event.EventLogger;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class EventService {

    private static final String ISSUER_MINIDPLUS = "MinID-Plus";

    static final LogEntryLogType MINIDPLUS_AUTHENTICATE_USER = new LogEntryLogType("MINIDPLUS_AUTHENTICATE_USER", "MinIDPlus user authenticated");
    static final LogEntryLogType MINIDPLUS_PASSWORD_CHANGED = new LogEntryLogType("MINIDPLUS_PASSWORD_CHANGED", "MinIDPlus user changed password");

    private final EventLogger eventLogger;

    public void logUserAuthenticated(String personIdentifier) {
        eventLogger.log(logEntry(MINIDPLUS_AUTHENTICATE_USER, personIdentifier));
    }

    public void logUserPasswordChanged(String personIdentifier) {
        eventLogger.log(logEntry(MINIDPLUS_PASSWORD_CHANGED, personIdentifier));
    }


    private LogEntry logEntry(LogEntryLogType logType, String personIdentifier, LogEntryData... logEntryData) {
        LogEntry logEntry = new LogEntry(logType);
        logEntry.setIssuer(ISSUER_MINIDPLUS);
        logEntry.setPersonIdentifier(personIdentifier);
        logEntry.addAllLogEntryData(Arrays.asList(logEntryData));
        return logEntry;
    }

}
