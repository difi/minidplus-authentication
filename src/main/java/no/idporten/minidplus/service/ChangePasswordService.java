package no.idporten.minidplus.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.resilience.CorrelationId;
import no.minid.service.MinIDService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class ChangePasswordService {

    private final OTCPasswordService otcPasswordService;

    private final MinIDService minIDService;

    private final MinidPlusCache minidPlusCache;


    private void warn(String message, String ssn) {
        log.warn(CorrelationId.get() + " " + ssn + " " + message);
    }

}
