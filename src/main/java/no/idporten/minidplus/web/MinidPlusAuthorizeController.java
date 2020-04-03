package no.idporten.minidplus.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.idporten.minidplus.util.MinIdPlusProperties;
import no.idporten.ui.impl.IDPortenButtonType;
import no.idporten.ui.impl.IDPortenInputType;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
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
    private final String IDPORTEN_INPUT_PREFIX = "idporten.input.";
    private final String IDPORTEN_FEEDBACK_PREFIX = "idporten.feedback.";
    private final String MOBILE_NUMBER = "mobileNumber";
    private final String SOCIAL_SECURITY_NUMBER = "socialSecurityNumber";
    private final String EMAIL_ADDRESS = "emailAddress";
    private final String IDPORTEN_INPUTBUTTON_PREFIX = "idporten.inputbutton.";

    private final LocaleResolver localeResolver;

    @Value("${idporten.redirecturl}")
    private String redirectUrl;

    @GetMapping
    public ModelAndView doGet(HttpServletRequest request, HttpServletResponse response) {
        request.getSession().invalidate();
        //request.getSession().setAttribute("locale", request.getParameter("locale"));
        return new ModelAndView("minidplus_enter_credentials");
    }

    @PostMapping
    public ModelAndView doPost(HttpServletRequest request,
                               HttpServletResponse response) throws URISyntaxException, IOException {
        try {
            int state = (int) request.getSession().getAttribute(MinIdPlusProperties.HTTP_SESSION_STATE);
        } finally {
            //all roads lead to error
            prepareErrorPage("501", request);
            return new ModelAndView("error");
        }
    }


    private int prepareErrorPage(String errorCode, HttpServletRequest request) {
        if (StringUtils.isNotEmpty(errorCode)) {
            request.setAttribute("errorCode", errorCode);
        }
        return STATE_ERROR;
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
