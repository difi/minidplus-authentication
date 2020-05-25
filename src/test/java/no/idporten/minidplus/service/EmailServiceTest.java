package no.idporten.minidplus.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringRunner;

import static java.time.LocalDateTime.now;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest
@RunWith(SpringRunner.class)
public class EmailServiceTest {

    @Autowired
    EmailService emailService;

    @MockBean
    JavaMailSender javaMailSender;

    @Test
    public void test_send_email() {
        emailService.sendOtc("kons-ahj@digdir.no", "abc12", now().plusSeconds(300));
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    public void test_send_email_not_in_whitelist() {
        emailService.sendOtc("kons-ahj@diggdir.no", "abc12", now().plusSeconds(300));
        verifyNoInteractions(javaMailSender);
    }
}
