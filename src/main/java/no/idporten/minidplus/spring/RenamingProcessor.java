package no.idporten.minidplus.spring;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Method processor supports {@link ParamName} parameters renaming
 *
 * @author jkee
 */

public class RenamingProcessor extends ServletModelAttributeMethodProcessor {

    //Rename cache
    private final Map<Class<?>, Map<String, Field>> replaceMap = new ConcurrentHashMap<>();
    @Autowired
    private ObjectProvider<RequestMappingHandlerAdapter> requestMappingHandlerAdapter;

    public RenamingProcessor(boolean annotationNotRequired) {
        super(annotationNotRequired);
    }

    private static Map<String, Field> analyzeClass(Class<?> targetClass) {
        Field[] fields = targetClass.getDeclaredFields();
        Map<String, Field> renameMap = new HashMap<>();
        for (Field field : fields) {
            ParamName paramNameAnnotation = field.getAnnotation(ParamName.class);
            if (paramNameAnnotation != null && !paramNameAnnotation.value().isEmpty()) {
                renameMap.put(paramNameAnnotation.value(), field);
            }
        }
        if (renameMap.isEmpty()) return Collections.emptyMap();
        return renameMap;
    }

    @Override
    protected void bindRequestParameters(WebDataBinder binder, NativeWebRequest nativeWebRequest) {
        Object target = binder.getTarget();
        Class<?> targetClass = target.getClass();
        if (!replaceMap.containsKey(targetClass)) {
            Map<String, Field> mapping = analyzeClass(targetClass);
            replaceMap.put(targetClass, mapping);
        }
        Map<String, Field> mapping = replaceMap.get(targetClass);
        ParamNameDataBinder paramNameDataBinder = new ParamNameDataBinder(target, binder.getObjectName(), mapping);
        requestMappingHandlerAdapter.getIfAvailable().getWebBindingInitializer().initBinder(paramNameDataBinder);
        super.bindRequestParameters(paramNameDataBinder, nativeWebRequest);
        for (ObjectError error : paramNameDataBinder.getBindingResult().getAllErrors()) {
            binder.getBindingResult().addError(error);
        }
    }
}