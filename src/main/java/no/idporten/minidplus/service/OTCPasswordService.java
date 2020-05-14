package no.idporten.minidplus.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.resilience.CorrelationId;
import no.idporten.domain.sp.ServiceProvider;
import no.idporten.domain.user.MinidUser;
import no.idporten.domain.user.PersonNumber;
import no.idporten.minidplus.exception.IDPortenExceptionID;
import no.idporten.minidplus.exception.minid.MinIDPincodeException;
import no.idporten.minidplus.exception.minid.MinIDTimeoutException;
import no.idporten.minidplus.linkmobility.LINKMobilityClient;
import no.idporten.minidplus.notification.NotificationService;
import no.idporten.validation.util.RandomUtil;
import no.minid.exception.MinidUserInvalidException;
import no.minid.exception.MinidUserNotFoundException;
import no.minid.service.MinIDService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.LocalDateTime.now;

@RequiredArgsConstructor
@Service
@Slf4j
public class OTCPasswordService {

    /** Password 'one time code' length. */
    public static final int PWD_OTC_LENGTH = 5;

    @Value("${idporten.serviceprovider.default-name}")
    private String serviceProviderDefaultName;

    @Value("${minid-plus.cache.otp-ttl-in-s:600}")
    private int otpTtl;

    /** Characters that can be used in a password. */
    private static final char[] PWD_OTC_ALL_CHARS = "acdefghjkmnpqrstwxyz2345789".toCharArray();

    /** Regular expression complied pattern for numbers in a password. */
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^.*[0-9].*$");
    /** Regular expression complied pattern for letters in a password. */
    private static final Pattern LETTER_PATTERN = Pattern.compile("^.*[a-zA-Z].*$");

    @Value("${minid-plus.quarantine-counter-max-number}")
    private int maxNumberOfQuarantineCounters;

    /**
     * Map from entity encodings to characters.
     * //todo hvorfor er denne tom
     */
    private static final Map<String, String> entityToCharacterMap = new HashMap<String, String>();

    /**
     * Regex matching unkown encodings.
     */
    private static final String unknownEntityRegex = "&#\\d*;";

    private static final int MAX_GENERATION_TIMES = 10;

    /** Random class. */
    private final transient SecureRandom random = new SecureRandom();

    private final MinidPlusCache minidPlusCache;

    private final LINKMobilityClient linkMobilityClient;

    private final NotificationService notificationService;

    private final MinIDService minIDService;

    private final MessageSource messageSource;
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

    void sendSMSOtp(String sid, ServiceProvider sp, MinidUser identity) throws MinidUserInvalidException {
        // Generates one time code and sends SMS with one time code to user's mobile phone number
        // Does not send one time code to users that are not allowed to get temporary passwords
        // Does not resend one time code
        if (identity.getPhoneNumber() == null) {
            throw new MinidUserInvalidException("Mobile number not found not found for user");
        }
        if (minidPlusCache.getOTP(sid) == null) {
            String generatedOneTimeCode = generateOTCPassword();
            minidPlusCache.putOTP(sid, generatedOneTimeCode);
            final String mobileNumber = identity.getPhoneNumber().getNumber();
            linkMobilityClient.sendSms(mobileNumber, getMessageBody(sp, generatedOneTimeCode, now().plusSeconds(otpTtl)));
            if (log.isInfoEnabled()) {
                log.info(CorrelationId.get() + " " + "Otp sendt to " + mobileNumber);
            }
        }
    }

    public void sendEmailOtp(String sid, MinidUser identity) {
        // Generates one time code and sends one time code to user's email
        // Does not send one time code to users that are not allowed to get temporary passwords
        // Does not resend one time code
        if (minidPlusCache.getOTP(sid) == null) {
            String generatedOneTimeCode = generateOTCPassword();
            minidPlusCache.putOTP(sid, generatedOneTimeCode);
            final String email = identity.getEmail().getAddress();
            notificationService.sendForgottenPasswordEmail(email, generatedOneTimeCode, now().plusSeconds(otpTtl));
            if (log.isInfoEnabled()) {
                log.info(CorrelationId.get() + " " + "Otp sendt to " + email);
            }
        }
    }

    public boolean checkOTCCode(String sid, String inputOneTimeCode) throws MinIDPincodeException, MinidUserNotFoundException, MinIDTimeoutException {
        String pid = minidPlusCache.getSSN(sid);
        MinidUser user;
        if (pid != null) {
            user = minIDService.findByPersonNumber(new PersonNumber(pid));
        } else {
            throw new MinIDTimeoutException("Otc code timed out");
        }
        if (user.getQuarantineCounter() == null) {
            user.setQuarantineCounter(0);
        }
        if (user.isOneTimeCodeLocked()) {
            if (user.getQuarantineExpiryDate().before(Date.from(Clock.systemUTC().instant().minusSeconds(3600)))) {
                warn("User has been in quarantine for more than one hour.", user.getPersonNumber().getSsn());
                throw new MinIDPincodeException(IDPortenExceptionID.IDENTITY_QUARANTINED, "User has been in quarantine for more than one hour.");
            }
            warn("Pincode locked for ssn=", user.getPersonNumber().getSsn());
            throw new MinIDPincodeException(IDPortenExceptionID.IDENTITY_PINCODE_LOCKED, "pin code is locked");
        }

        if (user.getQuarantineCounter() >= maxNumberOfQuarantineCounters) {
            user.setOneTimeCodeLocked(true);
            minIDService.blockOneTimeCode(user.getPersonNumber(), user.getOneTimeCodeLocked());
            warn("Pincode is locked for ssn=", user.getPersonNumber().getSsn());
            throw new MinIDPincodeException(IDPortenExceptionID.IDENTITY_PINCODE_LOCKED, "pin code is locked");
        }

        if (otpIsValid(sid, inputOneTimeCode)) {
            minidPlusCache.removeOTP(sid);
            // resetting counters when setting user to authenticated.
            resetCountersOnIdentity(user);
            minIDService.setQuarantineCounter(user.getPersonNumber(), user.getQuarantineCounter());
            updateUserAfterSuccessfulLogin(user);
            return true;
        } else { // Increments the error counter
            user.setQuarantineCounter(user.getQuarantineCounter() + 1);
            if (user.getQuarantineCounter() >= maxNumberOfQuarantineCounters) {
                user.setQuarantineExpiryDate(Date.from(Clock.systemUTC().instant().plusSeconds(3600)));
                minIDService.setQuarantineExpiryDate(user.getPersonNumber(), user.getQuarantineExpiryDate());
            }
            minIDService.setQuarantineCounter(user.getPersonNumber(), user.getQuarantineCounter());
            if (checkIfLastTry(user)) {
                if (log.isDebugEnabled()) {
                    log.debug("Last attempt for user " + user);
                }
            }
            warn("Pincode incorrect for ssn=", user.getPersonNumber().getSsn());
            return false;
        }
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

    private String getMessageBody(ServiceProvider sp, String otc, LocalDateTime expire) {
        if (isDummySp(sp)) {
            return getMessage(
                    "auth.ui.output.otc.message",
                    new String[]{otc, DateTimeFormatter.ofPattern("HH:mm").format(expire)});
        } else {
            final String messageBody = getMessage(
                    "auth.ui.output.otc.message.sp",
                    new String[]{otc, sp.getName().trim(), DateTimeFormatter.ofPattern("HH:mm").format(expire)});
            return replaceEntities(messageBody);
        }
    }

    /**
     * Checks if SP is a dummy or not a real SP.  That is:
     * <p>
     * - it is null
     * - it is marked as dummy
     * - it does not have a name set
     * - the name set is "default"
     * - the name is the default configured for ID-porten
     */
    private boolean isDummySp(final ServiceProvider sp) {
        if (sp == null || StringUtils.isEmpty(sp.getName()) || sp.isDummy() || "default".equals(sp.getName())) {
            return true;
        }
        return sp.getName().equals(serviceProviderDefaultName);
    }

    private boolean otpIsValid(String sid, String oneTimePassword) {
        String expectedOtp = minidPlusCache.getOTP(sid);
        return sid != null && expectedOtp != null && expectedOtp.equalsIgnoreCase(oneTimePassword);
    }

    /**
     * Fetch message with given key and locale.
     *
     * @param key  message key
     * @param args Argument values defined in given message
     * @return Message for given key and locale, where argument placeholders are replaced with values from args.
     */
    private String getMessage(String key, Object[] args) {
        return messageSource.getMessage(key, args, new Locale("en"));
    }

    private void updateUserAfterSuccessfulLogin(MinidUser user) throws MinidUserNotFoundException {
        user.setLastLogin(new Date());
        minIDService.updateContactInformation(user);
    }

    private boolean checkIfLastTry(MinidUser user) {
        return user.getQuarantineCounter() == maxNumberOfQuarantineCounters - 1;

    }

    /**
     * Resets the quarantine counters on MinidUser.
     */
    private MinidUser resetCountersOnIdentity(MinidUser user) {
        if (user.getQuarantineCounter() > 0) {
            user.setQuarantineCounter(0);
        }
        return user;
    }

    /**
     * Replaces predefined entities with predefined characters.  If an entity is
     * found and it's not predefined, it is replaced with " ".
     */
    private static String replaceEntities(final String message) {
        if (StringUtils.isEmpty(message)) {
            return message;
        }
        String encoded = message;
        // replace all predefined entities
        for (Map.Entry<String, String> entry : entityToCharacterMap.entrySet()) {
            encoded = encoded.replaceAll(entry.getKey(), entry.getValue());
        }
        // replace all unkown entities
        encoded = encoded.replaceAll(unknownEntityRegex, " ");

        return encoded;
    }

    private void warn(String message, String ssn) {
        log.warn(CorrelationId.get() + " " + ssn + " " + message);
    }
}
