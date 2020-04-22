package no.idporten.minidplus.validator;

import no.difi.validation.EmailValidator;
import no.difi.validation.MobileValidator;
import no.difi.validation.SsnValidator;
import no.idporten.validation.util.RandomUtil;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * //todo splitte opp, men for å ha samme regler som idporten
 * Validates according to various policies.
 */
public final class ValidatorUtil {

    /**
     * Regular expression for pincode characters.
     */
    private static final String PINCODE_LEGAL_CHARS_REGEX = "^[0-9]{5}$";
    /**
     * Regular expression compiled pattern for pincode characters.
     */
    private static final Pattern PINCODE_LEGAL_CHARS_PATTERN = Pattern.compile(PINCODE_LEGAL_CHARS_REGEX);
    /**
     * Regular expression for password characters.
     */
    public static final String PWD_LEGAL_CHARS_REGEX = "^[-_!\"#\\$%&'()*+,-\\./:;<=>?@\\[\\]\\\\^_`{|}~£€a-zA-Z0-9]+$";
    /**
     * Regular expression compiled pattern for password characters.
     */
    private static final Pattern PWD_LEGAL_CHARS_PATTERN = Pattern.compile(PWD_LEGAL_CHARS_REGEX);
    /**
     * Regular expression complied pattern for numbers in a password.
     */
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^.*[0-9].*$");
    /**
     * Regular expression complied pattern for letters in a password.
     */
    private static final Pattern LETTER_PATTERN = Pattern.compile("^.*[a-zA-Z].*$");
    /**
     * Minimum length of a password.
     */
    private static final int PWD_MIN_LENGTH = 8;
    /**
     * Maximum length of a password.
     */
    private static final int PWD_MAX_LENGTH = 256;
    /**
     * Length of a temporary password.
     */
    private static final int TEMP_PWD_LENGTH = 6;
    /**
     * Unnecessary chars in a MSISDN.
     */
    private static final Pattern MOBILE_PRUNE_PATTERN = Pattern.compile("[- _]");
    /**
     * Length of a Norwegian number without country code.
     */
    private static final int MOBILE_NORWEGIAN_LENGTH = 8;
    /**
     * Norwegian country code prefixes.
     */
    private static final String NORWEGIAN_LANDCODE_00 = "0047";
    private static final String NORWEGIAN_LANDCODE_PLUS = "+47";
    /**
     * The length of a pin code.
     */
    private static final int PINCODE_LENGTH = 5;

    /**
     * Hiding default constructor, as this class should never be instantiated.
     */
    private ValidatorUtil() {
    }

    /**
     * Validates the pincode supplied.
     *
     * @param pincode The pincode to check
     * @return True if pincode is valid
     */
    public static boolean validatePincode(final String pincode) {
        if (pincode == null || pincode.length() != PINCODE_LENGTH) {
            return false;
        }
        final Matcher matcher = PINCODE_LEGAL_CHARS_PATTERN.matcher(pincode);
        return matcher.matches();
    }

    /**
     * Validates the password supplied against the implemented policy.
     *
     * @param password The password to check
     * @return True if password is valid
     */
    public static boolean validatePassword(final String password) {

        if ((password == null) || (password.length() == 0)) {
            return false;
        }

        if ((password.length() < PWD_MIN_LENGTH) || (password.length() > PWD_MAX_LENGTH)) {
            return false;
        }

        final Matcher matcher = PWD_LEGAL_CHARS_PATTERN.matcher(password);
        if (!matcher.matches()) {
            return false;
        }

        final boolean hasNumber = NUMBER_PATTERN.matcher(password).matches();
        final boolean hasLetter = LETTER_PATTERN.matcher(password).matches();
        return !(!hasNumber || !hasLetter);

    }

    /**
     * Validates the temp password supplied against the implemented policy.
     *
     * @param password The password to check
     * @return True if password is valid
     */
    public static boolean validateTempPassword(final String password) {

        // Check that the password is not null
        if ((password == null) || (password.length() == 0)) {
            return false;
        }

        // Check password length
        if ((password.length() < TEMP_PWD_LENGTH) || (password.length() > TEMP_PWD_LENGTH)) {
            return false;
        }

        // Check password chars
        final Pattern tempPattern = Pattern.compile("^[-_a-z0-9]+$");

        final Matcher matcher = tempPattern.matcher(password);
        if (!matcher.matches()) {
            return false;
        }

        // Check for letters and numbers
        final boolean hasNumber = NUMBER_PATTERN.matcher(password).matches();
        final boolean hasLetter = LETTER_PATTERN.matcher(password).matches();
        return !(!hasNumber || !hasLetter);

    }

    /**
     * Validates the 'one time code' password supplied against the implemented policy.
     *
     * @param password The password to check
     * @return True if password is valid
     */
    public static boolean validateOTCPassword(final String password) {
        // Check that the password is not null
        if (isEmpty(password)) {
            return false;
        }

        // Check password length
        if ((password.length() != RandomUtil.PWD_OTC_LENGTH)) {
            return false;
        }

        // Check password chars
        final Pattern otcPattern = Pattern.compile("^[-_a-z0-9]+$");

        final Matcher matcher = otcPattern.matcher(password);
        if (!matcher.matches()) {
            return false;
        }

        // Check for letters and numbers
        final boolean hasNumber = NUMBER_PATTERN.matcher(password).matches();
        final boolean hasLetter = LETTER_PATTERN.matcher(password).matches();
        if (!hasNumber || !hasLetter) {
            return false;
        }

        // Checks complete, password valid
        return true;
    }

    /**
     * Validates email address.
     *
     * @param email The email address to validate
     * @return True if valid
     */
    public static boolean validateEmailAddress(final String email) {
        return EmailValidator.isValid(email);
    }

    /**
     * prune email address.
     * <p>
     * Empty --> null
     * nonempty --> email.trim()
     *
     * @return null or trimmed email
     */
    public static String pruneEmail(final String email) {
        if (isEmpty(email)) {
            return null;
        } else {
            return email.trim();
        }
    }


    /**
     * Validates that a value is allowed among a selection of values.  Can be used for select lists, radio groups.
     *
     * @param value
     * @param acceptedValues
     * @return
     */
    public static boolean validateSelection(final String value, List<String> acceptedValues) {
        return value != null && acceptedValues != null && acceptedValues.contains(value);
    }


    /**
     * Validates mobile phone number.
     *
     * @param mobileNumber The mobile number to validate
     * @return True if valid
     */
    public static boolean validateMobileNumber(final String mobileNumber) {
        return MobileValidator.isValid(mobileNumber);
    }

    private static int getStartNumber(final String mobileNumber) {

        // Set the startPosition of the telephone number without land code
        if (mobileNumber.length() == MOBILE_NORWEGIAN_LENGTH) {
            return 0;
        } else if (mobileNumber.startsWith(NORWEGIAN_LANDCODE_00)) {
            return NORWEGIAN_LANDCODE_00.length();
        } else if (mobileNumber.startsWith(NORWEGIAN_LANDCODE_PLUS)) {
            return NORWEGIAN_LANDCODE_PLUS.length();
        }
        return -1;
    }

    /**
     * Removes unnecessary chars from the mobile phone number.
     *
     * @param mobileNumber The mobile number to prune
     * @return The modified mobile phone number
     */
    public static String pruneMobileNumber(final String mobileNumber) {
        // Check that the mobile number is not null
        if ((mobileNumber == null) || (mobileNumber.trim().length() == 0)) {
            return null;
        }

        // Remove unnecessary chars
        // Checks complete, email valid
        return MOBILE_PRUNE_PATTERN.matcher(mobileNumber).replaceAll("");
    }


    /**
     * Check if the supplied string is null string is null or empty.
     *
     * @param string the string to validate
     * @return If the string is not empty, false otherwise true
     */
    public static boolean isEmpty(final String string) {
        return (string == null) || (string.trim().length() == 0) || "".equals(string.trim());
    }

    /**
     * Checks if input is a valid  norwegian ssn
     *
     * @param ssn
     * @return true if valid ssn
     */
    public static boolean validateSsn(final String ssn) {
        return SsnValidator.isValid(ssn);
    }

}
