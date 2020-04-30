package no.idporten.minidplus.spring;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.web.servlet.mvc.method.annotation.ExtendedServletRequestDataBinder;

import javax.servlet.ServletRequest;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * ServletRequestDataBinder which supports fields renaming using {@link ParamName}
 *
 * @author jkee
 */
public class ParamNameDataBinder extends ExtendedServletRequestDataBinder {

    private final Map<String, Field> renameMapping;

    public ParamNameDataBinder(Object target, String objectName, Map<String, Field> renameMapping) {
        super(target, objectName);
        this.renameMapping = renameMapping;
    }

    @Override
    protected void addBindValues(MutablePropertyValues mpvs, ServletRequest request) {
        super.addBindValues(mpvs, request);
        for (Map.Entry<String, Field> entry : renameMapping.entrySet()) {
            String from = entry.getKey();
            String to = entry.getValue().getName();
            if (mpvs.contains(from) && !from.equals(to)) {
                mpvs.addPropertyValue(to, mpvs.getPropertyValue(from).getValue());
                mpvs.removePropertyValue(from);
            }
        }
    }
}
