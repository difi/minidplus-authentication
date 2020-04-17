package no.idporten.minidplus.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.idporten.domain.auth.AuthType;
import no.idporten.minidplus.domain.AuthorizationRequest;
import no.idporten.minidplus.domain.MinidPlusSessionAttributes;
import no.idporten.minidplus.domain.OneTimePassword;
import no.idporten.minidplus.domain.UserCredentials;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

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

    @Value("${idporten.redirecturl}")
    private String redirectUrl;

    @GetMapping(produces = "text/html; charset=utf-8")
    public String doGet(HttpServletRequest request, HttpServletResponse response, /*@Valid*/ AuthorizationRequest authorizationRequest, Model model) throws IOException {
        request.getSession().invalidate();
        request.getSession().setAttribute(MinidPlusSessionAttributes.HTTP_SESSION_AUTH_TYPE, AuthType.MINID_PLUS);
        request.getSession().setAttribute(MinidPlusSessionAttributes.HTTP_SESSION_SID, UUID.randomUUID().toString());

        authorizationRequest.setStartService(request.getParameter(MinidPlusSessionAttributes.HTTP_SESSION_START_SERVICE)); //tmp workaround for stupid dash-param
        setLocale(request, response, authorizationRequest);
        request.getSession().setAttribute("AUTHORIZATION_REQUEST", authorizationRequest);

        UserCredentials userCredentials = new UserCredentials();
        model.addAttribute("userCredentials", userCredentials);
        return getNextView(request, STATE_USERDATA);
    }

    private void setLocale(HttpServletRequest request, HttpServletResponse response, AuthorizationRequest authorizationRequest) {
        String locale = authorizationRequest.getLocale();
        if(StringUtils.isEmpty(locale)){
            locale = request.getLocale().toString();
        }
        localeResolver.setLocale(request, response, new Locale(locale));
        request.getSession().setAttribute("locale", locale);
    }

    @PostMapping
    public String postUserCredentials(HttpServletRequest request, @Valid @ModelAttribute("userCredentials") UserCredentials userCredentials, BindingResult result, Model model) throws IOException {
        //eksempel
        int state = (int) request.getSession().getAttribute(MinidPlusSessionAttributes.HTTP_SESSION_STATE);
        if (state == STATE_USERDATA) {
            if (result.hasErrors()) {
                return getNextView(request, STATE_USERDATA);
            }
            if (handleUserdataInput(request, userCredentials) == STATE_ERROR) {
                ObjectError objectError = new ObjectError("*", "Login failed"); //todo internationalized message
                result.addError(objectError);
                return getNextView(request, STATE_USERDATA);
            }
            OneTimePassword oneTimePassword = new OneTimePassword();
            model.addAttribute(oneTimePassword);
            return getNextView(request, STATE_VERIFICATION_CODE);
        } else {
            return getNextView(request, STATE_ERROR);
        }
    }

    @PostMapping(params = "otpCode")
    public String postOTP(HttpServletRequest request, @Valid @ModelAttribute("oneTimePassword") OneTimePassword oneTimePassword, BindingResult result ) throws IOException {
        try {
            int state = (int) request.getSession().getAttribute(MinidPlusSessionAttributes.HTTP_SESSION_STATE);
            if (state == STATE_VERIFICATION_CODE) {
                if (result.hasErrors()) {
                    return getNextView(request, STATE_VERIFICATION_CODE);
                }
                return getNextView(request, handleOtpInput(oneTimePassword.getOtpCode()));
            }
        } catch (Exception e) {
            //todo
            prepareErrorPage("501", request);
        }
        return getNextView(request, STATE_ERROR);
    }

    private int handleOtpInput(String otp) {
        return STATE_AUTHENTICATED; //todo
    }

    private int handleUserdataInput(HttpServletRequest request, UserCredentials userCredentials) {

        if (true) {
            return STATE_VERIFICATION_CODE; //todo
        } else {
            request.setAttribute("errorCode", "loginFailed");
            return STATE_ERROR;
        }

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
            return "success";
          /*  log.debug("RedirectUrl: " + request.getSession().getAttribute("redirectUrl"));
            return "redirect:"+redirectUrl;*/
        }

        return "error";

    }

    private void setSessionState(HttpServletRequest request, int state) {
        request.getSession().setAttribute(MinidPlusSessionAttributes.HTTP_SESSION_STATE, state);
    }
}
