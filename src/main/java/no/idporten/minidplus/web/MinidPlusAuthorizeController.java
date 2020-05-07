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
import no.idporten.minidplus.exception.IDPortenExceptionID;
import no.idporten.minidplus.exception.minid.MinIDIncorrectCredentialException;
import no.idporten.minidplus.exception.minid.MinIDInvalidAcrLevelException;
import no.idporten.minidplus.exception.minid.MinIDPincodeException;
import no.idporten.minidplus.exception.minid.MinIDSystemException;
import no.idporten.minidplus.service.AuthenticationService;
import no.idporten.minidplus.service.OTCPasswordService;
import no.idporten.minidplus.service.ServiceproviderService;
import no.idporten.ui.impl.MinidPlusButtonType;
import no.minid.exception.MinidUserNotFoundException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
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
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.ValidatorFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    protected static final int STATE_CANCEL = 4;
    protected static final int STATE_CONSTRAINT_VIOLATIONS = 4;

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
    private static final Set<String> supportedLocales = Stream.of("nb", "nn", "en").collect(Collectors.toSet());

    private final LocaleResolver localeResolver;

    private final AuthenticationService authenticationService;

    private final ValidatorFactory validatorFactory;

    private final OTCPasswordService otcPasswordService;
    private final ServiceproviderService serviceproviderService;

    @Value("${minid-plus.registrationUri}")
    private String registrationUri;

    @GetMapping(produces = "text/html; charset=utf-8")
    public String doGet(HttpServletRequest request, HttpServletResponse response, AuthorizationRequest authorizationRequest, Model model) {
        Object state = request.getSession().getAttribute(HTTP_SESSION_STATE);
        if (state == null || (int) state != MinidPlusPasswordController.STATE_CANCEL) {
            request.getSession().invalidate();
            request.getSession().setAttribute(HTTP_SESSION_AUTH_TYPE, AuthType.MINID_PLUS);
            request.getSession().setAttribute(HTTP_SESSION_SID, UUID.randomUUID().toString());
            setLocale(request, response, authorizationRequest);
            Set<ConstraintViolation<AuthorizationRequest>> violations = validatorFactory.getValidator().validate(authorizationRequest);
            if (!violations.isEmpty()) {
                return getNextView(request, STATE_CONSTRAINT_VIOLATIONS);
            }
            request.getSession().setAttribute(MinidPlusSessionAttributes.AUTHORIZATION_REQUEST, authorizationRequest);
        }

        ServiceProvider sp;
        try {
            List<ServiceProvider> serviceProviders = serviceproviderService.findByEntityIdFilter(authorizationRequest.getSpEntityId());
            serviceProviders = serviceProviders.stream().filter(e -> e.getEntityId().equalsIgnoreCase(authorizationRequest.getSpEntityId())).collect(Collectors.toList());
            sp = serviceProviders.get(0);
            model.addAttribute("serviceprovider", sp);
            request.getSession().setAttribute(SERVICEPROVIDER, sp);
        } catch (Exception e) {
            warn("Exception handling otp: " + e.getMessage());
        }

        UserCredentials userCredentials = new UserCredentials();
        model.addAttribute(MODEL_USER_CREDENTIALS, userCredentials);
        return getNextView(request, STATE_USERDATA);
    }

    private void setLocale(HttpServletRequest request, HttpServletResponse response, AuthorizationRequest authorizationRequest) {
        String locale = authorizationRequest.getLocale();
        if (!supportedLocales.contains(locale)) {
            locale = "en";
        }
        localeResolver.setLocale(request, response, new Locale(locale));
        request.getSession().setAttribute("locale", locale);
    }

    @PostMapping
    public String postUserCredentials(HttpServletRequest request, @Valid @ModelAttribute(MODEL_USER_CREDENTIALS) UserCredentials userCredentials, BindingResult result, Model model) {

        int state = (int) request.getSession().getAttribute(HTTP_SESSION_STATE);
        String sid = (String) request.getSession().getAttribute(HTTP_SESSION_SID);
        ServiceProvider sp = (ServiceProvider) request.getSession().getAttribute(SERVICEPROVIDER);

        AuthorizationRequest ar = (AuthorizationRequest) request.getSession().getAttribute(AUTHORIZATION_REQUEST);
        // Check cancel
        if (buttonIsPushed(request, MinidPlusButtonType.CANCEL)) {
            model.addAttribute("redirectUrl", buildUrl(request, STATE_CANCEL));
            return getNextView(request, STATE_CANCEL);
        }
        if (state == STATE_USERDATA || state == STATE_CANCEL) {
            if (result.hasErrors()) {
                return getNextView(request, STATE_USERDATA);
            }
            try {
                authenticationService.authenticateUser(sid, userCredentials.getPersonalIdNumber(), userCredentials.getPassword(), sp, ar.getAcrValues());
            } catch (MinIDIncorrectCredentialException e) {
                result.addError(new FieldError(MODEL_AUTHORIZATION_REQUEST, PASSWORD, null, true, new String[]{"auth.ui.usererror.format.password"}, null, "Login failed"));
                return getNextView(request, STATE_USERDATA);
            } catch (MinidUserNotFoundException e) {
                result.addError(new ObjectError(MODEL_AUTHORIZATION_REQUEST, new String[]{"auth.ui.usererror.format.ssn"}, null, "Login failed"));
                return getNextView(request, STATE_USERDATA);
            } catch (MinIDSystemException e) {
                if (e.getExceptionId().equals(IDPortenExceptionID.LDAP_ATTRIBUTE_MISSING)) {
                    result.addError(new ObjectError(MODEL_AUTHORIZATION_REQUEST, new String[]{"auth.ui.usererror.format.missing.mobile"}, null, "Mobile number not registered on your user"));
                } else {
                    result.addError(new ObjectError(MODEL_AUTHORIZATION_REQUEST, new String[]{"no.idporten.error.line1"}, null, "Login failed"));
                }
                return getNextView(request, STATE_USERDATA);
            } catch (MinIDInvalidAcrLevelException e) {
                result.addError(new ObjectError(MODEL_AUTHORIZATION_REQUEST, new String[]{"no.idporten.module.minidplus.invalidacr"}, new Object[]{registrationUri}, "Login failed"));
                return getNextView(request, STATE_USERDATA);
            }

        } else {
            result.addError(new ObjectError(MODEL_AUTHORIZATION_REQUEST, new String[]{"no.idporten.error.line1"}, null, "System error"));
            result.addError(new ObjectError(MODEL_AUTHORIZATION_REQUEST, new String[]{"no.idporten.error.line3"}, null, "Please try again"));
            prepareErrorPage(request, model);
            return getNextView(request, STATE_ERROR);
        }
        OneTimePassword oneTimePassword = new OneTimePassword();
        model.addAttribute(oneTimePassword);
        return getNextView(request, STATE_VERIFICATION_CODE);

    }

    private void prepareErrorPage(HttpServletRequest request, Model model) {
        String requestUrl = request.getRequestURL().toString();
        model.addAttribute("requestUrl", requestUrl);
        Map<String, String[]> params = request.getParameterMap();
        HashMap<String, String> finalParams = new HashMap<>();
        params.keySet().forEach(key -> finalParams.put(key, (request.getParameterMap().get(key) != null && request.getParameterMap().get(key).length > 0) ? request.getParameterMap().get(key)[0] : null));
        finalParams.remove("personalIdNumber");
        finalParams.remove("password");
        model.addAttribute("params", finalParams);
    }

    private boolean buttonIsPushed(HttpServletRequest request, MinidPlusButtonType type) {
        return request.getParameter(type.id()) != null;
    }

    @PostMapping(params = "otpCode")
    public String postOTP(HttpServletRequest request, @Valid @ModelAttribute(MODEL_ONE_TIME_CODE) OneTimePassword oneTimePassword, BindingResult result, Model model) {
        try {
            int state = (int) request.getSession().getAttribute(HTTP_SESSION_STATE);
            String sid = (String) request.getSession().getAttribute(HTTP_SESSION_SID);
            // Check cancel
            if (buttonIsPushed(request, MinidPlusButtonType.CANCEL)) {
                model.addAttribute("redirectUrl", buildUrl(request, STATE_CANCEL));
                return getNextView(request, STATE_CANCEL);
            }
            if (state == STATE_VERIFICATION_CODE) {
                if (authenticationService.authenticateOtpStep(sid, oneTimePassword.getOtpCode())) {
                    model.addAttribute("redirectUrl", buildUrl(request, STATE_AUTHENTICATED));
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
        } else if (state == STATE_AUTHENTICATED || state == STATE_CANCEL || state == STATE_CONSTRAINT_VIOLATIONS) {
            request.getSession().invalidate();
            return "redirect_to_idporten";
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
                        .queryParam(HTTP_SESSION_SID, sid);

                uriComponentsBuilder.queryParam(HTTP_SESSION_REDIRECT_URI, ar.getRedirectUri())
                        .queryParam(HTTP_SESSION_LOCALE, ar.getLocale())
                        .queryParam(HTTP_SESSION_GOTO, ar.getGotoParam())
                        .queryParam(HTTP_SESSION_CLIENT_STATE, ar.getState());
                if (state == STATE_CANCEL) {
                    uriComponentsBuilder.queryParam("error", ABORTED_BY_USER);
                    uriComponentsBuilder.queryParam(HTTP_SESSION_SERVICE, START_SERVICE);
                } else if (state == STATE_CONSTRAINT_VIOLATIONS) {
                    uriComponentsBuilder.queryParam("error", CONSTRAINT_VIOLATIONS);
                    uriComponentsBuilder.queryParam(HTTP_SESSION_SERVICE, START_SERVICE);
                } else {
                    uriComponentsBuilder.queryParam(HTTP_SESSION_SERVICE, SERVICE_NAME);
                }
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
