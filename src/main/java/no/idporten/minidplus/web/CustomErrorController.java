package no.idporten.minidplus.web;

import lombok.extern.slf4j.Slf4j;
import no.difi.resilience.CorrelationId;
import no.idporten.minidplus.domain.MinidState;
import no.idporten.minidplus.domain.UserCredentials;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import static no.idporten.minidplus.domain.MinidPlusSessionAttributes.HTTP_SESSION_STATE;

@Controller
@Slf4j
public class CustomErrorController implements ErrorController {

    @GetMapping("/error")
    public ModelAndView handleError(HttpServletRequest request) {
        // get error status
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        ModelAndView errorPage = new ModelAndView("error");
        String errorMsg = "unkown";
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            // display specific error page
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                errorMsg = "404";
            } else if (statusCode == HttpStatus.BAD_REQUEST.value()) {
                errorMsg = "400...some bad request";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                errorMsg = "500";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                errorMsg = "403";
            }
        }
        if (request.getAttribute(RequestDispatcher.ERROR_EXCEPTION) != null) {
            Exception e = ((Exception) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION));
            log.warn(CorrelationId.get() + " Exception occurred " + errorMsg, e);
        }
        errorPage.addObject("errorMsg", errorMsg);

        return errorPage;
    }

    @PostMapping("/retry")
    public String handleRetry(HttpServletRequest request, Model model) {
        model.addAttribute(MinidPlusAuthorizeController.MODEL_USER_CREDENTIALS, new UserCredentials());
        request.getSession().setAttribute(HTTP_SESSION_STATE, MinidState.STATE_START_LOGIN);
        return "minidplus_enter_credentials";
    }

    @GetMapping("/testerror")
    public void handleRequest() {
        throw new RuntimeException("test exception");
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
