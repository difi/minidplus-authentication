package no.idporten.minidplus.web;

import no.idporten.minidplus.util.MinidPlusButtonType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class CustomErrorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void test_retry_returns_to_login_page() throws Exception {

        mockMvc.perform(post("/retry")
                .param(MinidPlusButtonType.CANCEL.id(), "")
                .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(view().name("minidplus_enter_credentials"))
                .andExpect(model().attributeExists("userCredentials"));

    }

}