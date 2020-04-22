package no.idporten.minidplus.web;

import no.idporten.domain.auth.AuthType;
import no.idporten.domain.sp.ServiceProvider;
import no.idporten.minidplus.domain.AuthorizationRequest;
import no.idporten.minidplus.domain.MinidPlusSessionAttributes;
import no.idporten.minidplus.service.AuthenticationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MinidPlusAuthorizeController.class)
@RunWith(SpringRunner.class)
public class MinIdPlusAuthorizeControllerTest {

    private final ServiceProvider sp = new ServiceProvider("idporten");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    AuthenticationService authenticationService;

    @Test
    public void test_authorization_session_parameters_set() throws Exception {

        when(authenticationService.authenticateUser(anyString(), anyString(), anyString(), eq(sp))).thenReturn(true);
        AuthorizationRequest ar = getAuthorizationRequest();
        MvcResult mvcResult = mockMvc.perform(get("/authorize")
                .param(MinidPlusSessionAttributes.HTTP_SESSION_REDIRECT_URL, ar.getRedirectUrl())
                .param(MinidPlusSessionAttributes.HTTP_SESSION_FORCE_AUTH, ar.getForceAuth().toString())
                .param(MinidPlusSessionAttributes.HTTP_SESSION_GOTO, ar.getGotoParam())
                .param(MinidPlusSessionAttributes.HTTP_SESSION_LOCALE, ar.getLocale())
                .param(MinidPlusSessionAttributes.HTTP_SESSION_GX_CHARSET, ar.getGx_charset())
                .param(MinidPlusSessionAttributes.HTTP_SESSION_SERVICE, ar.getService())
                .param(MinidPlusSessionAttributes.HTTP_SESSION_START_SERVICE, ar.getStartService())
        )//.andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("minidplus_enter_credentials"))
                .andExpect(content().string(containsString("")))
                .andExpect(model().attributeExists("authorizationRequest"))
                .andExpect(request().sessionAttribute("session.authenticationType", equalTo(AuthType.MINID_PLUS)))
                .andExpect(model().attributeExists("userCredentials"))
                .andExpect(request().sessionAttribute("session.state", is(1)))
                .andExpect(request().sessionAttribute("sid", is(notNullValue())))
                .andExpect(model().attribute("authorizationRequest", equalTo(ar)))
                .andReturn();
        assertEquals("nb_no", mvcResult.getResponse().getLocale().toString());
    }

    private AuthorizationRequest getAuthorizationRequest() {
        AuthorizationRequest ar = new AuthorizationRequest();
        ar.setRedirectUrl("http://localhost");
        ar.setLocale("nb_no");
        ar.setForceAuth(true);
        ar.setGotoParam("hello");
        ar.setGx_charset("dunno");
        ar.setService("myService");
        ar.setStartService("startMeUp");
        return ar;
    }
}