package no.idporten.minidplus.exception;

import lombok.extern.slf4j.Slf4j;
import no.difi.resilience.CorrelationId;
import no.idporten.minidplus.domain.MinidPlusSessionAttributes;
import no.idporten.minidplus.util.MinIdPlusViews;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@Slf4j
public class GlobalExceptionController {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ModelAndView handleError405(HttpServletRequest request, Exception e) {
        log.warn("Request method not supported for: " + request.getRequestURL() + " raised " + e.getMessage(), e);
        String msg = "405";
        if (request.getSession().getAttribute("_csrf") == null) {
            log.warn("CSRF token emtpy " + CorrelationId.get());
            log.warn("Authorization request empty: " + (request.getSession().getAttribute(MinidPlusSessionAttributes.AUTHORIZATION_REQUEST) == null));
            if (request.getSession().getAttribute(MinidPlusSessionAttributes.AUTHORIZATION_REQUEST) == null) {
                log.warn("Authorization request also empty. Session timeout or invalid session " + CorrelationId.get());
            }
            msg = "session timeout";
        }


        ModelAndView mav = new ModelAndView(MinIdPlusViews.VIEW_GENERIC_ERROR);
        mav.addObject(MinIdPlusViews.MODEL_ERROR_MSG, msg);
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleError(HttpServletRequest request, Exception e) {
        log.error("Request: " + request.getRequestURL() + " raised " + e.getMessage(), e);
        return new ModelAndView(MinIdPlusViews.VIEW_GENERIC_ERROR);
    }
}