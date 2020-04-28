package no.idporten.minidplus.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class NotificationService {
    private NotificationClient notificationClient;
    private MessageSource messageSource;

    @Autowired
    public NotificationService(NotificationClient notificationClient, MessageSource messageSource) {
        this.notificationClient = notificationClient;
        this.messageSource = messageSource;
    }

    //TODO: Må legge på eingangskode eller kva enn som blir brukt for å gjennopprette passord.
    public void sendForgottenPasswordEmail(String email) {
        notificationClient.sendEmail(email,
                messageSource.getMessage("no.idporten.forgottenpassword.email.subject", null, Locale.getDefault()),
                messageSource.getMessage("no.idporten.forgottenpassword.email.message", null, Locale.getDefault()));
    }

}
