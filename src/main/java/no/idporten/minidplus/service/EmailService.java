package no.idporten.minidplus.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    //todo implement http://jira.difi.local/browse/PBLEID-20478
    @Value("${minid-plus.mail.allowed-filter}")
    private String allowedFilter;

    private final JavaMailSender javaMailSender;

    private final MessageSource messageSource;

    void sendOtc(String to, String otc, LocalDateTime expire) {
        if (!inWhitelist(to)) {
            log.warn("Email ignored since the domain of " + to + " is not in whitelist");
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Sending otp to email " + to);
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(messageSource.getMessage("no.idporten.forgottenpassword.email.subject", null, Locale.getDefault()));
        message.setText(messageSource.getMessage("no.idporten.forgottenpassword.email.message", new String[]{otc, DateTimeFormatter.ofPattern("HH:mm").format(expire)}, Locale.getDefault()));

        javaMailSender.send(message);
    }

    private boolean inWhitelist(String to) {
        return to.matches(allowedFilter);
    }
}