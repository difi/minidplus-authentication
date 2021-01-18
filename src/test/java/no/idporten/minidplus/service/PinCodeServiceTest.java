package no.idporten.minidplus.service;

import no.idporten.domain.user.MinidUser;
import no.idporten.domain.user.MobilePhoneNumber;
import no.idporten.domain.user.PersonNumber;
import no.idporten.domain.user.Pincodes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(SpringRunner.class)
public class PinCodeServiceTest {

    private final String pid = "23079421189";

    @MockBean
    private MinidPlusCache minidPlusCache;

    @MockBean
    private MinidIdentityService minidIdentityService;

    @Autowired
    private PinCodeService pinCodeService;

    @Test
    public void checkCodeTest()  throws Exception {
        String code = "abc123-bcdg-234325235-2436dfh-gsfh34w";
        MinidUser identity = new MinidUser();
        identity.setPhoneNumber(new MobilePhoneNumber("12345678"));
        identity.setPrefersOtc(true);
        identity.setPersonNumber(new PersonNumber(pid));
        Pincodes pincodes = new Pincodes(identity.getPersonNumber(), Date.from(Instant.now()), 1);
        pincodes.addPincode("12343", 1);
        identity.setPincodes(pincodes);
        identity.setState(MinidUser.State.NORMAL);
        when(minidPlusCache.getSSN(code)).thenReturn(pid);
        when(minidIdentityService.findUserFromPid(pid)).thenReturn(identity);
        assertTrue(pinCodeService.checkPinCode(code, "12343", 1));
    }
}
