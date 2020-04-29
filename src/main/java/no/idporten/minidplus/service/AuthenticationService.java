package no.idporten.minidplus.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.resilience.CorrelationId;
import no.idporten.domain.sp.ServiceProvider;
import no.idporten.domain.user.MinidUser;
import no.idporten.domain.user.PersonNumber;
import no.idporten.minidplus.exception.IDPortenExceptionID;
import no.idporten.minidplus.exception.minid.MinIDIncorrectCredentialException;
import no.idporten.minidplus.exception.minid.MinIDInvalidCredentialException;
import no.idporten.minidplus.exception.minid.MinIDSystemException;
import no.idporten.minidplus.exception.minid.MinIDUserNotFoundException;
import no.idporten.minidplus.util.FeatureSwitches;
import no.minid.service.MinIDService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthenticationService {

    private final OTCPasswordService otcPasswordService;

    private final MinIDService minIDService;

    private final MinidPlusCache minidPlusCache;

    private final FeatureSwitches featureSwitches;

    public boolean authenticateUser(String sid, String pid, String password, ServiceProvider sp) throws MinIDUserNotFoundException, MinIDIncorrectCredentialException, MinIDInvalidCredentialException {

        MinidUser identity = findUserFromPid(pid);

        if (identity == null) {
            warn("User not found for ssn=", pid);
            throw new MinIDUserNotFoundException(IDPortenExceptionID.LDAP_ENTRY_NOT_FOUND, "User not found uid=" + pid);
        }
        if (featureSwitches.isRequestObjectEnabled() && !identity.getSecurityLevel().equals("4")) {
            throw new MinIDInvalidCredentialException(IDPortenExceptionID.IDENTITY_INVALID_SECURITY_LEVEL, "User must be level 4 to log in.");
        }
        if (identity.isOneTimeCodeLocked()) {
            warn("One time code is locked for ssn=", pid);
            return false;
        }

        if (!minIDService.validateUserPassword(identity.getPersonNumber(), password)) {
            warn("Password invalid for ssn=", pid);
            throw new MinIDIncorrectCredentialException(IDPortenExceptionID.IDENTITY_PASSWORD_INCORRECT, "Password validation failed");
        }
        minidPlusCache.putSSN(sid, identity.getPersonNumber().getSsn());

        otcPasswordService.sendSMSOtp(sid, sp, identity);

        return true;
    }

    public boolean verifyUserByEmail(String sid) throws MinIDSystemException, MinIDUserNotFoundException {

        String pid = minidPlusCache.getSSN(sid);
        MinidUser identity = findUserFromPid(pid);

        if (identity.getEmail() == null) {
            warn("Email not found not found for user with ssn=", pid);
            throw new MinIDSystemException(IDPortenExceptionID.LDAP_ATTRIBUTE_MISSING, "Email not found not found for user with ssn=" + pid);
        }

        if (identity.isOneTimeCodeLocked()) {
            warn("One time code is locked for ssn=", pid);
            return false;
        }

        otcPasswordService.sendEmailOtp(sid, identity);

        return true;
    }

    public boolean authenticatePid(String sid, String pid, ServiceProvider sp) throws MinIDUserNotFoundException {
        MinidUser identity = findUserFromPid(pid);
        if (identity.isOneTimeCodeLocked()) {
            warn("One time code is locked for ssn=", pid);
            return false;
        }
        minidPlusCache.putSSN(sid, identity.getPersonNumber().getSsn());
        otcPasswordService.sendSMSOtp(sid, sp, identity);
        return true;
    }

    private MinidUser findUserFromPid(String pid) throws MinIDUserNotFoundException {
        PersonNumber uid = new PersonNumber(pid);
        MinidUser identity = minIDService.findByPersonNumber(uid);

        if (identity == null) {
            warn("User not found for ssn=", pid);
            throw new MinIDUserNotFoundException(IDPortenExceptionID.LDAP_ENTRY_NOT_FOUND, "User not found uid=" + pid);
        }
        return identity;
    }

    private void warn(String message, String ssn) {
        log.warn(CorrelationId.get() + " " + ssn + " " + message);
    }

}
