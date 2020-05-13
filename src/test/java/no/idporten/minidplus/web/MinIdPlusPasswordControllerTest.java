package no.idporten.minidplus.web;

import no.idporten.domain.sp.ServiceProvider;
import no.idporten.minidplus.domain.MinidPlusSessionAttributes;
import no.idporten.minidplus.service.AuthenticationService;
import no.idporten.minidplus.service.MinidPlusCache;
import no.idporten.minidplus.service.OTCPasswordService;
import no.idporten.ui.impl.MinidPlusButtonType;
import no.minid.exception.MinidUserNotFoundException;
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

import static no.idporten.minidplus.domain.MinidPlusSessionAttributes.HTTP_SESSION_STATE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class MinIdPlusPasswordControllerTest {

    private final ServiceProvider sp = new ServiceProvider("idporten");
    private final String pid = "23079421189";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    AuthenticationService authenticationService;

    @MockBean
    OTCPasswordService otcPasswordService;

    @MockBean
    MinidPlusCache minidPlusCache;

    @Test
    public void test_initiate_change_password() throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/password")
                .param(MinidPlusSessionAttributes.HTTP_SESSION_LOCALE, "en"))
                .andExpect(status().isOk())
                .andExpect(view().name("minidplus_password_personid"))
                .andExpect(content().string(containsString("")))
                .andExpect(model().attributeExists("personIdInput"))
                .andExpect(request().sessionAttribute("session.state", is(MinidPlusPasswordController.STATE_PERSONID)))
                .andExpect(request().sessionAttribute("sid", is(notNullValue())))
                .andReturn();
        assertEquals("en", mvcResult.getResponse().getLocale().toString());
    }

    @Test
    public void test_post_personid_successful() throws Exception {
        String code = "abc123-bcdg-234325235-2436dfh-gsfh34w";
        when(minidPlusCache.getSSN(code)).thenReturn(pid);
        when(authenticationService.authenticatePid(eq(pid), eq(code), eq(sp))).thenReturn(true);
        MvcResult mvcResult = mockMvc.perform(post("/password")
                .sessionAttr(MinidPlusSessionAttributes.HTTP_SESSION_SID, code)
                .sessionAttr(MinidPlusSessionAttributes.HTTP_SESSION_STATE, MinidPlusPasswordController.STATE_PERSONID)
                .param("personalIdNumber", pid)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("minidplus_password_otp_sms"))
                .andReturn();
    }

    @Test
    public void test_post_otp_sms_successful() throws Exception {
        String code = "abc123-bcdg-234325235-2436dfh-gsfh34w";
        String otp = "ab123";
        when(minidPlusCache.getSSN(code)).thenReturn("55555555555");
        when(otcPasswordService.checkOTCCode(eq(code), eq(otp))).thenReturn(true);
        MvcResult mvcResult = mockMvc.perform(post("/password?otpType=sms")
                .sessionAttr(MinidPlusSessionAttributes.HTTP_SESSION_SID, code)
                .sessionAttr(MinidPlusSessionAttributes.HTTP_SESSION_STATE, MinidPlusPasswordController.STATE_VERIFICATION_CODE_SMS)
                .param("otpCode", otp)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(view().name("minidplus_password_otp_email"))
                .andReturn();
    }

    @Test
    public void test_post_otp_sms_unsuccessful() throws Exception {
        String code = "abc123-bcdg-234325235-2436dfh-gsfh34w";
        when(minidPlusCache.getSSN(code)).thenReturn("55555555555");
        when(otcPasswordService.checkOTCCode(eq(code), eq(code))).thenReturn(false);
        MvcResult mvcResult = mockMvc.perform(post("/password?otpType=sms")
                .sessionAttr(MinidPlusSessionAttributes.HTTP_SESSION_SID, code)
                .sessionAttr(MinidPlusSessionAttributes.HTTP_SESSION_STATE, 2)
                .param("otpCode", code)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .with(csrf())
        )//.andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("minidplus_password_otp_sms"))
                .andExpect(model().hasErrors())
                .andReturn();
    }

    @Test
    public void test_post_otp_email_successful() throws Exception {
        String code = "abc123-bcdg-234325235-2436dfh-gsfh34w";
        String otp = "ab123";
        when(minidPlusCache.getSSN(code)).thenReturn(pid);
        when(otcPasswordService.checkOTCCode(eq(code), eq(otp))).thenReturn(true);
        MvcResult mvcResult = mockMvc.perform(post("/password?otpType=email")
                .sessionAttr(MinidPlusSessionAttributes.HTTP_SESSION_SID, code)
                .sessionAttr(MinidPlusSessionAttributes.HTTP_SESSION_STATE, MinidPlusPasswordController.STATE_VERIFICATION_CODE_EMAIL)
                .param("otpCode", otp)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .with(csrf())
        )//.andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("minidplus_password_change"))
                .andReturn();
    }

    @Test
    public void test_post_otp_email_unsuccessful() throws Exception {
        String code = "abc123-bcdg-234325235-2436dfh-gsfh34w";
        String otp = "ab123";
        when(minidPlusCache.getSSN(code)).thenReturn(pid);
        when(otcPasswordService.checkOTCCode(eq(code), eq(otp))).thenReturn(false);
        MvcResult mvcResult = mockMvc.perform(post("/password?otpType=email")
                .sessionAttr(MinidPlusSessionAttributes.HTTP_SESSION_SID, code)
                .sessionAttr(MinidPlusSessionAttributes.HTTP_SESSION_STATE, 3)
                .param("otpCode", otp)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .with(csrf())
        )//.andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("minidplus_password_otp_email"))
                .andExpect(model().hasErrors())
                .andReturn();
    }

    @Test
    public void test_post_new_password_successful() throws Exception {
        String code = "abc123-bcdg-234325235-2436dfh-gsfh34w";
        String newPassword = "Password0134";
        when(minidPlusCache.getSSN(code)).thenReturn(pid);
        when(authenticationService.changePassword(eq(code), eq(newPassword))).thenReturn(true);
        MvcResult mvcResult = mockMvc.perform(post("/password")
                .sessionAttr(MinidPlusSessionAttributes.HTTP_SESSION_SID, code)
                .sessionAttr(MinidPlusSessionAttributes.HTTP_SESSION_STATE, MinidPlusPasswordController.STATE_NEW_PASSWORD)
                .param("newPassword", newPassword)
                .param("reenterPassword", newPassword)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(view().name("minidplus_password_success"))
                .andReturn();
    }

    @Test
    public void test_post_new_password_not_successful() throws Exception {
        String code = "abc123-bcdg-234325235-2436dfh-gsfh34w";
        String newPassword = "Password0134";
        when(minidPlusCache.getSSN(code)).thenReturn(pid);
        when(authenticationService.changePassword(eq(code), eq(newPassword))).thenThrow(new MinidUserNotFoundException("User not here"));
        MvcResult mvcResult = mockMvc.perform(post("/password")
                .sessionAttr(MinidPlusSessionAttributes.HTTP_SESSION_SID, code)
                .sessionAttr(MinidPlusSessionAttributes.HTTP_SESSION_STATE, MinidPlusPasswordController.STATE_NEW_PASSWORD)
                .param("newPassword", newPassword)
                .param("reenterPassword", "pølse")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(view().name("minidplus_password_change"))
                .andReturn();

    }

    @Test
    public void test_success_receipt_continue_returns_to_authorization() throws Exception {

        mockMvc.perform(post("/password")
                .param(MinidPlusButtonType.CONTINUE.id(), "")
                .sessionAttr(HTTP_SESSION_STATE, MinidPlusPasswordController.STATE_PASSWORD_CHANGED)
                .with(csrf())
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/authorize"));
    }

    @Test
    public void test_user_cancels_otp_returns_to_authorization() throws Exception {

        mockMvc.perform(post("/password")
                .param("personalIdNumber", "")
                .param(MinidPlusButtonType.CANCEL.id(), "")
                .sessionAttr(HTTP_SESSION_STATE, 1)
                .with(csrf())
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/authorize"));
    }


}