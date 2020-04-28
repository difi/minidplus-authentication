package no.idporten.minidplus.web;

import no.idporten.domain.sp.ServiceProvider;
import no.idporten.minidplus.domain.MinidPlusSessionAttributes;
import no.idporten.minidplus.service.AuthenticationService;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class MinIdPlusPasswordControllerTest {

    private final ServiceProvider sp = new ServiceProvider("idporten");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    AuthenticationService authenticationService;

    @MockBean
    MinidPlusCache minidPlusCache;

    @Test
    public void test_initiate_change_password() throws Exception {

        when(authenticationService.authenticateUser(anyString(), anyString(), anyString(), eq(sp))).thenReturn(true);
        MvcResult mvcResult = mockMvc.perform(get("/password")
                .param(MinidPlusSessionAttributes.HTTP_SESSION_LOCALE, "en_gb")
        )//.andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("minidplus_password_personid"))
                .andExpect(content().string(containsString("")))
                .andExpect(model().attributeExists("personIdInput"))
                .andExpect(request().sessionAttribute("session.state", is(1)))
                .andExpect(request().sessionAttribute("sid", is(notNullValue())))
                .andReturn();
        assertEquals("en_gb", mvcResult.getResponse().getLocale().toString());
    }

    @Test
    public void test_post_personid_successful() throws Exception {
        String pid = "55555555555";
        String code = "abc123-bcdg-234325235-2436dfh-gsfh34w";
        when(minidPlusCache.getSSN(code)).thenReturn(pid);
        when(authenticationService.authenticatePid(eq(pid), eq(code), eq(sp))).thenReturn(true);
        MvcResult mvcResult = mockMvc.perform(post("/authorize")
                .sessionAttr(MinidPlusSessionAttributes.HTTP_SESSION_SID, code)
                .sessionAttr(MinidPlusSessionAttributes.HTTP_SESSION_STATE, 2)
                .param("otpCode", code)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        )//.andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("minidplus_enter_otp")) //todo fiks etter integrasjon med idporten
                .andReturn();
    }


}