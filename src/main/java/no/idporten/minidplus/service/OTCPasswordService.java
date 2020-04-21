package no.idporten.minidplus.service;

import lombok.RequiredArgsConstructor;
import no.idporten.validation.util.RandomUtil;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class OTCPasswordService {

    /** Password 'one time code' length. */
    public static final int PWD_OTC_LENGTH = 5;


    /** Characters that can be used in a password. */
    private static final char[] PWD_OTC_ALL_CHARS = "acdefghjkmnpqrstwxyz2345789".toCharArray();

    /** Regular expression complied pattern for numbers in a password. */
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^.*[0-9].*$");
    /** Regular expression complied pattern for letters in a password. */
    private static final Pattern LETTER_PATTERN = Pattern.compile("^.*[a-zA-Z].*$");

    private static final int MAX_GENERATION_TIMES = 10;

    /** Random class. */
    private final transient SecureRandom random = new SecureRandom();

    /**
     * Generates a new valid OTC password.
     *
     * @return new password
     */
    public String generateOTCPassword() {

        String password = newPasswordOTC();
        // Generate new password if password is invalid (Password is generated random,
        // so can be without letters or numbers)
        int tries = 1;

        while (!validateOTCPassword(password) && (tries < MAX_GENERATION_TIMES)) {
            password = newPasswordOTC();
            tries++;
        }

        return password;
    }

    /**
     * Generates a new password, can be invalid.
     *
     * @return new password
     */
    private String newPasswordOTC() {
        final StringBuilder password = new StringBuilder();

        // Append all characters except the last two
        for (int i = 0; i < PWD_OTC_LENGTH; i++) {
            final int nextCharacter = random.nextInt(PWD_OTC_ALL_CHARS.length);
            password.append(PWD_OTC_ALL_CHARS[nextCharacter]);
        }

        return password.toString();
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
     * Check if the supplied string is null string is null or empty.
     *
     * @param string the string to validate
     * @return If the string is not empty, false otherwise true
     */
    public static boolean isEmpty(final String string) {
        return (string == null) || (string.trim().length() == 0) || "".equals(string.trim());
    }
}
