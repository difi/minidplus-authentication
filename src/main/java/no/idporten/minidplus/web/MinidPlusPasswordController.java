package no.idporten.minidplus.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.resilience.CorrelationId;
import no.idporten.domain.sp.ServiceProvider;
import no.idporten.minidplus.domain.OneTimePassword;
import no.idporten.minidplus.domain.PasswordChange;
import no.idporten.minidplus.domain.PersonIdInput;
import no.idporten.minidplus.exception.minid.MinIDPincodeException;
import no.idporten.minidplus.service.AuthenticationService;
import no.idporten.minidplus.service.OTCPasswordService;
import no.idporten.minidplus.validator.InputTerminator;
import no.idporten.ui.impl.MinidPlusButtonType;
import no.minid.exception.MinidUserInvalidException;
import no.minid.exception.MinidUserNotFoundException;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    protected static final int STATE_PASSWORD_CHANGED = -101;
    protected static final int STATE_PERSONID = 101;
    protected static final int STATE_VERIFICATION_CODE_SMS = 102;
    protected static final int STATE_VERIFICATION_CODE_EMAIL = 103;
    protected static final int STATE_NEW_PASSWORD = 104;
    protected static final int STATE_CONTINUE = 108;
    protected static final int STATE_CANCEL = 109;
    protected static final int STATE_ERROR = 1010;

    private static final String ABORTED_BY_USER = "aborted_by_user";

    public static final String MODEL_USER_PERSONID = "personIdInput";
    public static final String MODEL_ONE_TIME_CODE = "oneTimePassword";
    public static final String MODEL_PASSWORDCHANGE = "passwordChange";

    private final LocaleResolver localeResolver;

    private final AuthenticationService authenticationService;

    private final OTCPasswordService otcPasswordService;

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

    @PostMapping(params = "personalIdNumber")
    public String postPersonId(HttpServletRequest request, @Valid @ModelAttribute(MODEL_USER_PERSONID) PersonIdInput personId, BindingResult result, Model model, RedirectAttributes redirectAttributes) {

        int state = (int) request.getSession().getAttribute(HTTP_SESSION_STATE);
        String sid = (String) request.getSession().getAttribute(HTTP_SESSION_SID);
        String pid = personId.getPersonalIdNumber();
        personId.setPersonalIdNumber("");
        // Check cancel
        if (buttonIsPushed(request, MinidPlusButtonType.CANCEL)) {
            return getNextView(request, STATE_CANCEL);
        }
        if (state == STATE_PERSONID) {
            if (result.hasErrors()) {
                InputTerminator.clearAllInput(personId, result, model);
                return getNextView(request, STATE_PERSONID);
            }
            try {
                ServiceProvider sp = new ServiceProvider("Idporten");
                sp.setName("idporten");
                authenticationService.authenticatePid(sid, pid, sp);
                model.addAttribute(new OneTimePassword());
                return getNextView(request, STATE_VERIFICATION_CODE_SMS);

            } catch (MinidUserNotFoundException e) {
                result.addError(new ObjectError(MODEL_USER_PERSONID, new String[]{"auth.ui.usererror.format.ssn"}, null, "Login failed"));
                return getNextView(request, STATE_PERSONID);
            } catch (MinidUserInvalidException e) {
                warn("Users exception handling otp. : " + e.getMessage());
                result.addError(new ObjectError(MODEL_ONE_TIME_CODE, new String[]{"auth.ui.usererror.format.missing.mobile"}, null, "Mobile number not registered on your user"));
            } catch (Exception e) {
                warn("Unexpected exception occurred " + e.getMessage());
                result.addError(new ObjectError(MODEL_USER_PERSONID, new String[]{"no.idporten.error.line1"}, null, "Login failed"));
            }
            return getNextView(request, STATE_PERSONID);
        } else {
            log.error("invalid state : " + state);
            result.addError(new ObjectError(MODEL_USER_PERSONID, new String[]{"no.idporten.error.line1"}, null, "System error"));
            result.addError(new ObjectError(MODEL_USER_PERSONID, new String[]{"no.idporten.error.line3"}, null, "Please try again"));
            return getNextView(request, STATE_ERROR);
        }

    }

    @PostMapping(params = "otpType=sms")
    public String postOTP(HttpServletRequest request, @Valid @ModelAttribute(MODEL_ONE_TIME_CODE) OneTimePassword oneTimePassword, BindingResult result, Model model) {
        try {
            int state = (int) request.getSession().getAttribute(HTTP_SESSION_STATE);
            String sid = (String) request.getSession().getAttribute(HTTP_SESSION_SID);
            String otp = oneTimePassword.getOtpCode();
            oneTimePassword.setOtpCode("");
            // Check cancel
            if (buttonIsPushed(request, MinidPlusButtonType.CANCEL)) {
                return getNextView(request, STATE_CANCEL);
            }

            if (result.hasErrors()) {
                InputTerminator.clearAllInput(oneTimePassword, result, model);
                return getNextView(request, STATE_VERIFICATION_CODE_SMS);
            }
            if (state == STATE_VERIFICATION_CODE_SMS) {
                if (otcPasswordService.checkOTCCode(sid, otp)) {
                    oneTimePassword = new OneTimePassword();
                    model.addAttribute(oneTimePassword);
                    authenticationService.verifyUserByEmail(sid);
                    return getNextView(request, STATE_VERIFICATION_CODE_EMAIL);
                } else {
                    warn("Wrong pincode ");
                    result.addError(new ObjectError(MODEL_ONE_TIME_CODE, new String[]{"auth.ui.usererror.wrong.pincode"}, null, "Try again"));
                }
            } else {
                warn("Illegal state in postOTP sms " + state);
                result.addError(new ObjectError(MODEL_ONE_TIME_CODE, new String[]{"no.idporten.error.line1"}, null, "System error"));
            }
        } catch (MinIDPincodeException e) {
            result.addError(new ObjectError(MODEL_ONE_TIME_CODE, new String[]{"auth.ui.usererror.format.otc.locked"}, null, "Too many attempts"));
            warn("Pincode locked " + e.getMessage());
        } catch (MinidUserInvalidException e) {
            warn("Exception handling otp. : " + e.getMessage());
            result.addError(new ObjectError(MODEL_ONE_TIME_CODE, new String[]{"auth.ui.usererror.format.missing.email"}, null, "Missing email"));
        } catch (Exception e) {
            warn("Exception handling otp: " + e.getMessage());
            result.addError(new ObjectError(MODEL_ONE_TIME_CODE, new String[]{"no.idporten.error.line1"}, null, "System error"));
            result.addError(new ObjectError(MODEL_ONE_TIME_CODE, new String[]{"no.idporten.error.line3"}, null, "Please try again"));
        }
        return getNextView(request, STATE_VERIFICATION_CODE_SMS);
    }

    @PostMapping(params = "otpType=email")
    public String postOTPEmail(HttpServletRequest request, @Valid @ModelAttribute(MODEL_ONE_TIME_CODE) OneTimePassword oneTimePassword, BindingResult result, Model model) {
        try {
            int state = (int) request.getSession().getAttribute(HTTP_SESSION_STATE);
            String sid = (String) request.getSession().getAttribute(HTTP_SESSION_SID);
            String otp = oneTimePassword.getOtpCode();
            oneTimePassword.setOtpCode("");
            // Check cancel
            if (buttonIsPushed(request, MinidPlusButtonType.CANCEL)) {
                return getNextView(request, STATE_CANCEL);
            }
            if (result.hasErrors()) {
                InputTerminator.clearAllInput(oneTimePassword, result, model);
                return getNextView(request, STATE_VERIFICATION_CODE_EMAIL);
            }
            if (state == STATE_VERIFICATION_CODE_EMAIL) {
                if (otcPasswordService.checkOTCCode(sid, otp)) {
                    model.addAttribute(MODEL_PASSWORDCHANGE, new PasswordChange());
                    return getNextView(request, STATE_NEW_PASSWORD);
                } else {
                    result.addError(new ObjectError(MODEL_ONE_TIME_CODE, new String[]{"auth.ui.usererror.wrong.pincode"}, null, "Try again"));
                }
            } else {
                warn("Illegal state in postOTPEmail" + state);
                result.addError(new ObjectError(MODEL_ONE_TIME_CODE, new String[]{"no.idporten.error.line1"}, null, "System error"));
            }
        } catch (MinIDPincodeException e) {
            warn("Pincode locked " + e.getMessage());
            result.addError(new ObjectError(MODEL_ONE_TIME_CODE, new String[]{"auth.ui.usererror.format.otc.locked"}, null, "Too many attempts"));
        } catch (Exception e) {
            warn("Exception handling otp: " + e.getMessage());
            result.addError(new ObjectError(MODEL_ONE_TIME_CODE, new String[]{"no.idporten.error.line1"}, null, "System error"));
            result.addError(new ObjectError(MODEL_ONE_TIME_CODE, new String[]{"no.idporten.error.line3"}, null, "Please try again"));
        }
        return getNextView(request, STATE_VERIFICATION_CODE_EMAIL);
    }

    @PostMapping(params = "newPassword")
    public String postPasswordChange(HttpServletRequest request, @Valid @ModelAttribute(MODEL_PASSWORDCHANGE) PasswordChange newPassword, BindingResult result, Model model) {
        try {
            int state = (int) request.getSession().getAttribute(HTTP_SESSION_STATE);
            String sid = (String) request.getSession().getAttribute(HTTP_SESSION_SID);
            String newPwd = newPassword.getNewPassword();
            String reenter = newPassword.getReenterPassword();
            newPassword.setNewPassword("");
            newPassword.setReenterPassword("");

            // Check cancel
            if (buttonIsPushed(request, MinidPlusButtonType.CANCEL)) {
                return getNextView(request, STATE_CANCEL);
            }
            if (result.hasErrors()) {
                InputTerminator.clearAllInput(newPassword, result, model);
                return getNextView(request, STATE_NEW_PASSWORD);
            }
            if (state == STATE_NEW_PASSWORD) {
                if (newPwd.equals(reenter)) { //extra backend check
                    if (authenticationService.changePassword(sid, newPwd)) {
                        return getNextView(request, STATE_PASSWORD_CHANGED);
                    }
                } else {
                    result.addError(new ObjectError(MODEL_PASSWORDCHANGE, new String[]{"auth.ui.usererror.invalidrepeat.newpassword"}, null, "Try again"));
                }
            }
        } catch (Exception e) {
            warn("Exception handling password change: " + e.getMessage());
            result.addError(new ObjectError(MODEL_PASSWORDCHANGE, new String[]{"no.idporten.forgottenpassword.failed"}, null, "System error"));
        }
        return getNextView(request, STATE_NEW_PASSWORD);
    }

    @PostMapping(params = "minidplus.inputbutton.CONTINUE")
    public String postPasswordChange(HttpServletRequest request) {
        int state = (int) request.getSession().getAttribute(HTTP_SESSION_STATE);
        if (state == STATE_PASSWORD_CHANGED) {
            return getNextView(request, STATE_CONTINUE);
        } else {
            warn("Illegal state " + state);
            return getNextView(request, STATE_ERROR);
        }
    }


    private String getNextView(HttpServletRequest request, int state) {
        setSessionState(request, state);
        if (state == STATE_PERSONID) {
            return "minidplus_password_personid";
        } else if (state == STATE_VERIFICATION_CODE_SMS) {
            return "minidplus_password_otp_sms";
        } else if (state == STATE_VERIFICATION_CODE_EMAIL) {
            return "minidplus_password_otp_email";
        } else if (state == STATE_NEW_PASSWORD) {
            return "minidplus_password_change";
        } else if (state == STATE_PASSWORD_CHANGED) {
            return "minidplus_password_success";
        } else if (state == STATE_CONTINUE || state == STATE_CANCEL) {
            return "redirect:/authorize";
        }
        return "error";
    }

    private void setSessionState(HttpServletRequest request, int state) {
        request.getSession().setAttribute(HTTP_SESSION_STATE, state);
    }

    private boolean buttonIsPushed(HttpServletRequest request, MinidPlusButtonType type) {
        return request.getParameter(type.id()) != null;
    }

    private void warn(String message) {
        log.warn(CorrelationId.get() + " " + message);
    }
}
