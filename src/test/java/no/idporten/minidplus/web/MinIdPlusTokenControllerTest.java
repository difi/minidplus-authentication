package no.idporten.minidplus.web;

import no.idporten.log.audit.AuditLogger;
import no.idporten.minidplus.domain.Authorization;
import no.idporten.minidplus.domain.LevelOfAssurance;
import no.idporten.minidplus.logging.audit.AuditID;
import no.idporten.minidplus.service.MinidPlusCache;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc(addFilters = false)
public class MinIdPlusTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    MinidPlusCache minidPlusCache;

    @MockBean
    AuditLogger auditLogger;

    @Test
    public void test_token_generated_with_valid_code() throws Exception {
        String code = "abc123-bcdg-234325235-2436dfh-gsfh34w";
        Authorization auth = new Authorization("55555555555", LevelOfAssurance.LEVEL4, 1000);
        when(minidPlusCache.getAuthorization(code)).thenReturn(auth);
        MvcResult mvcResult = mockMvc.perform(post("/token")
                .param("grant_type", "authorization_code")
                .param("code", code)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        )//.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ssn").value(auth.getSsn()))
                .andExpect(jsonPath("$.acr_level").value("Level4"))
                .andExpect(jsonPath("$.expires_in").value(600))
                .andReturn();
        verify(minidPlusCache).removeSession(code);
        verify(auditLogger).log(
                eq(AuditID.TOKEN_CREATED.auditId()),
                isNull(),
                any());
    }

    @Test
    public void test_auth_code_not_found() throws Exception {
        String code = "fc897796-58da-4f68-91fb-f62b972fe323";
        mockMvc.perform(post("/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("code", code))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(auditLogger);
    }

}