package no.idporten.minidplus.web;

import no.idporten.domain.auth.AuthType;
import no.idporten.domain.sp.ServiceProvider;
import no.idporten.minidplus.domain.AuthorizationRequest;
import no.idporten.minidplus.domain.LevelOfAssurance;
import no.idporten.minidplus.exception.IDPortenExceptionID;
import no.idporten.minidplus.exception.minid.MinIDIncorrectCredentialException;
import no.idporten.minidplus.exception.minid.MinIDInvalidAcrLevelException;
import no.idporten.minidplus.service.AuthenticationService;
import no.idporten.minidplus.service.MinidPlusCache;
import no.idporten.minidplus.service.ServiceproviderService;
import no.idporten.minidplus.util.MinIdPlusButtonType;
import no.idporten.minidplus.util.MinIdState;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static no.idporten.minidplus.domain.MinidPlusSessionAttributes.*;
import static no.idporten.minidplus.util.MinIdPlusViews.*;
import static no.idporten.minidplus.web.MinIdPlusAuthorizeController.VIEW_LOGIN_ENTER_OTP;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"minid-plus.serverid=testserver"})
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class MinIdPlusAuthorizeControllerTest {

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
    public void test_authorization_session_parameters_set() throws Exception {
        ServiceProvider serviceProvider = new ServiceProvider("NAV");
        when(authenticationService.authenticateUser(anyString(), anyString(), anyString(), eq(serviceProvider), any(LevelOfAssurance.class))).thenReturn(true);
        when(serviceproviderService.getServiceProvider(eq("NAV"), eq("localhost"))).thenReturn(serviceProvider);
        AuthorizationRequest ar = getAuthorizationRequest();
        MvcResult mvcResult = mockMvc.perform(get("/authorize")
                .header(HttpHeaders.HOST, "localhost")
                .param(HTTP_SESSION_CLIENT_ID, ar.getSpEntityId())
                .param(HTTP_SESSION_REDIRECT_URI, ar.getRedirectUri())
                .param(HTTP_SESSION_RESPONSE_TYPE, ar.getResponseType())
                .param(HTTP_SESSION_GOTO, ar.getGotoParam())
                .param(HTTP_SESSION_LOCALE, ar.getLocale())
                .param(HTTP_SESSION_ACR_VALUES, ar.getAcrValues().getExternalName())
                .param(HTTP_SESSION_CLIENT_STATE, ar.getState())
        )
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_START_LOGIN))
                .andExpect(content().string(containsString("")))
                .andExpect(model().attributeExists("authorizationRequest"))
                .andExpect(request().sessionAttribute("session.authenticationType", equalTo(AuthType.MINID_PLUS)))
                .andExpect(model().attributeExists("userCredentials"))
                .andExpect(request().sessionAttribute("session.state", is(MinIdState.STATE_START_LOGIN)))
                .andExpect(request().sessionAttribute("sid", is(notNullValue())))
                .andExpect(model().attribute("authorizationRequest", equalTo(ar)))
                .andExpect(request().sessionAttribute("serviceprovider", equalTo(serviceProvider)))
                .andReturn();
        assertEquals("nb", mvcResult.getResponse().getLocale().toString());
    }

    @Test
    public void test_unsupported_locale_defaults_to_en() throws Exception {
        ServiceProvider serviceProvider = new ServiceProvider("NAV");
        when(authenticationService.authenticateUser(anyString(), anyString(), anyString(), eq(serviceProvider), any(LevelOfAssurance.class))).thenReturn(true);
        AuthorizationRequest ar = getAuthorizationRequest();
        MvcResult mvcResult = mockMvc.perform(get("/authorize")
                .param(HTTP_SESSION_CLIENT_ID, ar.getSpEntityId())
                .param(HTTP_SESSION_REDIRECT_URI, ar.getRedirectUri())
                .param(HTTP_SESSION_RESPONSE_TYPE, ar.getResponseType())
                .param(HTTP_SESSION_GOTO, ar.getGotoParam())
                .param(HTTP_SESSION_LOCALE, "pf")
                .param(HTTP_SESSION_ACR_VALUES, ar.getAcrValues().getExternalName())
                .param(HTTP_SESSION_CLIENT_STATE, ar.getState())
        )
                .andExpect(status().isOk())
                .andReturn();
        assertEquals("en", mvcResult.getResponse().getLocale().toString());
    }

    @Test
    public void test_user_cancels_credentials_returns_error_user_aborted() throws Exception {

        mockMvc.perform(post("/authorize")
                .param("personalIdNumber", "")
                .param("password", "")
                .param(MinIdPlusButtonType.CANCEL.id(), "")
                .sessionAttr(HTTP_SESSION_STATE, 1)
                .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_REDIRECT_TO_IDPORTEN));

    }

    @Test
    public void test_post_credentials_successful() throws Exception {
        String code = "abc123-bcdg-234325235-2436dfh-gsfh34w";
        when(minidPlusCache.getSSN(code)).thenReturn(pid);
        when(authenticationService.authenticateUser(eq(code), eq(pid), eq("abcabcabc3"), eq(sp), any(LevelOfAssurance.class))).thenReturn(true);
        MvcResult mvcResult = mockMvc.perform(post("/authorize")
                .sessionAttr(HTTP_SESSION_SID, code)
                .sessionAttr(HTTP_SESSION_STATE, MinIdState.STATE_START_LOGIN)
                .sessionAttr(AUTHORIZATION_REQUEST, getAuthorizationRequest())
                .param("personalIdNumber", pid)
                .param("password", "abcabcabc3")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_LOGIN_ENTER_OTP))
                .andReturn();
    }

    @Test
    public void test_post_pid_wrong_format() throws Exception {
        String code = "abc123-bcdg-234325235-2436dfh-gsfh34w";
        MvcResult mvcResult = mockMvc.perform(post("/authorize")
                .sessionAttr(HTTP_SESSION_SID, code)
                .sessionAttr(HTTP_SESSION_STATE, MinIdState.STATE_START_LOGIN)
                .sessionAttr(AUTHORIZATION_REQUEST, getAuthorizationRequest())
                .param("personalIdNumber", "123")
                .param("password", "abc")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_START_LOGIN))
                .andExpect(model().hasErrors())
                .andReturn();
    }

    @Test
    public void test_post_pwd_wrong() throws Exception {
        String code = "abc123-bcdg-234325235-2436dfh-gsfh34w";
        when(authenticationService.authenticateUser(eq(code), eq(pid), eq("helloWorld"), eq(sp), any(LevelOfAssurance.class))).thenThrow(new MinIDIncorrectCredentialException(IDPortenExceptionID.IDENTITY_PASSWORD_INCORRECT, "Password validation failed"));
        MvcResult mvcResult = mockMvc.perform(post("/authorize")
                .sessionAttr(HTTP_SESSION_SID, code)
                .sessionAttr(HTTP_SESSION_STATE, MinIdState.STATE_START_LOGIN)
                .sessionAttr(AUTHORIZATION_REQUEST, getAuthorizationRequest())
                .sessionAttr(SERVICEPROVIDER, sp)
                .param("personalIdNumber", pid)
                .param("password", "helloWorld")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_START_LOGIN))
                .andExpect(model().hasErrors())
                .andReturn();
    }

    @Test
    public void test_wrong_acr_level() throws Exception {
        String code = "abc123-bcdg-234325235-2436dfh-gsfh34w";
        when(authenticationService.authenticateUser(eq(code), eq(pid), eq("abcabcabc3"), eq(sp), any(LevelOfAssurance.class))).thenThrow(new MinIDInvalidAcrLevelException("Dude...wrong level :-/"));
        MvcResult mvcResult = mockMvc.perform(post("/authorize")
                .sessionAttr(HTTP_SESSION_SID, code)
                .sessionAttr(HTTP_SESSION_STATE, MinIdState.STATE_START_LOGIN)
                .sessionAttr(AUTHORIZATION_REQUEST, getAuthorizationRequest())
                .sessionAttr(SERVICEPROVIDER, sp)
                .param("personalIdNumber", pid)
                .param("password", "abcabcabc3")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_ERROR_ACR))
                .andReturn();
    }

    @Test
    public void test_post_otp_successful() throws Exception {
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
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_REDIRECT_TO_IDPORTEN))
                .andExpect(model().attribute(MODEL_REDIRECT_URL, containsString("code=" + code)))
                .andExpect(model().attribute(MODEL_REDIRECT_URL, containsString("serverid=testserver")))
                .andReturn();
    }

    @Test
    public void test_post_otp_unsuccessful() throws Exception {
        String code = "abc123-bcdg-234325235-2436dfh-gsfh34w";
        when(minidPlusCache.getSSN(code)).thenReturn("55555555555");
        when(authenticationService.authenticateOtpStep(eq(code), eq(code), anyString())).thenReturn(false);
        MvcResult mvcResult = mockMvc.perform(post("/authorize")
                .sessionAttr(HTTP_SESSION_SID, code)
                .sessionAttr(HTTP_SESSION_STATE, MinIdPlusAuthorizeController.STATE_LOGIN_VERIFICATION_CODE)
                .param("otpCode", code)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .with(csrf())
        )//.andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_LOGIN_ENTER_OTP))
                .andExpect(model().hasErrors())
                .andReturn();
    }

    public void test_user_cancels_otp_returns_error_user_aborted() throws Exception {

        mockMvc.perform(post("/authorize")
                .param("otpCode", "")
                .param(MinIdPlusButtonType.CANCEL.id(), "")
                .sessionAttr(HTTP_SESSION_STATE, MinIdPlusAuthorizeController.STATE_LOGIN_VERIFICATION_CODE)
        )
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_REDIRECT_TO_IDPORTEN));
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