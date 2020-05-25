package no.idporten.minidplus.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringRunner;

import static java.time.LocalDateTime.now;
import static junit.framework.TestCase.fail;

@SpringBootTest
@RunWith(SpringRunner.class)
public class EmailServiceTest {

    @Autowired
    EmailService emailService;

    @MockBean
    JavaMailSender javaMailSender;

    @Test
    public void test_send_email(){
        try {
            emailService.sendOtc("kons-ahj@digdir", "abc12", now().plusSeconds(300));
        } catch (Exception e) {
            fail("Should not trow exception");
        }
    }
}
