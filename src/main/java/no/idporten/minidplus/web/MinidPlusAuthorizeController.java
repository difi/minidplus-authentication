package no.idporten.minidplus.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.idporten.domain.auth.AuthType;
import no.idporten.domain.sp.ServiceProvider;
import no.idporten.minidplus.domain.AuthorizationRequest;
import no.idporten.minidplus.domain.OneTimePassword;
import no.idporten.minidplus.domain.UserCredentials;
import no.idporten.minidplus.exception.minid.MinIDAuthException;
import no.idporten.minidplus.exception.minid.MinIDIncorrectCredentialException;
import no.idporten.minidplus.exception.minid.MinIDUserNotFoundException;
import no.idporten.minidplus.service.AuthenticationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.UUID;

import static no.idporten.minidplus.domain.MinidPlusSessionAttributes.*;

/**
 * Logic implementation of MinIdPluss web client module.
 */
@Controller
@RequestMapping(value = "/authorize")
@Slf4j
@Getter
@RequiredArgsConstructor
public class MinidPlusAuthorizeController {
    protected static final int STATE_AUTHENTICATED = -1;
    protected static final int STATE_USERDATA = 1;
    protected static final int STATE_VERIFICATION_CODE = 2;
    protected static final int STATE_ERROR = 3;

    private static final String IDPORTEN_INPUT_PREFIX = "idporten.input.";
    private static final String IDPORTEN_FEEDBACK_PREFIX = "idporten.feedback.";
    private static final String MOBILE_NUMBER = "mobileNumber";
    private static final String PERSONAL_ID_NUMBER = "personalIdNumber";
    private static final String PIN_CODE = "otpCode";
    private static final String PASSWORD = "password";
    private static final String EMAIL_ADDRESS = "emailAddress";
    private static final String IDPORTEN_INPUTBUTTON_PREFIX = "idporten.inputbutton.";

    private final LocaleResolver localeResolver;

    private final AuthenticationService authenticationService;

    @Value("${idporten.redirecturl}")
    private String redirectUrl;

    @GetMapping(produces = "text/html; charset=utf-8")
    public String doGet(HttpServletRequest request, HttpServletResponse response, /* //todo comment back in when ready @Valid*/ AuthorizationRequest authorizationRequest, Model model) throws IOException {
        request.getSession().invalidate();
        request.getSession().setAttribute(HTTP_SESSION_AUTH_TYPE, AuthType.MINID_PLUS);
        request.getSession().setAttribute(HTTP_SESSION_SID, UUID.randomUUID().toString());

        authorizationRequest.setStartService(request.getParameter(HTTP_SESSION_START_SERVICE)); //tmp workaround for stupid dash-param
        setLocale(request, response, authorizationRequest);
        request.getSession().setAttribute(AUTHORIZATION_REQUEST, authorizationRequest);

        UserCredentials userCredentials = new UserCredentials();
        model.addAttribute("userCredentials", userCredentials);
        return getNextView(request, STATE_USERDATA);
    }

    private void setLocale(HttpServletRequest request, HttpServletResponse response, AuthorizationRequest authorizationRequest) {
        String locale = authorizationRequest.getLocale();
        if (StringUtils.isEmpty(locale)) {
            locale = request.getLocale().toString();
        }
        localeResolver.setLocale(request, response, new Locale(locale));
        request.getSession().setAttribute("locale", locale);
    }

    @PostMapping
    public String postUserCredentials(HttpServletRequest request, @Valid @ModelAttribute("userCredentials") UserCredentials userCredentials, BindingResult result, Model model) throws IOException {

        int state = (int) request.getSession().getAttribute(HTTP_SESSION_STATE);
        String sid = (String) request.getSession().getAttribute(HTTP_SESSION_SID);
        AuthorizationRequest ar = (AuthorizationRequest) request.getSession().getAttribute(AUTHORIZATION_REQUEST);
        if (state == STATE_USERDATA) {
            if (result.hasErrors()) {
                return getNextView(request, STATE_USERDATA);
            }
            try {
                ServiceProvider sp = new ServiceProvider(ar.getService());
                authenticationService.authenticateUser(sid, userCredentials.getPersonalIdNumber(), userCredentials.getPassword(), sp);
            } catch (MinIDAuthException e) {
                String code = "";
                if (e instanceof MinIDIncorrectCredentialException) {
                    code = "auth.ui.usererror.format.password";
                } else if (e instanceof MinIDUserNotFoundException) {
                    code = "auth.ui.usererror.format.ssn";
                }
                result.addError(new ObjectError("authorizationRequest", new String[]{code}, null, "Login failed"));

                return getNextView(request, STATE_USERDATA);
            }

            OneTimePassword oneTimePassword = new OneTimePassword();
            model.addAttribute(oneTimePassword);
            return getNextView(request, STATE_VERIFICATION_CODE);
        } else {
            request.setAttribute("errorCode", "loginFailed");
            return getNextView(request, STATE_ERROR);
        }
    }

    @PostMapping(params = "otpCode")
    public String postOTP(HttpServletRequest request, @Valid @ModelAttribute("oneTimePassword") OneTimePassword oneTimePassword, BindingResult result) throws IOException {
        try {
            int state = (int) request.getSession().getAttribute(HTTP_SESSION_STATE);
            String sid = (String) request.getSession().getAttribute(HTTP_SESSION_SID);
            if (state == STATE_VERIFICATION_CODE) {

                String expectedOtp = oneTimePassword.getOtpCode(); //todo check otp minidPlusCache.getOTP(sid);
                if (sid != null && expectedOtp.equals(oneTimePassword.getOtpCode())) {
                    //todo success
                } else {
                    //todo add some error message.
                    return getNextView(request, STATE_VERIFICATION_CODE);
                }
                if (result.hasErrors()) {
                    return getNextView(request, STATE_VERIFICATION_CODE);
                }
                return getNextView(request, handleOtpInput(sid, oneTimePassword.getOtpCode()));
            }
        } catch (Exception e) {
            //todo
            prepareErrorPage("501", request);
        }
        return getNextView(request, STATE_ERROR);
    }

    private int handleOtpInput(String sid, String otp) {
        return STATE_AUTHENTICATED; //todo
    }

    private int prepareErrorPage(String errorCode, HttpServletRequest request) {
        if (StringUtils.isNotEmpty(errorCode)) {
            request.setAttribute("errorCode", errorCode);
        }
        return STATE_ERROR;
    }

    private String getNextView(HttpServletRequest request, int state) throws IOException {
        setSessionState(request, state);
        if (state == STATE_VERIFICATION_CODE) {
            return "minidplus_enter_otp";
        } else if (state == STATE_USERDATA) {
            return "minidplus_enter_credentials";
        } else if (state == STATE_ERROR) {
            return "error";
        } else if (state == STATE_AUTHENTICATED) {
            String url = buildUrl(request);
            log.debug("RedirectUrl: " + url);
            return "success";
            //return "redirect:" + url;
        }

        return "error";

    }

    private void setSessionState(HttpServletRequest request, int state) {
        request.getSession().setAttribute(HTTP_SESSION_STATE, state);
    }

    private String buildUrl(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String sid = (String) session.getAttribute("sid");
        AuthorizationRequest ar = (AuthorizationRequest) session.getAttribute(AUTHORIZATION_REQUEST);
        try {
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance()
                    .uri(new URI(redirectUrl))
                    .queryParam(HTTP_SESSION_SID, sid);
            if (ar != null) {
                uriComponentsBuilder.queryParam(HTTP_SESSION_REDIRECT_URL, ar.getRedirectUrl())
                        .queryParam(HTTP_SESSION_FORCE_AUTH, ar.getForceAuth())
                        .queryParam(HTTP_SESSION_GX_CHARSET, ar.getGx_charset())
                        .queryParam(HTTP_SESSION_LOCALE, ar.getLocale())
                        .queryParam(HTTP_SESSION_GOTO, ar.getGotoParam())
                        .queryParam(HTTP_SESSION_SERVICE, ar.getService());
            }
            uriComponentsBuilder.build()
                    .toUriString();
        } catch (URISyntaxException e) {
            log.error("Worng syntax durin URI building", e);
        }
        return null;
    }
}
