package no.idporten.minidplus.web;

import lombok.RequiredArgsConstructor;
import no.idporten.minidplus.service.EmailService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TmpEmailTesterDeleteMeLater {

    private final EmailService emailService;

    @PutMapping(value = "/sendemail")
    public void test_send_email_default() {
        try {
            emailService.sendSimpleMessage("hjemas@gmail.com", "success!!!", "juhu");
        } catch (Exception e) {
            emailService.sendSimpleMessage("kons-ahj@digdir.no", "fail", "Huff da: " + e.getMessage());
        }
    }

}
