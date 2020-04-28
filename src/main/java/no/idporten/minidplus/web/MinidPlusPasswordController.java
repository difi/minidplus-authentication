package no.idporten.minidplus.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.resilience.CorrelationId;
import no.idporten.domain.sp.ServiceProvider;
import no.idporten.minidplus.domain.AuthorizationRequest;
import no.idporten.minidplus.domain.OneTimePassword;
import no.idporten.minidplus.domain.PersonIdInput;
import no.idporten.minidplus.exception.minid.MinIDUserNotFoundException;
import no.idporten.minidplus.service.AuthenticationService;
import org.apache.commons.lang.StringUtils;
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
import java.util.Locale;
import java.util.UUID;

import static no.idporten.minidplus.domain.MinidPlusSessionAttributes.*;

/**
 * Handles password change
 */
@Controller
@RequestMapping(value = "/password")
@Slf4j
@RequiredArgsConstructor
public class MinidPlusPasswordController {
    protected static final int STATE_PASSWORD_CHANGED = -1;
    protected static final int STATE_PERSONID = 1;
    protected static final int STATE_OTC = 2;
    protected static final int STATE_EMAIL = 3;
    protected static final int STATE_NEW_PASSWORD = 4;
    protected static final int STATE_ERROR = 10;


    public static final String MODEL_USER_PERSONID = "personIdInput";

    private final LocaleResolver localeResolver;

    private final AuthenticationService authenticationService;


    @GetMapping(produces = "text/html; charset=utf-8")
    public String doGet(HttpServletRequest request, HttpServletResponse response, Model model) {
        request.getSession().setAttribute(HTTP_SESSION_SID, UUID.randomUUID().toString());
        setLocale(request, response, request.getParameter(HTTP_SESSION_LOCALE));
        PersonIdInput personIdInput = new PersonIdInput();
        model.addAttribute(MODEL_USER_PERSONID, personIdInput);
        return getNextView(request, STATE_PERSONID);
    }

    private void setLocale(HttpServletRequest request, HttpServletResponse response, String locale) {
        if (StringUtils.isEmpty(locale)) {
            locale = request.getLocale().toString();
        }
        localeResolver.setLocale(request, response, new Locale(locale));
        request.getSession().setAttribute("locale", locale);
    }

    @PostMapping
    public String postPersonId(HttpServletRequest request, @Valid @ModelAttribute(MODEL_USER_PERSONID) PersonIdInput personId, BindingResult result, Model model) {

        int state = (int) request.getSession().getAttribute(HTTP_SESSION_STATE);
        String sid = (String) request.getSession().getAttribute(HTTP_SESSION_SID);
        AuthorizationRequest ar = (AuthorizationRequest) request.getSession().getAttribute(AUTHORIZATION_REQUEST);

        if (state == STATE_PERSONID) {
            if (result.hasErrors()) {
                return getNextView(request, STATE_PERSONID);
            }
            try {
                ServiceProvider sp = new ServiceProvider("Idporten");
                sp.setName("idporten");
                authenticationService.authenticatePid(sid, personId.getPersonalIdNumber(), sp);
            } catch (MinIDUserNotFoundException e) {
                result.addError(new ObjectError(MODEL_USER_PERSONID, new String[]{"auth.ui.usererror.format.ssn"}, null, "Login failed"));
                return getNextView(request, STATE_PERSONID);
            }

        } else {
            result.addError(new ObjectError(MODEL_USER_PERSONID, new String[]{"no.idporten.error.line1"}, null, "System error"));
            result.addError(new ObjectError(MODEL_USER_PERSONID, new String[]{"no.idporten.error.line3"}, null, "Please try again"));
            return getNextView(request, STATE_PERSONID);
        }
        OneTimePassword oneTimePassword = new OneTimePassword();
        model.addAttribute(oneTimePassword);
        return getNextView(request, STATE_OTC);

    }

    private String getNextView(HttpServletRequest request, int state) {
        setSessionState(request, state);
        if (state == STATE_PERSONID) {
            return "minidplus_password_personid";
        } else if (state == STATE_OTC)
            return "success"; //todo
        return "error";
    }

    private void setSessionState(HttpServletRequest request, int state) {
        request.getSession().setAttribute(HTTP_SESSION_STATE, state);
    }

    private void warn(String message) {
        log.warn(CorrelationId.get() + " " + message);
    }
}
