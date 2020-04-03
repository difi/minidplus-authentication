package no.idporten.minidplus.util;

import java.util.regex.Pattern;

public class ValidatorUtil {

    private static final Pattern MOBILE_PRUNE_PATTERN = Pattern.compile("[- _]");

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
}
