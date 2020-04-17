package no.idporten.minidplus.web;

import no.idporten.minidplus.service.MinidPlusCache;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MinidPlusTokenController.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc(addFilters = false)
public class MinIdPlusTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    MinidPlusCache minidPlusCache;

    @Test
    public void test_token_generated_with_valid_code() throws Exception {
        String code = "abc123-bcdg-234325235-2436dfh-gsfh34w";
        when(minidPlusCache.getSSN(code)).thenReturn("55555555555");

        MvcResult mvcResult = this.mockMvc.perform(post("/token")
                .param("grant_type", "authorization_code")
                .param("code",code)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ssn").value("55555555555"))
                .andExpect(jsonPath("$.expires_in").value(600))
                .andReturn();
        //assertNull(minidPlusCache.getSSN(sid));
        //todo   verify(auditService).auditTokenResponse(any(), anyString(), any());
        //todo eventlogg?
    }

    @Test
    public void test_auth_code_not_found() throws Exception {
        String sid = "fc897796-58da-4f68-91fb-f62b972fe323";
        mockMvc.perform(post("/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("code", sid))
                .andExpect(status().isNotFound());
       //assertNull(minidPlusCache.getSSN(sid));
        //todo verify no interaction audit
    }

}