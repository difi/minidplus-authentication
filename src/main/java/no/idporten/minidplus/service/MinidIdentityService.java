package no.idporten.minidplus.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.resilience.CorrelationId;
import no.idporten.domain.user.MinidUser;
import no.idporten.domain.user.PersonNumber;
import no.minid.exception.MinidUserAlreadyExistsException;
import no.minid.exception.MinidUserNotFoundException;
import no.minid.service.MinIDService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MinidIdentityService {

    private final MinIDService minIDService;

    public MinidUser getIdentity(String pid) throws MinidUserNotFoundException {
        MinidUser identity;
        try {
            identity =  findUserFromPid(pid);
        } catch (MinidUserNotFoundException e) {
            warn("User not found. Creating dummy user");
            try {
                identity = minIDService.createDummyUser(new PersonNumber(pid));
            } catch (MinidUserAlreadyExistsException x) {
                //Should never happen
                identity =  findUserFromPid(pid);
            }
        }

        if (identity.getCredentialErrorCounter() == null) {
            identity.setCredentialErrorCounter(0);
        }
        return identity;
    }

    public MinidUser findUserFromPid(String pid) throws MinidUserNotFoundException {
        PersonNumber uid = new PersonNumber(pid);
        MinidUser identity = minIDService.findByPersonNumber(uid);

        if (identity == null) {
            warn("User not found");
            throw new MinidUserNotFoundException("User not found.");
        }
        return identity;
    }

    private void warn(String message) {
        log.warn(CorrelationId.get() + " " + message);
    }
}
