package no.idporten.minidplus.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.idporten.minidplus.util.MinIdPlusProperties;
import no.idporten.ui.impl.IDPortenButtonType;
import no.idporten.ui.impl.IDPortenInputType;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;

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
    private static final String PIN_CODE = "pinCode";
    private static final String PASSWORD = "password";
    private static final String EMAIL_ADDRESS = "emailAddress";
    private static final String IDPORTEN_INPUTBUTTON_PREFIX = "idporten.inputbutton.";

    private final LocaleResolver localeResolver;

    @Value("${idporten.redirecturl}")
    private String redirectUrl;

    @Autowired
    private ApplicationContext context;

    @GetMapping
    public ModelAndView doGet(HttpServletRequest request, HttpServletResponse response) {
        request.getSession().invalidate();
        setSessionState(request, STATE_USERDATA);
        //request.getSession().setAttribute("locale", request.getParameter("locale"));
        return new ModelAndView("minidplus_enter_credentials");
    }

    @PostMapping
    public ModelAndView doPost(HttpServletRequest request,
                               HttpServletResponse response) throws URISyntaxException, IOException {
        try {
            int state = (int) request.getSession().getAttribute(MinIdPlusProperties.HTTP_SESSION_STATE);
            if (state == STATE_USERDATA) {
                return getNextView(request, response, handleUserdataInput(request));
            }else if(state == STATE_VERIFICATION_CODE){
                return getNextView(request, response, handleOtpInput(request));
            }
        } catch (Exception e) {
            //todo
            prepareErrorPage("501", request);
        }
        return getNextView(request, response, STATE_ERROR);
    }

    private int handleOtpInput(HttpServletRequest request) {
        return STATE_AUTHENTICATED; //todo
    }

    private int handleUserdataInput(HttpServletRequest request) {
        //eksempel
        String message = context.getMessage("no.idporten.module.minid.step2.otc.info", null, request.getLocale());
        return STATE_VERIFICATION_CODE; //todo
    }


    private int prepareErrorPage(String errorCode, HttpServletRequest request) {
        if (StringUtils.isNotEmpty(errorCode)) {
            request.setAttribute("errorCode", errorCode);
        }
        return STATE_ERROR;
    }

    private ModelAndView getNextView(HttpServletRequest request, HttpServletResponse response, int state) throws IOException {
        setSessionState(request, state);
        if (state == STATE_VERIFICATION_CODE) {
            return new ModelAndView("minidplus_enter_otp");
        } else if (state == STATE_USERDATA) {
            return new ModelAndView("minidplus_enter_credentials");
        } else if (state == STATE_ERROR) {
            return new ModelAndView("error");
        } else if (state == STATE_AUTHENTICATED ) {
            return new ModelAndView("success");
          /*  log.debug("RedirectUrl: " + request.getSession().getAttribute("redirectUrl"));
            response.sendRedirect((String) request.getSession().getAttribute("redirectUrl"));*/
        } else {
            return new ModelAndView("error");
        }
    }
    private void setFeedback(HttpServletRequest request, Enum feedbackType, String messageId) {
        request.getSession().setAttribute(IDPORTEN_FEEDBACK_PREFIX + feedbackType.toString(), messageId);
    }

    private void setInput(HttpServletRequest request, Enum inputType, String messageId) {
        request.getSession().setAttribute(IDPORTEN_INPUT_PREFIX + inputType.toString(), messageId);
    }

    private void clearInput(HttpServletRequest request, Enum feedbackType) {
        request.getSession().setAttribute(IDPORTEN_INPUT_PREFIX + feedbackType.toString(), null);
    }

    private String getInput(HttpServletRequest request, IDPortenInputType inputType) {
        return request.getParameter(IDPORTEN_INPUT_PREFIX + inputType.toString());
    }

    private boolean isButtonPushed(HttpServletRequest request, IDPortenButtonType buttonType) {
        return request.getParameter(IDPORTEN_INPUTBUTTON_PREFIX + buttonType.toString()) != null;
    }

    private void setSessionState(HttpServletRequest request, int state) {
        request.getSession().setAttribute(MinIdPlusProperties.HTTP_SESSION_STATE, state);
    }
}
