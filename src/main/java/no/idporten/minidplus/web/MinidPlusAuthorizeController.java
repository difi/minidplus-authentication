package no.idporten.minidplus.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.resilience.CorrelationId;
import no.idporten.domain.auth.AuthType;
import no.idporten.domain.sp.ServiceProvider;
import no.idporten.minidplus.domain.*;
import no.idporten.minidplus.exception.minid.*;
import no.idporten.minidplus.service.AuthenticationService;
import no.idporten.minidplus.service.OTCPasswordService;
import no.idporten.minidplus.service.ServiceproviderService;
import no.idporten.minidplus.validator.InputTerminator;
import no.minid.exception.MinidUserInvalidException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
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
import javax.validation.ValidatorFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.idporten.minidplus.domain.MinidPlusSessionAttributes.*;
import static no.idporten.minidplus.domain.MinidState.*;

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
    protected static final int STATE_LOGIN_VERIFICATION_CODE = 2;
    protected static final int STATE_LOGIN_WRONG_ACR = 4;

    private static final String MOBILE_NUMBER = "mobileNumber";
    private static final String PERSONAL_ID_NUMBER = "personalIdNumber";
    private static final String PIN_CODE = "otpCode";
    private static final String PASSWORD = "password";
    private static final String EMAIL_ADDRESS = "emailAddress";
    private static final String IDPORTEN_INPUTBUTTON_PREFIX = "idporten.inputbutton.";

    public static final String MODEL_AUTHORIZATION_REQUEST = "authorizationRequest";
    public static final String MODEL_ONE_TIME_CODE = "oneTimePassword";
    public static final String MODEL_USER_CREDENTIALS = "userCredentials";

    private static final String ABORTED_BY_USER = "aborted_by_user";
    private static final String CONSTRAINT_VIOLATIONS = "contraint_violations_in_authorize_request";
    private static final String START_SERVICE = "start-service";
    private static final Set<String> supportedLocales = Stream.of("nb", "nn", "en", "se").collect(Collectors.toSet());

    @Value("${minid-plus.serverid}")
    private String serverid;

    private final LocaleResolver localeResolver;

    private final AuthenticationService authenticationService;

    private final ValidatorFactory validatorFactory;

    private final OTCPasswordService otcPasswordService;
    private final ServiceproviderService serviceproviderService;

    @Value("${minid-plus.registrationUri}")
    private String registrationUri;

    @GetMapping(produces = "text/html; charset=utf-8")
    public String doGet(HttpServletRequest request, HttpServletResponse response, @Valid AuthorizationRequest authorizationRequest, Model model) {
        if (log.isDebugEnabled()) {
            log.debug("Authorizing user with " + authorizationRequest.toString());
        }

        request.getSession().invalidate();
        request.getSession().setAttribute(HTTP_SESSION_AUTH_TYPE, AuthType.MINID_PLUS);
        request.getSession().setAttribute(HTTP_SESSION_SID, UUID.randomUUID().toString());
        setLocale(request, response, authorizationRequest);
        request.getSession().setAttribute(MinidPlusSessionAttributes.AUTHORIZATION_REQUEST, authorizationRequest);
        ServiceProvider sp = getServiceProvider(authorizationRequest.getSpEntityId(), request.getHeader(HttpHeaders.HOST));
        request.getSession().setAttribute(SERVICEPROVIDER, sp);
        UserCredentials userCredentials = new UserCredentials();
        model.addAttribute(MODEL_USER_CREDENTIALS, userCredentials);
        return getNextView(request, STATE_START_LOGIN);
    }

    @PostMapping(params = {"personalIdNumber"})
    public String postUserCredentials(HttpServletRequest request, @Valid @ModelAttribute(MODEL_USER_CREDENTIALS) UserCredentials userCredentials, BindingResult result, Model model) {
        Object state = request.getSession().getAttribute(HTTP_SESSION_STATE);

        String sid = (String) request.getSession().getAttribute(HTTP_SESSION_SID);
        ServiceProvider sp = (ServiceProvider) request.getSession().getAttribute(SERVICEPROVIDER);
        String pwd = userCredentials.getPassword();
        String pid = userCredentials.getPersonalIdNumber();
        userCredentials.setPersonalIdNumber("");
        userCredentials.setPassword("");
        AuthorizationRequest ar = (AuthorizationRequest) request.getSession().getAttribute(AUTHORIZATION_REQUEST);

        if (state != null && ((int) state == STATE_START_LOGIN)) {
            if (result.hasErrors()) {
                warn("There are contraint violations: " + Arrays.toString(result.getAllErrors().toArray()));
                InputTerminator.clearAllInput(userCredentials, result, model);
                return getNextView(request, STATE_START_LOGIN);
            }
            try {
                authenticationService.authenticateUser(sid, pid, pwd, sp, ar.getAcrValues());
                OneTimePassword oneTimePassword = new OneTimePassword();
                model.addAttribute(oneTimePassword);
                return getNextView(request, STATE_LOGIN_VERIFICATION_CODE);
            } catch (MinIDIncorrectCredentialException e) {
                warn("Incorrect credentials " + e.getMessage());
                if (e.getMessage().equalsIgnoreCase("Password validation failed, last try.")) {
                    result.addError(new FieldError(MODEL_AUTHORIZATION_REQUEST, PASSWORD, null, true, new String[]{"auth.ui.usererror.wrong.credentials.lasttry"}, null, "Wrong credentials"));
                } else {
                    result.addError(new FieldError(MODEL_AUTHORIZATION_REQUEST, PASSWORD, null, true, new String[]{"auth.ui.usererror.wrong.credentials"}, null, "Wrong credentials"));
                }
                return getNextView(request, STATE_START_LOGIN);
            } catch (MinidUserInvalidException e) {
                warn("User exception occurred " + e.getMessage());
                result.addError(new ObjectError(MODEL_AUTHORIZATION_REQUEST, new String[]{"auth.ui.error.sendingotc.messsage"}, null, "Mobile number not registered on your user"));
            } catch (MinIDInvalidAcrLevelException e) {
                warn("User attempted to log in with wrong acr: " + e.getMessage());
                return getNextView(request, STATE_LOGIN_WRONG_ACR);
            } catch (MinIDQuarantinedUserException e) {
                warn("User quarantined " + e.getMessage());
                if (e.getMessage().equalsIgnoreCase("User is closed")) {
                    model.addAttribute("alertMessage", "auth.ui.error.closed.message");
                } else if (e.getMessage().equalsIgnoreCase("User has been in quarantine for more than one hour.")) {
                    model.addAttribute("alertMessage", "auth.ui.error.locked.message");
                } else {
                    model.addAttribute("alertMessage", "auth.ui.error.quarantined.message");
                }
                return getNextView(request, STATE_ALERT);
            } catch (Exception e) {
                log.error("Unexpected exception occurred during post user credentials", e);
                result.addError(new ObjectError(MODEL_AUTHORIZATION_REQUEST, new String[]{"no.idporten.error.line1"}, null, "Login failed"));
            }
            model.addAttribute(new UserCredentials());
            return getNextView(request, STATE_START_LOGIN);
        } else {
            log.error("Illegal state " + state);
            model.addAttribute("errorMsg", "403");
            return getNextView(request, STATE_ERROR);
        }
    }

    @PostMapping(params = {"otpCode"})
    public String postOTP(HttpServletRequest request, @Valid @ModelAttribute(MODEL_ONE_TIME_CODE) OneTimePassword oneTimePassword, BindingResult result, Model model) {
        try {
            int state = (int) request.getSession().getAttribute(HTTP_SESSION_STATE);
            String sid = (String) request.getSession().getAttribute(HTTP_SESSION_SID);
            String otp = oneTimePassword.getOtpCode();
            oneTimePassword.setOtpCode("");

            if (result.hasErrors()) {
                warn("There are contraint violations: " + Arrays.toString(result.getAllErrors().toArray()));
                InputTerminator.clearAllInput(oneTimePassword, result, model);
                return getNextView(request, STATE_LOGIN_VERIFICATION_CODE);
            }
            if (state == STATE_LOGIN_VERIFICATION_CODE) {
                if (authenticationService.authenticateOtpStep(sid, otp)) {
                    return backToIdporten(request, model, STATE_AUTHENTICATED);
                } else {
                    result.addError(new ObjectError(MODEL_ONE_TIME_CODE, new String[]{"auth.ui.usererror.wrong.pincode"}, null, "Try again"));
                }
            }
        } catch (MinIDTimeoutException e) {
            warn("User cache timed out " + e.getMessage());
            result.addError(new ObjectError(MODEL_ONE_TIME_CODE, new String[]{"no.idporten.module.minidplus.timeout"}, null, "Timeout"));
            model.addAttribute("alertMessage", "no.idporten.module.minidplus.timeout");
            return getNextView(request, STATE_ALERT);
        } catch (MinIDPincodeException e) {
            warn("User pincode locked " + e.getMessage());
            result.addError(new ObjectError(MODEL_ONE_TIME_CODE, new String[]{"auth.ui.usererror.format.otc.locked"}, null, "Too many attempts"));
            model.addAttribute("alertMessage", "auth.ui.error.quarantined.message");
            return getNextView(request, STATE_ALERT);
        } catch (Exception e) {
            warn("Exception handling otp: " + e.getMessage());
            result.addError(new ObjectError(MODEL_ONE_TIME_CODE, new String[]{"no.idporten.error.line1"}, null, "System error"));
            result.addError(new ObjectError(MODEL_ONE_TIME_CODE, new String[]{"no.idporten.error.line3"}, null, "Please try again"));
        }
        model.addAttribute(new OneTimePassword());
        return getNextView(request, STATE_LOGIN_VERIFICATION_CODE);
    }

    @PostMapping(params = {"cancel", "!next"})
    public String cancel(HttpServletRequest request, Model model) {
        return backToIdporten(request, model, MinidState.STATE_CANCEL);
    }

    private ServiceProvider getServiceProvider(String entityId, String hostName) {
        try {
            return serviceproviderService.getServiceProvider(entityId, hostName);
        } catch (Exception e) {
            warn("Exception getting service provider info: " + e.getMessage());
            return new ServiceProvider("idporten");
        }
    }

    private void setLocale(HttpServletRequest request, HttpServletResponse response, AuthorizationRequest authorizationRequest) {
        String locale = authorizationRequest.getLocale();
        if (!supportedLocales.contains(locale)) {
            locale = "en";
        }
        localeResolver.setLocale(request, response, new Locale(locale));
        request.getSession().setAttribute("locale", locale);
    }

    private String getNextView(HttpServletRequest request, int state) {
        setSessionState(request, state);
        if (state == STATE_LOGIN_VERIFICATION_CODE) {
            return "minidplus_enter_otp";
        } else if (state == STATE_START_LOGIN) {
            return "minidplus_enter_credentials";
        } else if (state == STATE_AUTHENTICATED || state == MinidState.STATE_CANCEL) {
            request.getSession().invalidate();
            return "redirect_to_idporten";
        } else if (state == STATE_ALERT) {
            return "alert";
        } else if (state == STATE_LOGIN_WRONG_ACR) {
            return "error_acr";
        }
        return "error";
    }

    private void setSessionState(HttpServletRequest request, int state) {
        request.getSession().setAttribute(HTTP_SESSION_STATE, state);
    }

    private String buildUrl(HttpServletRequest request, int state) {
        HttpSession session = request.getSession();
        String sid = (String) session.getAttribute("sid");
        AuthorizationRequest ar = (AuthorizationRequest) session.getAttribute(MinidPlusSessionAttributes.AUTHORIZATION_REQUEST);
        if (ar != null && StringUtils.isNotEmpty(ar.getRedirectUri())) {
            try {
                UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance()
                        .uri(new URI(ar.getRedirectUri()))
                        .queryParam(HTTP_SESSION_SID, sid)
                        .queryParam(SERVERID, serverid);

                uriComponentsBuilder.queryParam(HTTP_SESSION_REDIRECT_URI, ar.getRedirectUri())
                        .queryParam(HTTP_SESSION_LOCALE, ar.getLocale())
                        .queryParam(HTTP_SESSION_GOTO, ar.getGotoParam())
                        .queryParam(HTTP_SESSION_CLIENT_STATE, ar.getState());
                if (state == MinidState.STATE_CANCEL) {
                    uriComponentsBuilder.queryParam("error", ABORTED_BY_USER);
                    uriComponentsBuilder.queryParam(HTTP_SESSION_SERVICE, START_SERVICE);
                } else {
                    uriComponentsBuilder.queryParam(HTTP_SESSION_SERVICE, SERVICE_NAME);
                }
                String uri = uriComponentsBuilder.build()
                        .toUriString();
                if (log.isDebugEnabled()) {
                    log.debug("Redirecting back to " + uri);
                }
                return uri;
            } catch (URISyntaxException e) {
                log.error(CorrelationId.get() + " Wrong syntax during URI building", e);
            }
        }
        return null;
    }

    private String backToIdporten(HttpServletRequest request, Model model, int backState) {
        model.addAttribute("redirectUrl", buildUrl(request, backState));
        return getNextView(request, backState);
    }

    private void warn(String message) {
        log.warn(CorrelationId.get() + " " + message);
    }
}
