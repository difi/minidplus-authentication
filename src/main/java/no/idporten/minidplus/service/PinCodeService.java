package no.idporten.minidplus.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.idporten.domain.user.MinidUser;
import no.idporten.validation.util.RandomUtilInterface;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PinCodeService {

    private transient RandomUtilInterface randomUtil;

    public int getRandomCode(MinidUser identity){
        return randomUtil.generateNextPin(identity.getNumPinCodes());
    }

    public boolean checkPinCode(String sid, String inputPinCode, int pinCodeNumber) {
        return true;
    }
}
