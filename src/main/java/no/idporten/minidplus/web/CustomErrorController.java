package no.idporten.minidplus.web;

import lombok.extern.slf4j.Slf4j;
import no.difi.resilience.CorrelationId;
import no.idporten.minidplus.domain.AuthorizationRequest;
import no.idporten.minidplus.domain.MinidPlusSessionAttributes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
@Slf4j
public class CustomErrorController implements ErrorController {

    @Value("${minid-plus.context-path}")
    public String contextPath = "";

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

    @PostMapping("/error")
    public String handleRetry(HttpServletRequest request, Model model) {
        if (request.getSession().getAttribute("retry") == null) {
            request.getSession().setAttribute("retry", true);
            return "redirect:" + contextPath + "/authorize";
        } else {
            if (request.getSession().getAttribute(MinidPlusSessionAttributes.AUTHORIZATION_REQUEST) != null) {
                AuthorizationRequest ar = (AuthorizationRequest) request.getSession().getAttribute(MinidPlusSessionAttributes.AUTHORIZATION_REQUEST);
                request.getSession().removeAttribute("retry");
                return "redirect:" + ar.getGotoParam();
            } else {
                model.addAttribute("dontRetry", true);
                return "error";
            }
        }
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
