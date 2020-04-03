package no.idporten.minidplus.util;

import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * Contains constants for names in the MinidPlus module properties-file.
 */
@Component
@Getter
public class MinIdPlusProperties {
        public static final String MINIDPLUS_LOCALE_ENGLISH = "English";
        public static final String MINIDPLUS_LOCALE_NORWEGIAN = "Norwegian";
        public static final String HTTP_SESSION_AUTH_TYPE = "session.authenticationType";
        public static final String HTTP_SESSION_CLIENT_TYPE = "session.clientType";
        public static final String HTTP_SESSION_STATE = "session.state";


        public String getLocale(HttpServletRequest request) {
                String locale = LanguageUtils.getLanguage(request);
                if (LanguageUtils.LANGUAGE_EN.equals(locale)){
                        return MINIDPLUS_LOCALE_ENGLISH;
                }else {
                        return MINIDPLUS_LOCALE_NORWEGIAN;
                }

        }
}