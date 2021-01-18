package no.idporten.minidplus.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.resilience.CorrelationId;
import no.idporten.domain.auth.AuthType;
import no.idporten.domain.sp.ServiceProvider;
import no.idporten.domain.user.MinidUser;
import no.idporten.minidplus.domain.AuthorizationRequest;
import no.idporten.minidplus.domain.LevelOfAssurance;
import no.idporten.minidplus.domain.MinidPlusSessionAttributes;
import no.idporten.minidplus.domain.UserInputtedCode;
import no.idporten.minidplus.domain.UserCredentials;
import no.idporten.minidplus.exception.IDPortenExceptionID;
import no.idporten.minidplus.exception.minid.MinIDIncorrectCredentialException;
import no.idporten.minidplus.exception.minid.MinIDInvalidAcrLevelException;
import no.idporten.minidplus.exception.minid.MinIDPincodeException;
import no.idporten.minidplus.exception.minid.MinIDQuarantinedUserException;
import no.idporten.minidplus.exception.minid.MinIDTimeoutException;
import no.idporten.minidplus.service.*;
import no.idporten.minidplus.util.MinIdPlusButtonType;
import no.idporten.minidplus.util.MinIdState;
import no.idporten.minidplus.validator.InputTerminator;
import no.idporten.sdk.oidcserver.OAuth2Exception;
import no.idporten.sdk.oidcserver.OpenIDConnectIntegration;
import no.idporten.sdk.oidcserver.protocol.Authorization;
import no.idporten.sdk.oidcserver.protocol.AuthorizationResponse;
import no.idporten.sdk.oidcserver.protocol.ErrorResponse;
import no.idporten.sdk.oidcserver.protocol.PushedAuthorizationRequest;
import no.idporten.sdk.oidcserver.protocol.PushedAuthorizationResponse;
import no.idporten.sdk.oidcserver.protocol.TokenRequest;
import no.idporten.sdk.oidcserver.protocol.TokenResponse;
import no.minid.exception.MinidUserInvalidException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.idporten.minidplus.domain.MinidPlusSessionAttributes.*;
import static no.idporten.minidplus.util.MinIdPlusViews.*;
import static no.idporten.minidplus.util.MinIdState.STATE_ALERT;
import static no.idporten.minidplus.util.MinIdState.STATE_ERROR;
import static no.idporten.minidplus.util.MinIdState.STATE_START_LOGIN;

/**
 * Logic implementation of MinIdPluss web client module.
 */
@Controller
@RequestMapping
@Slf4j
@Getter
@RequiredArgsConstructor
public class MinIdPlusAuthorizeController {

    @Value("${minid-plus.serverid}")
    private String serverid;

    @Value("${minid-plus.registrationUri}")
    private String registrationUri;

    @Value("${minid-plus.callback-method-post:true}")
    private boolean postCallBackMethod;

    //internal states
    protected static final int STATE_AUTHENTICATED = -1;
    protected static final int STATE_LOGIN_VERIFICATION_CODE = 2;
    protected static final int STATE_LOGIN_WRONG_ACR = 4;
    protected static final int STATE_LOGIN_PINCODE = 6;

    private static final String CODE = "code";
    //private models
    private static final String MODEL_AUTHORIZATION_REQUEST = "authorizationRequest";
    private static final String MODEL_ONE_TIME_CODE = "oneTimePassword";


    //states to idporten
    private static final String ABORTED_BY_USER = "aborted_by_user";
    private static final String START_SERVICE = "start-service";

    private static final Set<String> supportedLocales = Stream.of("nb", "nn", "en", "se").collect(Collectors.toSet());

    //internal views
    protected static final String VIEW_LOGIN_ENTER_OTP = "minidplus_enter_otp";
    protected static final String VIEW_LOGIN_ENTER_PINCODE = "minid_enter_pincode";

    private final LocaleResolver localeResolver;

    private final AuthenticationService authenticationService;

    private final ValidatorFactory validatorFactory;

    private final OTCPasswordService otcPasswordService;

    private final ServiceproviderService serviceproviderService;

    private final MinidPlusCache minidPlusCache;

    private final OpenIDConnectIntegration openIDConnectIntegration;

    private final MinidIdentityService minidIdentityService;
    private final PinCodeService pinCodeService;


    @PostMapping(value = "/v2/par", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PushedAuthorizationResponse> par(HttpServletRequest request) {
        return ResponseEntity.ok(openIDConnectIntegration.process(new PushedAuthorizationRequest(request)));
    }

    @GetMapping("/v2/authorize")
    public String authorize(HttpServletRequest req, HttpServletResponse response, Model model) throws IOException {
        PushedAuthorizationRequest authorizationRequest = openIDConnectIntegration.process(new no.idporten.sdk.oidcserver.protocol.AuthorizationRequest(req));
        req.getSession().setAttribute("authorization_request", authorizationRequest);
        return mapToAuthorizeRequest(req, response, authorizationRequest, model);
    }


    private String mapToAuthorizeRequest(HttpServletRequest servletRequest, HttpServletResponse servletResponse,
                                         PushedAuthorizationRequest pushedAuthorizationRequest, Model model) {
        AuthorizationRequest request = new AuthorizationRequest(pushedAuthorizationRequest.getRedirectUri(),
                pushedAuthorizationRequest.getClientId(),
                pushedAuthorizationRequest.getResponseType(),
                LevelOfAssurance.resolve(pushedAuthorizationRequest.getResolvedAcrValue()),
                pushedAuthorizationRequest.getState(),
                pushedAuthorizationRequest.getParameter("X-goto"),
                pushedAuthorizationRequest.getResolvedUiLocale());
        return doGet(servletRequest, servletResponse, request, model);
    }

    @PostMapping(value = "/v2/token",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<TokenResponse> token(HttpServletRequest request) {
        return ResponseEntity.ok(openIDConnectIntegration.process(new TokenRequest(request)));
    }

    @ExceptionHandler(OAuth2Exception.class)
    public ResponseEntity<ErrorResponse> handleError(HttpSession session, OAuth2Exception exception) {
        session.invalidate();
        log.warn(exception.getMessage(), exception.getCause());
        return ResponseEntity.status(exception.getHttpStatusCode()).body(exception.errorResponse());
    }

    @GetMapping(value = "/authorize", produces = "text/html; charset=utf-8")
    public String doGet(HttpServletRequest request, HttpServletResponse response, @Valid AuthorizationRequest authorizationRequest, Model model) {
        if (log.isDebugEnabled()) {
            log.debug("Authorizing user with " + authorizationRequest.toString());
        }

        clearSessionAndCache(request);
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

    @PostMapping(value = "/v2/authorize", params = {"personalIdNumber"})
    public String postUserCredentialsV2(HttpServletRequest request, @Valid @ModelAttribute(MODEL_USER_CREDENTIALS) UserCredentials userCredentials, BindingResult result, Model model) {
//        request.getSession().invalidate();
        return postUserCredentials(request, userCredentials, result, model);
    }

    @PostMapping(value = "/authorize", params = {"personalIdNumber"})
    public String postUserCredentials(HttpServletRequest request, @Valid @ModelAttribute(MODEL_USER_CREDENTIALS) UserCredentials userCredentials, BindingResult result, Model model) {
        Object state = request.getSession().getAttribute(HTTP_SESSION_STATE);

        String sid = (String) request.getSession().getAttribute(HTTP_SESSION_SID);
        ServiceProvider sp = (ServiceProvider) request.getSession().getAttribute(SERVICEPROVIDER);
        String pwd = userCredentials.getPassword();
        String pid = userCredentials.getPersonalIdNumber();
        userCredentials.clearValues();
        // Check cancel
        if (buttonIsPushed(request, MinIdPlusButtonType.CANCEL)) {
            return backToClient(request, model, MinIdState.STATE_CANCEL);
        }
        AuthorizationRequest ar = (AuthorizationRequest) request.getSession().getAttribute(AUTHORIZATION_REQUEST);

        if (state != null && ((int) state == STATE_START_LOGIN)) {
            if (result.hasErrors()) {
                warn("There are contraint violations: " + Arrays.toString(result.getAllErrors().toArray()));
                InputTerminator.clearAllInput(userCredentials, result, model);
                return getNextView(request, STATE_START_LOGIN);
            }
            try {
                MinidUser identity = minidIdentityService.getIdentity(pid);
                authenticationService.authenticateUser(sid, identity, pwd, ar.getAcrValues());
                if (identity.getPrefersOtc()) {
                    otcPasswordService.sendSMSOtp(sid, sp, identity);
                    UserInputtedCode userInputtedCode = new UserInputtedCode();
                    model.addAttribute(userInputtedCode);
                    return getNextView(request, STATE_LOGIN_VERIFICATION_CODE);
                } else {
                    model.addAttribute("pincodeNumber", pinCodeService.getRandomCode(identity));
                    UserInputtedCode userInputtedCode = new UserInputtedCode();
                    model.addAttribute(userInputtedCode);
                    return getNextView(request, STATE_LOGIN_PINCODE);

                }
            } catch (MinIDIncorrectCredentialException e) {
                warn("Incorrect credentials " + e.getMessage());
                if (e.getMessage().equalsIgnoreCase("Password validation failed, last try.")) {
                    result.addError(new ObjectError(MODEL_AUTHORIZATION_REQUEST, new String[]{"auth.ui.usererror.wrong.credentials.lasttry"}, null, "Wrong credentials"));
                } else {
                    result.addError(new ObjectError(MODEL_AUTHORIZATION_REQUEST, new String[]{"auth.ui.usererror.wrong.credentials"}, null, "Wrong credentials"));
                }
            } catch (MinidUserInvalidException e) {
                warn("User exception occurred " + e.getMessage());
                result.addError(new ObjectError(MODEL_AUTHORIZATION_REQUEST, new String[]{"auth.ui.error.sendingotc.messsage"}, null, "Mobile number not registered on your user"));
            } catch (MinIDInvalidAcrLevelException e) {
                warn("User attempted to log in with wrong acr: " + e.getMessage());
                model.addAttribute(SERVICEPROVIDER, sp);
                return getNextView(request, STATE_LOGIN_WRONG_ACR);
            } catch (MinIDQuarantinedUserException e) {
                addQuarantineMessage(model, e);
                return getNextView(request, STATE_ALERT);
            } catch (Exception e) {
                log.error("Unexpected exception occurred during post user credentials", e);
                result.addError(new ObjectError(MODEL_AUTHORIZATION_REQUEST, new String[]{"no.idporten.error.line1"}, null, "Login failed"));
            }
            return getNextView(request, STATE_START_LOGIN);
        } else {
            log.error("Illegal state " + state);
            model.addAttribute("errorMsg", "403");
            return getNextView(request, STATE_ERROR);
        }
    }

    private void addQuarantineMessage(Model model, MinIDQuarantinedUserException e) {
        warn("User quarantined " + e.getMessage());
        if (IDPortenExceptionID.IDENTITY_CLOSED.equals(e.getExceptionID())) {
            model.addAttribute(MODEL_ALERT_MESSAGE, "auth.ui.error.closed.message");
            model.addAttribute(MODEL_LINK_TO_OTHER_SERVICE, "");
        } else if (IDPortenExceptionID.IDENTITY_QUARANTINED_ONE_HOUR.equals(e.getExceptionID())) {
            model.addAttribute(MODEL_ALERT_MESSAGE, "auth.ui.error.locked.message");
            model.addAttribute(MODEL_LINK_TO_OTHER_SERVICE, "password");
        } else {
            model.addAttribute(MODEL_ALERT_MESSAGE, "auth.ui.error.quarantined.message");
            model.addAttribute(MODEL_LINK_TO_OTHER_SERVICE, "password");
        }
    }

    @PostMapping(value = {"/authorize", "/v2/authorize"}, params = {"otpCode"})
    public String postCode(HttpServletRequest request, HttpServletResponse response, @Valid @ModelAttribute(MODEL_ONE_TIME_CODE) UserInputtedCode userInputtedCode, BindingResult result, Model model) {
        try {
            int state = (int) request.getSession().getAttribute(HTTP_SESSION_STATE);
            String sid = (String) request.getSession().getAttribute(HTTP_SESSION_SID);
            ServiceProvider sp = (ServiceProvider) request.getSession().getAttribute(SERVICEPROVIDER);
            String code = userInputtedCode.getOtpCode();
            int numPinCodes = userInputtedCode.getPinCodeNumber();
            // Check cancel
            if (buttonIsPushed(request, MinIdPlusButtonType.CANCEL)) {
                return backToClient(request, model, MinIdState.STATE_CANCEL);
            }
            if (result.hasErrors()) {
                warn("There are contraint violations: " + Arrays.toString(result.getAllErrors().toArray()));
                InputTerminator.clearAllInput(userInputtedCode, result, model);
                return getNextView(request, STATE_LOGIN_VERIFICATION_CODE);
            }
            if (state == STATE_LOGIN_VERIFICATION_CODE) {
                if (authenticationService.authenticateOtpStep(sid, code, sp.getEntityId())) {
                    //if minidplus: backToClient,
                    if (isMinidPlus(request)) {
                        return backToClient(request, model, STATE_AUTHENTICATED);
                    } else {
                        return backToClientV2(request, model, STATE_AUTHENTICATED);
                    }
                } else {
                    result.addError(new ObjectError(MODEL_ONE_TIME_CODE, new String[]{"auth.ui.usererror.wrong.pincode"}, null, "Try again"));
                }
            } else if (state == STATE_LOGIN_PINCODE) {
                if (authenticationService.authenticatePinCodeStep(sid, code, numPinCodes, sp.getEntityId())) {
                    return backToClientV2(request, model, STATE_AUTHENTICATED);
                } else {
                    result.addError(new ObjectError(MODEL_ONE_TIME_CODE, new String[]{"auth.ui.usererror.wrong.pincode"}, null, "Try again"));
                }
            }
        } catch (MinIDTimeoutException e) {
            warn("User cache timed out " + e.getMessage());
            model.addAttribute(MODEL_ALERT_MESSAGE, "no.idporten.module.minidplus.timeout");
            model.addAttribute(MODEL_LINK_TO_OTHER_SERVICE, "");
            return getNextView(request, STATE_ALERT);
        } catch (MinIDPincodeException e) {
            warn("User pincode locked " + e.getMessage());
            model.addAttribute(MODEL_ALERT_MESSAGE, "auth.ui.error.quarantined.message");
            model.addAttribute(MODEL_LINK_TO_OTHER_SERVICE, "password");
            return getNextView(request, STATE_ALERT);
        } catch (Exception e) {
            warn("Exception handling otp: " + e.getMessage());
            result.addError(new ObjectError(MODEL_ONE_TIME_CODE, new String[]{"no.idporten.error.line1"}, null, "System error"));
            result.addError(new ObjectError(MODEL_ONE_TIME_CODE, new String[]{"no.idporten.error.line3"}, null, "Please try again"));
        }
        return getNextView(request, STATE_LOGIN_VERIFICATION_CODE);
    }

    /**
     * If acr_values = Level4 -> MinidPlus, if acr_values = Level3 -> MinIDEkstern
     */
    private boolean isMinidPlus(HttpServletRequest request) {
        AuthorizationRequest ar = (AuthorizationRequest) request.getSession().getAttribute(MinidPlusSessionAttributes.AUTHORIZATION_REQUEST);
        return ar.getAcrValues().equals(LevelOfAssurance.LEVEL4);
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
            return VIEW_LOGIN_ENTER_OTP;
        } else if (state == STATE_LOGIN_PINCODE) {
            return VIEW_LOGIN_ENTER_PINCODE;
        } else if (state == STATE_START_LOGIN) {
            return VIEW_START_LOGIN;
        } else if (state == STATE_AUTHENTICATED || state == MinIdState.STATE_CANCEL) {
            clearSessionAndCache(request);
            if(postCallBackMethod) {
                return VIEW_REDIRECT_TO_IDPORTEN;
            }else{
                return "redirect:"+buildUrl(request, state);
            }
        } else if (state == STATE_ALERT) {
            return VIEW_ALERT;
        } else if (state == STATE_LOGIN_WRONG_ACR) {
            clearSessionAndCache(request);
            return VIEW_ERROR_ACR;
        } else if (state == STATE_ERROR) {
            return VIEW_GENERIC_ERROR;
        }
        log.error("Illegal state " + state);
        return VIEW_GENERIC_ERROR;
    }

    private void clearSessionAndCache(HttpServletRequest request) {
        Object sid = request.getSession().getAttribute(HTTP_SESSION_SID);
        if (sid != null) {
            minidPlusCache.removeSSN((String) sid);
        }
    }

    private void setSessionState(HttpServletRequest request, int state) {
        request.getSession().setAttribute(HTTP_SESSION_STATE, state);
    }

    private boolean buttonIsPushed(HttpServletRequest request, MinIdPlusButtonType type) {
        return request.getParameter(type.id()) != null;
    }

    private String buildUrl(HttpServletRequest request, int state) {
        HttpSession session = request.getSession();
        String sid = (String) session.getAttribute("sid");
        AuthorizationRequest ar = (AuthorizationRequest) session.getAttribute(MinidPlusSessionAttributes.AUTHORIZATION_REQUEST);
        if (ar != null && StringUtils.isNotEmpty(ar.getRedirectUri())) {
            try {
                UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance()
                        .uri(new URI(ar.getRedirectUri()));


                uriComponentsBuilder.queryParam(HTTP_SESSION_REDIRECT_URI, ar.getRedirectUri())
                        .queryParam(HTTP_SESSION_LOCALE, ar.getLocale());

                if (StringUtils.isNotEmpty(ar.getState() )) {
                    uriComponentsBuilder.queryParam(HTTP_SESSION_CLIENT_STATE, ar.getState());
                }
                //start idporten specifics
                if (StringUtils.isNotEmpty(ar.getGotoParam())) {
                    uriComponentsBuilder.queryParam(HTTP_SESSION_GOTO, ar.getGotoParam());
                }
                if(StringUtils.isNotEmpty(serverid)) {
                    uriComponentsBuilder.queryParam(SERVERID, serverid);
                }
                if (state == MinIdState.STATE_CANCEL) {
                    uriComponentsBuilder.queryParam("error", ABORTED_BY_USER);
                    uriComponentsBuilder.queryParam(HTTP_SESSION_SERVICE, START_SERVICE);
                } else {
                    uriComponentsBuilder.queryParam(HTTP_SESSION_SERVICE, SERVICE_NAME);
                    uriComponentsBuilder.queryParam(CODE, sid);
                }
                //end idporten specifics

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

    private String backToClientV2(HttpServletRequest request, Model model, int backState) throws IOException {
        PushedAuthorizationRequest authorizationRequest = (PushedAuthorizationRequest) request.getSession().getAttribute("authorization_request");
        String sid = (String) request.getSession().getAttribute("sid");
        Authorization authorization = minidPlusCache.getAuthorization(sid);
        AuthorizationResponse authorizationResponse = openIDConnectIntegration.authorize(authorizationRequest, authorization);

        if (authorizationResponse.isQuery()) {
            return "redirect:" + authorizationResponse.toQueryRedirectUri();

        } else {
            model.addAttribute(MODEL_REDIRECT_URL, authorizationResponse.toQueryRedirectUri().toString());
            return getNextView(request, backState);

        }
    }

    private String backToClient(HttpServletRequest request, Model model, int backState) {
        model.addAttribute(MODEL_REDIRECT_URL, buildUrl(request, backState));
        return getNextView(request, backState);
    }

    private void warn(String message) {
        log.warn(CorrelationId.get() + " " + message);
    }
}
