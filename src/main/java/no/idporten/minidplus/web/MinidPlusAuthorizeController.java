package no.idporten.minidplus.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.idporten.minidplus.domain.OneTimePassword;
import no.idporten.minidplus.domain.UserCredentials;
import no.idporten.minidplus.util.MinIdPlusProperties;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
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

    private static final String SERVICE_PARAMETER_NAME = "service";
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

    @Autowired
    private ApplicationContext context;

    @GetMapping
    public String doGet(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
        request.getSession().invalidate();
        String sid = UUID.randomUUID().toString();
        request.getSession().setAttribute("sid", sid);
        request.getSession().setAttribute("locale", request.getParameter("locale"));
        UserCredentials userCredentials = new UserCredentials();
        model.addAttribute("userCredentials", userCredentials);
        return getNextView(request, STATE_USERDATA);
    }

    @PostMapping
    public String postUserCredentials(HttpServletRequest request, @Valid @ModelAttribute("userCredentials") UserCredentials userCredentials, BindingResult result, Model model) throws IOException {
        //eksempel
        int state = (int) request.getSession().getAttribute(MinIdPlusProperties.HTTP_SESSION_STATE);
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
            int state = (int) request.getSession().getAttribute(MinIdPlusProperties.HTTP_SESSION_STATE);
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
        request.getSession().setAttribute(MinIdPlusProperties.HTTP_SESSION_STATE, state);
    }
}
