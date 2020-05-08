package no.idporten.minidplus.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.resilience.CorrelationId;
import no.idporten.domain.sp.ServiceProvider;
import no.idporten.domain.user.MinidUser;
import no.idporten.domain.user.PersonNumber;
import no.idporten.log.audit.AuditLogger;
import no.idporten.minidplus.domain.Authorization;
import no.idporten.minidplus.domain.LevelOfAssurance;
import no.idporten.minidplus.exception.IDPortenExceptionID;
import no.idporten.minidplus.exception.minid.MinIDIncorrectCredentialException;
import no.idporten.minidplus.exception.minid.MinIDInvalidAcrLevelException;
import no.idporten.minidplus.exception.minid.MinIDPincodeException;
import no.idporten.minidplus.exception.minid.MinIDSystemException;
import no.idporten.minidplus.logging.audit.AuditID;
import no.idporten.minidplus.logging.event.EventService;
import no.idporten.minidplus.util.FeatureSwitches;
import no.minid.exception.MinidUserInvalidException;
import no.minid.exception.MinidUserNotFoundException;
import no.minid.service.MinIDService;
import org.springframework.stereotype.Service;

import java.time.Instant;

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

    private final String minidplusSourcePrefix = "minid-on-the-fly";

    public boolean authenticateUser(String sid, String pid, String password, ServiceProvider sp, LevelOfAssurance levelOfAssurance) throws MinidUserNotFoundException, MinIDIncorrectCredentialException, MinIDSystemException, MinIDInvalidAcrLevelException, MinidUserInvalidException {
        LevelOfAssurance assignedLevelOfAssurance = LevelOfAssurance.LEVEL4; //default
        MinidUser identity = findUserFromPid(pid);

        if (identity == null) {
            warn("User not found.");
            throw new MinidUserNotFoundException("User not found");
        }

        assignedLevelOfAssurance = getLevelOfAssurance(identity.getSource(), levelOfAssurance);
        if (identity.isOneTimeCodeLocked()) {
            warn("One time code is locked for user");
            return false;
        }

        if (!minIDService.validateUserPassword(identity.getPersonNumber(), password)) {
            warn("Password invalid for user");
            throw new MinIDIncorrectCredentialException(IDPortenExceptionID.IDENTITY_PASSWORD_INCORRECT, "Password validation failed");
        }
        minidPlusCache.putSSN(sid, identity.getPersonNumber().getSsn());
        minidPlusCache.putAuthorization(sid, new Authorization(pid, assignedLevelOfAssurance, Instant.now().toEpochMilli()));
        otcPasswordService.sendSMSOtp(sid, sp, identity);
        return true;
    }

    public boolean verifyUserByEmail(String sid) throws MinidUserNotFoundException, MinidUserInvalidException {

        String pid = minidPlusCache.getSSN(sid);
        MinidUser identity = findUserFromPid(pid);

        if (identity.getEmail() == null) {
            warn("Email not found not found for user");
            throw new MinidUserInvalidException("Email not found not found for user");
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

    protected LevelOfAssurance getLevelOfAssurance(String source, LevelOfAssurance requested) throws MinIDInvalidAcrLevelException, MinIDSystemException {
        if (source.startsWith(minidplusSourcePrefix)) {
            return LevelOfAssurance.LEVEL4;
        } else if (requested.equals(LevelOfAssurance.LEVEL4)) {
            throw new MinIDInvalidAcrLevelException("Only minid-on-the-fly-passport users can log in with level 4");
        } else if (LevelOfAssurance.LEVEL3.equals(requested)) {
            return LevelOfAssurance.LEVEL3;
        } else {
            throw new MinIDSystemException(IDPortenExceptionID.IDENTITY_INVALID_SECURITY_LEVEL, "Invalid security level");
        }
    }

    public boolean authenticatePid(String sid, String pid, ServiceProvider sp) throws MinidUserNotFoundException, MinidUserInvalidException {
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
