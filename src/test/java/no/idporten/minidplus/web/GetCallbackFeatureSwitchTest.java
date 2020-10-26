package no.idporten.minidplus.web;

import no.idporten.domain.sp.ServiceProvider;
import no.idporten.minidplus.domain.AuthorizationRequest;
import no.idporten.minidplus.domain.LevelOfAssurance;
import no.idporten.minidplus.service.AuthenticationService;
import no.idporten.minidplus.service.MinidPlusCache;
import no.idporten.minidplus.service.ServiceproviderService;
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

import static no.idporten.minidplus.domain.MinidPlusSessionAttributes.*;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest(properties = {"minid-plus.callback-method-post:false"})
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class GetCallbackFeatureSwitchTest {

    protected final ServiceProvider sp = new ServiceProvider("idporten");
    private final String pid = "23079421189";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    AuthenticationService authenticationService;

    @MockBean
    MinidPlusCache minidPlusCache;

    @MockBean
    ServiceproviderService serviceproviderService;


    @Test
    public void test_redirects_without_post() throws Exception {
        String code = "abc123-bcdg-234325235-2436dfh-gsfh34w";
        String otp = "abc12";
        when(minidPlusCache.getSSN(code)).thenReturn("55555555555");
        when(authenticationService.authenticateOtpStep(eq(code), eq(otp), eq(sp.getEntityId()))).thenReturn(true);
        MvcResult mvcResult = mockMvc.perform(post("/authorize")
                .sessionAttr(HTTP_SESSION_SID, code)
                .sessionAttr(HTTP_SESSION_STATE, MinIdPlusAuthorizeController.STATE_LOGIN_VERIFICATION_CODE)
                .sessionAttr(AUTHORIZATION_REQUEST, getAuthorizationRequest())
                .sessionAttr(SERVICEPROVIDER, sp)
                .param("otpCode", otp)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .with(csrf())
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(startsWith("redirect:http://localhost")))
                .andExpect(view().name(containsString("code=" + code)))
                .andExpect(view().name(containsString("state=" + 123)))
                .andReturn();
        mvcResult.getModelAndView();
    }

    private AuthorizationRequest getAuthorizationRequest() {
        AuthorizationRequest ar = new AuthorizationRequest();
        ar.setRedirectUri("http://localhost");
        ar.setLocale("nb");
        ar.setState("123");
        ar.setGotoParam("http://localhost");
        ar.setSpEntityId("NAV");
        ar.setResponseType("authorization_code");
        ar.setAcrValues(LevelOfAssurance.LEVEL4);
        return ar;
    }
}