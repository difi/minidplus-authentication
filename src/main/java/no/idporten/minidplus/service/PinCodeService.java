package no.idporten.minidplus.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.resilience.CorrelationId;
import no.idporten.domain.user.MinidUser;
import no.idporten.minidplus.exception.IDPortenExceptionID;
import no.idporten.minidplus.exception.minid.MinIDQuarantinedUserException;
import no.idporten.validation.util.RandomUtil;
import no.minid.exception.MinidUserNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class PinCodeService {

    private final MinidPlusCache minidPlusCache;

    private final MinidIdentityService minidIdentityService;

    @Value("${minid-plus.quarantine-counter-max-number}")
    private int maxNumberOfQuarantineCounters;

    public int getRandomCode(MinidUser identity) throws MinIDQuarantinedUserException {
        validateUserState(identity);
        RandomUtil randomUtil = new RandomUtil();
        return randomUtil.generateNextPin(identity.getNumPinCodes());
    }

    public boolean checkPinCode(String sid, String inputPinCode, int pinCodeNumber) throws MinIDQuarantinedUserException {
        MinidUser identity = new MinidUser();
        String pid = minidPlusCache.getSSN(sid);
        if (pid != null) {
            try {
                identity = minidIdentityService.findUserFromPid(pid);
            } catch (MinidUserNotFoundException e) {

            }
        }
        validateUserState(identity);
        if (identity.getPersonNumber() != null && identity.isPinCodesLocked()) {
            // User has canceled pin code. Send to authentication error with a message
            return false;
        }
        if (identity.getPincodes().isEqual(inputPinCode, pinCodeNumber)) {
            return true;
        }
        return false;
    }

    public void validateUserState(MinidUser user) throws MinIDQuarantinedUserException {
        if (user.getQuarantineCounter() == null) {
            user.setQuarantineCounter(0);
        }
        if (user.getQuarantineCounter() >= maxNumberOfQuarantineCounters) {
            if (user.getQuarantineExpiryDate() != null) {
                if (user.getQuarantineExpiryDate().before(Date.from(Clock.systemUTC().instant().minusSeconds(3600)))) {
                    warn("User has been in quarantine for more than one hour.");
                    throw new MinIDQuarantinedUserException(IDPortenExceptionID.IDENTITY_QUARANTINED, "User has been in quarantine for more than one hour.");
                }
            }
            warn("Pincode locked");
            throw new MinIDQuarantinedUserException(IDPortenExceptionID.IDENTITY_QUARANTINED, "pin code is locked");
        }
        if (user.getState().equals(MinidUser.State.CLOSED)) {
            warn("User has state CLOSED.");
            throw new MinIDQuarantinedUserException(IDPortenExceptionID.IDENTITY_CLOSED, "User is closed");
        }

    }

    private void warn(String message) {
        log.warn(CorrelationId.get() +  " " + message);
    }

}
