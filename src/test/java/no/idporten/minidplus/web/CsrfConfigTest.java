package no.idporten.minidplus.web;

import no.idporten.domain.sp.ServiceProvider;
import no.idporten.minidplus.MinidplusApplication;
import no.idporten.minidplus.config.SecurityConfiguration;
import no.idporten.minidplus.domain.Authorization;
import no.idporten.minidplus.domain.AuthorizationRequest;
import no.idporten.minidplus.domain.LevelOfAssurance;
import no.idporten.minidplus.service.AuthenticationService;
import no.idporten.minidplus.service.MinidPlusCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;

import static no.idporten.minidplus.domain.MinidPlusSessionAttributes.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Selected tests. Should work for all with form:form
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {MinidplusApplication.class, SecurityConfiguration.class})
public class CsrfConfigTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private Filter springSecurityFilterChain;

    protected MockMvc mvc;

    @Before
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    protected final ServiceProvider sp = new ServiceProvider("idporten");

    @MockBean
    AuthenticationService authenticationService;

    @MockBean
    MinidPlusCache minidPlusCache;

    @Test
    public void test_post_otp_forbidden_sans_csrf() throws Exception {
        String code = "abc123-bcdg-234325235-2436dfh-gsfh34w";
        when(minidPlusCache.getSSN(code)).thenReturn("55555555555");
        when(authenticationService.authenticateOtpStep(eq(code), eq(code))).thenReturn(true);
        MvcResult mvcResult = mvc.perform(post("/authorize")
                .sessionAttr(HTTP_SESSION_SID, code)
                .sessionAttr(HTTP_SESSION_STATE, 2)
                .sessionAttr(AUTHORIZATION_REQUEST, getAuthorizationRequest())
                .param("otpCode", code)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        )
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    public void test_post_otp_successful() throws Exception {
        String code = "abc123-bcdg-234325235-2436dfh-gsfh34w";
        when(minidPlusCache.getSSN(code)).thenReturn("55555555555");
        when(authenticationService.authenticateOtpStep(eq(code), eq(code))).thenReturn(true);
        MvcResult mvcResult = mvc.perform(post("/authorize")
                .sessionAttr(HTTP_SESSION_SID, code)
                .sessionAttr(HTTP_SESSION_STATE, 2)
                .sessionAttr(AUTHORIZATION_REQUEST, getAuthorizationRequest())
                .param("otpCode", code)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(view().name("redirect_to_idporten"))
                .andReturn();
    }

    @Test
    public void test_token_generated_with_valid_code() throws Exception {
        String code = "abc123-bcdg-234325235-2436dfh-gsfh34w";
        Authorization auth = new Authorization("55555555555", LevelOfAssurance.LEVEL4, 1000);
        when(minidPlusCache.getAuthorization(code)).thenReturn(auth);
        MvcResult mvcResult = mvc.perform(post("/token")
                .param("grant_type", "authorization_code")
                .param("code", code)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .with(user("user")))
                .andExpect(status().isOk())
                .andReturn();
    }

    private AuthorizationRequest getAuthorizationRequest() {
        AuthorizationRequest ar = new AuthorizationRequest();
        ar.setRedirectUri("http://localhost");
        ar.setLocale("nb_no");
        ar.setState("123");
        ar.setGotoParam("http://localhost");
        ar.setSpEntityId("NAV");
        ar.setResponseType("authorization_code");
        ar.setAcrValues(LevelOfAssurance.LEVEL4);
        return ar;
    }
}
