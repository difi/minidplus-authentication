package no.idporten.minidplus.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class EmailServiceTest {

    @Autowired
    EmailService emailService;

    @Test
    public void test_send_email(){
        emailService.sendSimpleMessage("kons-ahj@digdir", "hei", "juhu");
    }
}
