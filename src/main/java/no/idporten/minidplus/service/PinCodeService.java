package no.idporten.minidplus.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.idporten.domain.user.MinidUser;
import no.idporten.validation.util.RandomUtil;
import no.idporten.validation.util.RandomUtilInterface;
import no.minid.exception.MinidUserNotFoundException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class PinCodeService {

    private final MinidPlusCache minidPlusCache;

    private final MinidIdentityService minidIdentityService;

    public int getRandomCode(MinidUser identity){
        RandomUtil randomUtil = new RandomUtil();
        return randomUtil.generateNextPin(identity.getNumPinCodes());
    }

    public boolean checkPinCode(String sid, String inputPinCode, int pinCodeNumber) {
        MinidUser identity = new MinidUser();
        String pid = minidPlusCache.getSSN(sid);
        if (pid != null) {
            try {
                identity = minidIdentityService.findUserFromPid(pid);
            } catch (MinidUserNotFoundException e) {

            }
        }
        if (identity.getPersonNumber() != null && identity.isPinCodesLocked()) {
            // User has canceled pin code. Send to authentication error with a message
            return false;
        }
        if (identity.getPincodes().isEqual(inputPinCode, pinCodeNumber)) {
            return true;
        }
        return false;
    }
}
