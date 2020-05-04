package no.idporten.minidplus.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.resilience.CorrelationId;
import no.idporten.domain.sp.ServiceProvider;
import no.idporten.domain.user.MinidUser;
import no.idporten.domain.user.PersonNumber;
import no.idporten.log.audit.AuditLogger;
import no.idporten.minidplus.exception.IDPortenExceptionID;
import no.idporten.minidplus.exception.minid.MinIDIncorrectCredentialException;
import no.idporten.minidplus.exception.minid.MinIDInvalidCredentialException;
import no.idporten.minidplus.exception.minid.MinIDPincodeException;
import no.idporten.minidplus.exception.minid.MinIDSystemException;
import no.idporten.minidplus.logging.audit.AuditID;
import no.idporten.minidplus.logging.event.EventService;
import no.idporten.minidplus.util.FeatureSwitches;
import no.minid.exception.MinidUserNotFoundException;
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

    private final AuditLogger auditLogger;

    private final EventService eventService;

    public boolean authenticateUser(String sid, String pid, String password, ServiceProvider sp) throws MinidUserNotFoundException, MinIDIncorrectCredentialException, MinIDInvalidCredentialException, MinIDSystemException {

        MinidUser identity = findUserFromPid(pid);

        if (identity == null) {
            warn("User not found for user");
            throw new MinidUserNotFoundException("User not found uid=" + pid);
        }
        if (featureSwitches.isRequestObjectEnabled() && !identity.getSecurityLevel().equals("4")) {
            throw new MinIDInvalidCredentialException(IDPortenExceptionID.IDENTITY_INVALID_SECURITY_LEVEL, "User must be level 4 to log in.");
        }
        if (identity.isOneTimeCodeLocked()) {
            warn("One time code is locked for user");
            return false;
        }

        if (!minIDService.validateUserPassword(identity.getPersonNumber(), password)) {
            warn("Password invalid for user");
            throw new MinIDIncorrectCredentialException(IDPortenExceptionID.IDENTITY_PASSWORD_INCORRECT, "Password validation failed");
        }
        minidPlusCache.putSSN(sid, identity.getPersonNumber().getSsn());

        otcPasswordService.sendSMSOtp(sid, sp, identity);

        return true;
    }

    public boolean verifyUserByEmail(String sid) throws MinIDSystemException, MinidUserNotFoundException {

        String pid = minidPlusCache.getSSN(sid);
        MinidUser identity = findUserFromPid(pid);

        if (identity.getEmail() == null) {
            warn("Email not found not found for user");
            throw new MinIDSystemException(IDPortenExceptionID.LDAP_ATTRIBUTE_MISSING, "Email not found not found for user");
        }

        if (identity.isOneTimeCodeLocked()) {
            warn("One time code is locked for user");
            return false;
        }

        otcPasswordService.sendEmailOtp(sid, identity);

        return true;
    }

    public boolean changePassword(String sid, String password) throws MinidUserNotFoundException {
        String pid = minidPlusCache.getSSN(sid);
        minIDService.updatePassword(new PersonNumber(pid), password);
        auditLogger.log(AuditID.PASSWORD_CHANGED.auditId(), null, pid, CorrelationId.get());
        eventService.logUserPasswordChanged(pid);
        return true;
    }

    public boolean authenticatePid(String sid, String pid, ServiceProvider sp) throws MinidUserNotFoundException, MinIDSystemException {
        MinidUser identity = findUserFromPid(pid);
        if (identity.isOneTimeCodeLocked()) {
            warn("One time code is locked");
            return false;
        }
        minidPlusCache.putSSN(sid, identity.getPersonNumber().getSsn());
        otcPasswordService.sendSMSOtp(sid, sp, identity);
        return true;
    }

    public boolean authenticateOtpStep(String sid, String inputOneTimeCode) throws MinidUserNotFoundException, MinIDPincodeException {
        if (otcPasswordService.checkOTCCode(sid, inputOneTimeCode)) {
            eventService.logUserAuthenticated(minidPlusCache.getSSN(sid));
            return true;
        }
        return false;

    }

    private MinidUser findUserFromPid(String pid) throws MinidUserNotFoundException {
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
