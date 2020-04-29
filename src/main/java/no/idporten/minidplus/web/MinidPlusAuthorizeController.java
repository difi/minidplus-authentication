package no.idporten.minidplus.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.resilience.CorrelationId;
import no.idporten.domain.auth.AuthType;
import no.idporten.domain.sp.ServiceProvider;
import no.idporten.minidplus.domain.AuthorizationRequest;
import no.idporten.minidplus.domain.MinidPlusSessionAttributes;
import no.idporten.minidplus.domain.OneTimePassword;
import no.idporten.minidplus.domain.UserCredentials;
import no.idporten.minidplus.exception.minid.MinIDIncorrectCredentialException;
import no.idporten.minidplus.exception.minid.MinIDPincodeException;
import no.idporten.minidplus.exception.minid.MinIDUserNotFoundException;
import no.idporten.minidplus.service.AuthenticationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
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
import java.util.Map;
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

    public static final String MODEL_AUTHORIZATION_REQUEST = "authorizationRequest";
    public static final String MODEL_ONE_TIME_CODE = "oneTimePassword";
    public static final String MODEL_USER_CREDENTIALS = "userCredentials";

    private final LocaleResolver localeResolver;

    private final AuthenticationService authenticationService;


    @GetMapping(produces = "text/html; charset=utf-8")
    public String doGet(HttpServletRequest request, HttpServletResponse response, /* //todo comment back in when ready @Valid*/ AuthorizationRequest authorizationRequest, Model model) {
        request.getSession().invalidate();
        request.getSession().setAttribute(HTTP_SESSION_AUTH_TYPE, AuthType.MINID_PLUS);
        request.getSession().setAttribute(HTTP_SESSION_SID, UUID.randomUUID().toString());

        authorizationRequest.setStartService(request.getParameter(HTTP_SESSION_START_SERVICE)); //tmp workaround for stupid dash-param
        setLocale(request, response, authorizationRequest);
        request.getSession().setAttribute(MinidPlusSessionAttributes.AUTHORIZATION_REQUEST, authorizationRequest);

        UserCredentials userCredentials = new UserCredentials();
        model.addAttribute(MODEL_USER_CREDENTIALS, userCredentials);
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
    public String postUserCredentials(HttpServletRequest request, @Valid @ModelAttribute(MODEL_USER_CREDENTIALS) UserCredentials userCredentials, BindingResult result, Model model) {

        int state = (int) request.getSession().getAttribute(HTTP_SESSION_STATE);
        String sid = (String) request.getSession().getAttribute(HTTP_SESSION_SID);
        AuthorizationRequest ar = (AuthorizationRequest) request.getSession().getAttribute(AUTHORIZATION_REQUEST);

        if (state == STATE_USERDATA) {
            if (result.hasErrors()) {
                return getNextView(request, STATE_USERDATA);
            }
            try {
                ServiceProvider sp = new ServiceProvider(ar.getSpEntityId());
                sp.setName(ar.getSpEntityId());//todo lookup from ldap
                authenticationService.authenticateUser(sid, userCredentials.getPersonalIdNumber(), userCredentials.getPassword(), sp);
            } catch (MinIDIncorrectCredentialException e) {
                result.addError(new FieldError(MODEL_AUTHORIZATION_REQUEST, PASSWORD, null, true, new String[]{"auth.ui.usererror.format.password"}, null, "Login failed"));
                return getNextView(request, STATE_USERDATA);
            } catch (MinIDUserNotFoundException e) {
                result.addError(new ObjectError(MODEL_AUTHORIZATION_REQUEST, new String[]{"auth.ui.usererror.format.ssn"}, null, "Login failed"));
                return getNextView(request, STATE_USERDATA);
            }

        } else {
            result.addError(new ObjectError(MODEL_AUTHORIZATION_REQUEST, new String[]{"no.idporten.error.line1"}, null, "System error"));
            result.addError(new ObjectError(MODEL_AUTHORIZATION_REQUEST, new String[]{"no.idporten.error.line3"}, null, "Please try again"));
            return getNextView(request, STATE_USERDATA);
        }
        OneTimePassword oneTimePassword = new OneTimePassword();
        model.addAttribute(oneTimePassword);
        return getNextView(request, STATE_VERIFICATION_CODE);

    }

    @PostMapping(params = "otpCode")
    public String postOTP(HttpServletRequest request, @Valid @ModelAttribute(MODEL_ONE_TIME_CODE) OneTimePassword oneTimePassword, BindingResult result, Model model) {
        try {
            int state = (int) request.getSession().getAttribute(HTTP_SESSION_STATE);
            String sid = (String) request.getSession().getAttribute(HTTP_SESSION_SID);
            if (state == STATE_VERIFICATION_CODE) {
                if (authenticationService.checkOTCCode(sid, oneTimePassword.getOtpCode())) {
                    model.addAttribute("redirectUrl", buildUrl(request));
                    return getNextView(request, STATE_AUTHENTICATED);
                } else {
                    result.addError(new ObjectError(MODEL_ONE_TIME_CODE, new String[]{"auth.ui.usererror.wrong.pincode"}, null, "Try again"));
                }
            }
        } catch (MinIDPincodeException e) {
            result.addError(new ObjectError(MODEL_ONE_TIME_CODE, new String[]{"auth.ui.usererror.format.otc.locked"}, null, "Too many attempts"));
        } catch (Exception e) {
            warn("Exception handling otp: " + e.getMessage());
            result.addError(new ObjectError(MODEL_ONE_TIME_CODE, new String[]{"no.idporten.error.line1"}, null, "System error"));
            result.addError(new ObjectError(MODEL_ONE_TIME_CODE, new String[]{"no.idporten.error.line3"}, null, "Please try again"));
        }
        return getNextView(request, STATE_VERIFICATION_CODE);
    }


    private String getNextView(HttpServletRequest request, int state) {
        setSessionState(request, state);
        if (state == STATE_VERIFICATION_CODE) {
            return "minidplus_enter_otp";
        } else if (state == STATE_USERDATA) {
            return "minidplus_enter_credentials";
        } else if (state == STATE_ERROR) {
            return "error";
        } else if (state == STATE_AUTHENTICATED) {
            return returnAuthorizationCode(request);
        }
        return "error";
    }


    private String returnAuthorizationCode(HttpServletRequest request) {
        String url = buildUrl(request);
        if (url != null) {
            if (log.isDebugEnabled()) {
                log.debug("RedirectUrl: " + url);
            }

//            return "redirect:" + url;
            return "redirect_to_idporten";
        } else {
            warn("Using tmp test redirect uri");
            return "success"; //todo fjern før prod!
        }
    }

    private void setSessionState(HttpServletRequest request, int state) {
        request.getSession().setAttribute(HTTP_SESSION_STATE, state);
    }

    private String buildUrl(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String sid = (String) session.getAttribute("sid");
        AuthorizationRequest ar = (AuthorizationRequest) session.getAttribute(MinidPlusSessionAttributes.AUTHORIZATION_REQUEST);
        if (ar != null && StringUtils.isNotEmpty(ar.getRedirectUrl())) {
            try {
                UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance()
                        .uri(new URI(ar.getRedirectUrl()))
                        .queryParam(HTTP_SESSION_SID, sid);

                uriComponentsBuilder.queryParam(HTTP_SESSION_REDIRECT_URL, ar.getRedirectUrl())
                        .queryParam(HTTP_SESSION_FORCE_AUTH, ar.getForceAuth())
                        .queryParam(HTTP_SESSION_GX_CHARSET, ar.getGx_charset())
                        .queryParam(HTTP_SESSION_LOCALE, ar.getLocale())
                        .queryParam(HTTP_SESSION_GOTO, ar.getGotoParam())
                        .queryParam(HTTP_SESSION_SERVICE, ar.getService());

                return uriComponentsBuilder.build()
                        .toUriString();
            } catch (URISyntaxException e) {
                log.error(CorrelationId.get() + " Wrong syntax during URI building", e);
            }
        }
        return null;
    }

    private void warn(String message) {
        log.warn(CorrelationId.get() + " " + message);
    }
}
