package no.idporten.minidplus.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationClient notificationClient;

    private final MessageSource messageSource;

    public void sendForgottenPasswordEmail(String email, String otc, LocalDateTime expire) {
        if (log.isDebugEnabled()) {
            log.debug("Sending otp to email " + email);
        }
        notificationClient.sendEmail(email,
                messageSource.getMessage("no.idporten.forgottenpassword.email.subject", null, Locale.getDefault()),
                messageSource.getMessage("no.idporten.forgottenpassword.email.message", new String[]{otc, DateTimeFormatter.ofPattern("HH:mm").format(expire)}, Locale.getDefault()));
    }

}
