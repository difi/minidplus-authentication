package no.idporten.minidplus.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.idporten.minidplus.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TmpEmailTesterDeleteMeLater {

    private final EmailService emailService;

    @GetMapping(value = "/sendemail")
    public ResponseEntity test_send_email_default() {
        StringBuilder errors = new StringBuilder();
        try {
            emailService.sendSimpleMessage("hjemas@gmail.com", "success!!!", "juhu");
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            log.warn("Failed to send to gmail. Trying digdir", e);
            errors.append("to gmail: " + e.getMessage() + "\n");
            try {
                emailService.sendSimpleMessage("kons-ahj@digdir.no", "fail", "Huff da: " + e.getMessage());
            } catch (Exception ex) {
                log.error("wtf", ex);
                errors.append("to digdir: " + ex.getMessage() + "\n");
            }

        }
        return ResponseEntity.badRequest().body(errors.toString());
    }

}
