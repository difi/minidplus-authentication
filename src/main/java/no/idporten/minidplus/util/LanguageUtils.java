package no.idporten.minidplus.util;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * Utility methods related to language.
 */
public final class LanguageUtils {

    public static final String LOCALE = "locale";
    public static final String IDPORTEN_LOCALE = "IDPortenLocale";
    public static final String LANGUAGE_SE = "se";
    public static final String LANGUAGE_EN = "en";
    public static final String LANGUAGE_NN = "nn";
    public static final String LANGUAGE_NB = "nb";

    private LanguageUtils() {
    }

    /**
     * Get the language based on locale, default is "nb".
     *
     * @param request the the request.
     * @return the language, "nb" if locale is null or not recognized
     */
    public static String getLanguage(final ServletRequest request) {
        String localestr = request.getParameter(LOCALE);
        Locale locale;
        try {
            final HttpServletRequest httpsr = (HttpServletRequest) request;
            if ((localestr == null) || (localestr.isEmpty())) {
                localestr = (String) httpsr.getSession().getAttribute(IDPORTEN_LOCALE);
            }
            locale = new Locale(localestr);
        } catch (Exception e) {
            locale = new Locale(LANGUAGE_NB);
        }

        if (isValidLocale(locale)) {
            return locale.toString().substring(0, 2);
        }

        return LANGUAGE_NB;

    }

    private static boolean isValidLocale(final Locale loc) {
        if (loc == null) {
            return false;
        }
        final String language = loc.toString();
        return language.startsWith(LANGUAGE_NB) || language.startsWith(LANGUAGE_NN) || language.startsWith(LANGUAGE_EN)
                || language.startsWith(LANGUAGE_SE);
    }
}
