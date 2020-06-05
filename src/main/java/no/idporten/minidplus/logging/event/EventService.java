package no.idporten.minidplus.logging.event;

import lombok.RequiredArgsConstructor;
import no.idporten.domain.auth.AuthLevel;
import no.idporten.domain.auth.AuthType;
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

    public void logUserAuthenticated(String serviceprovider, int authLevel, String personIdentifier) {
        eventLogger.log(logAuthEntry(serviceprovider, authLevel, personIdentifier));
    }

    public void logUserPasswordChanged(String personIdentifier) {
        eventLogger.log(logPasswordEntry(personIdentifier));
    }

    private LogEntry logAuthEntry(String serviceProvider, int authLevel, String personIdentifier, LogEntryData... logEntryData) {
        LogEntry logEntry = new LogEntry(MINIDPLUS_AUTHENTICATE_USER);
        logEntry.setIssuer(serviceProvider);
        logEntry.setPersonIdentifier(personIdentifier);
        logEntry.setAuthType(AuthType.MINID_PLUS);
        logEntry.setAuthLevel(AuthLevel.resolve(authLevel));
        logEntry.addAllLogEntryData(Arrays.asList(logEntryData));
        return logEntry;
    }

    private LogEntry logPasswordEntry(String personIdentifier, LogEntryData... logEntryData) {
        LogEntry logEntry = new LogEntry(MINIDPLUS_PASSWORD_CHANGED);
        logEntry.setIssuer(ISSUER_MINIDPLUS);
        logEntry.setPersonIdentifier(personIdentifier);
        logEntry.setAuthType(AuthType.MINID_PLUS);
        logEntry.addAllLogEntryData(Arrays.asList(logEntryData));
        return logEntry;
    }
}
