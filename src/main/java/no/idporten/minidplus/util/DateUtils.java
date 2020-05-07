package no.idporten.minidplus.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

/** Date utility methods. */
public final class DateUtils {

    /** Default LDAP date format. */
    public static final String DEFAULT_DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";

    /** Date formatter, old-style Java. */
    private static final DateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(DEFAULT_DATE_FORMAT);

    /** Date formatter, Java 8. */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);

    private static final Logger LOGGER = LoggerFactory.getLogger(DateUtils.class);

    /** Private constructor because this is a utility class with only static methods. */
    private DateUtils() {
    }

    /**
     * Parse a date string to a java.util.Date using the specified date format.
     * @param dateString string representation of a date.
     * @return date object, or {@code null} if dateString is not recognized as a date.
     */
    public static Date parseDate(final String dateString) {
        return parseDate(dateString, SIMPLE_DATE_FORMAT);
    }

    /**
     * Parse a date string to a java.util.Date using the specified date format.
     * @param dateString string representation of a date.
     * @param dateFormat date format to use
     * @return date object, or {@code null} if dateString is not recognized as a date.
     */
    public static Date parseDate(final String dateString, final DateFormat dateFormat) {
        if (StringUtils.isNullOrEmpty(dateString)) {
            return null;
        }
        try {
            return dateFormat.parse(dateString);
        }
        catch (final ParseException pae) {
            LOGGER.warn("Error parsing date string: '" + dateString + "'", pae);
            return null;
        }
    }

    /**
     * Parse a date string to a java.time.LocalDateTime using the specified date format.
     * @param dateString string representation of a date.
     * @return date object, or {@code null} if dateString is not recognized as a date.
     */
    public static LocalDateTime parseLocalDateTime(final String dateString) {
        return parseLocalDateTime(dateString, DATE_TIME_FORMATTER);
    }

    /**
     * Parse a date string to a java.time.LocalDateTime using the specified date format.
     * @param dateString string representation of a date.
     * @param dateFormat date format to use
     * @return date object, or {@code null} if dateString is not recognized as a date.
     */
    private static LocalDateTime parseLocalDateTime(final String dateString, final DateTimeFormatter dateFormat) {
        if (StringUtils.isNullOrEmpty(dateString)) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateString, dateFormat);
        }
        catch (final DateTimeParseException pae) {
            LOGGER.warn("Error parsing date string: '" + dateString + "'", pae);
            return null;
        }
    }

    /**
     * Encode a date using the default LDAP date format.
     * @param date date object.
     * @return string representation, or {@code null} if date is null.
     */
    public static String encodeDate(final Date date) {
        return encodeDate(date, SIMPLE_DATE_FORMAT);
    }

    /**
     * Encode a date using the default LDAP date format.
     * @param date date object.
     * @return string representation, or {@code null} if date is null.
     */
    public static String encodeDate(final LocalDateTime date) {
        return date == null ? null : date.format(DATE_TIME_FORMATTER);
    }

    /**
     * Encode a date using the specified date format.
     * @param date date object.
     * @param dateFormat date format to use
     * @return string representation, or {@code null} if date is null.
     */
    public static String encodeDate(final Date date, final DateFormat dateFormat) {
        return date == null ? null : dateFormat.format(date);
    }

    /** String utility methods. */
    private static final class StringUtils {

        private StringUtils() {
        }

        /**
         * Check if a str is {@code null} or empty/blank.
         * @param str string to check
         * @return {@code true} if {@code str} is {@code null}, empty or whitespace only;
         *         {@code false} otherwise.
         */
        private static boolean isNullOrEmpty(final String str) {
            return str == null || str.trim().isEmpty();
        }

    }


}
