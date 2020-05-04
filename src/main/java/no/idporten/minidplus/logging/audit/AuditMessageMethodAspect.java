package no.idporten.minidplus.logging.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import no.difi.resilience.CorrelationId;
import no.idporten.log.audit.AuditLogger;
import no.idporten.minidplus.domain.MinidPlusSessionAttributes;
import no.idporten.minidplus.service.MinidPlusCache;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditMessageMethodAspect {

    private final AuditLogger auditLogger;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<HttpServletRequest> requestObjectProvider;
    private final MinidPlusCache minidPlusCache;

    @Around("@annotation(AuditMessage)")
    public Object auditLog(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        Object body = pjp.proceed();
        AuditMessage auditMessage = method.getAnnotation(AuditMessage.class);
        HttpServletRequest request = requestObjectProvider.getObject();
        String systemId = request.getHeader("System-Id");
        String sid = (String) request.getSession().getAttribute(MinidPlusSessionAttributes.HTTP_SESSION_SID);
        String ssn = null;
        if (sid != null)
            ssn = minidPlusCache.getSSN(sid);
        Object logValue = body;
        if (logValue instanceof ResponseEntity) {
            logValue = ((ResponseEntity) logValue).getBody();
            if (!((ResponseEntity) body).getStatusCode().is2xxSuccessful()) {
                return body;
            }
        }
        List<String> resourceId = new ArrayList<>();
        for (int i = 0; i < method.getParameters().length; i++) {
            if (method.getParameters()[i].isAnnotationPresent(AuditResourceId.class)) {
                Object arg = pjp.getArgs()[i];
                if (arg instanceof String) {
                    resourceId.add((String) arg);
                } else {
                    resourceId.add(arg.toString());
                }
            }
        }
        auditLogger.log(
                auditMessage.value().auditId(),
                null,
                systemId,
                CorrelationId.get(),
                ssn,
                StringUtils.collectionToDelimitedString(resourceId, "::"),
                Optional.ofNullable(logValue).map(value -> {
                    try {
                        return objectMapper.writeValueAsString(value);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }).orElse(null)
        );

        return body;
    }
}
