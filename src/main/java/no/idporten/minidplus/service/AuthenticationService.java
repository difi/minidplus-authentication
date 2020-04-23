package no.idporten.minidplus.service;

import lombok.RequiredArgsConstructor;
import no.idporten.domain.sp.ServiceProvider;
import no.idporten.domain.user.MinidUser;
import no.idporten.domain.user.PersonNumber;
import no.idporten.minidplus.config.SmsProperties;
import no.idporten.minidplus.domain.SmsMessage;
import no.idporten.minidplus.exception.IDPortenExceptionID;
import no.idporten.minidplus.exception.minid.MinIDIncorrectCredentialException;
import no.idporten.minidplus.exception.minid.MinIDSystemException;
import no.idporten.minidplus.exception.minid.MinIDUserNotFoundException;
import no.minid.exception.MinidUserNotFoundException;
import no.minid.service.MinIDService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class AuthenticationService {

    @Value("${minid-plus.credential-error-max-number}")
    private int MAX_NUMBER_OF_CREDENTIAL_ERRORS;
    private final MinidPlusCache minidPlusCache;

    @Value("${idporten.serviceprovider.default-name}")
    private String serviceProviderDefaultName;

    /**
     * Map from entity encodings to characters.
     */
    private static final Map<String, String> entityToCharacterMap = new HashMap<String, String>();

    /**
     * Regex matching unkown encodings.
     */
    private static final String unknownEntityRegex = "&#\\d*;";

    private final MessageSource messageSource;

    private final OTCPasswordService otcPasswordService;

    private final SmsService smsService;

    private final SmsProperties smsProperties;

    private final MinIDService minIDService;


    public boolean authenticateUser(String sid, String pid, String password, ServiceProvider sp) throws MinIDUserNotFoundException, MinIDIncorrectCredentialException, IOException {

        PersonNumber uid = new PersonNumber(pid);
        MinidUser identity = minIDService.findByPersonNumber(uid);

        if (identity == null) {
            throw new MinIDUserNotFoundException(IDPortenExceptionID.LDAP_ENTRY_NOT_FOUND, "User not found uid=" + uid);
        }
        if(identity.isOneTimeCodeLocked()) {
            return false;
        }

        if (!minIDService.validateUserPassword(uid, password)) {
            throw new MinIDIncorrectCredentialException(IDPortenExceptionID.IDENTITY_PASSWORD_INCORRECT, "Password validation failed");
        }
        minidPlusCache.putSSN(sid, identity.getPersonNumber().getSsn());

        sendOtp(sid, sp, identity);
        return true;
    }

    private void sendOtp(String sid, ServiceProvider sp, MinidUser identity) throws IOException {
        // Generates one time code and sends SMS with one time code to user's mobile phone number
        // Does not send one time code to users that are not allowed to get temporary passwords
        // Does not resend one time code
        if (minidPlusCache.getOTP(sid) == null) {
            String generatedOneTimeCode = otcPasswordService.generateOTCPassword();
            minidPlusCache.putOTP(sid, generatedOneTimeCode);
            try {
//                final String mobileNumber = identity.getPhoneNumber().getNumber();
                final SmsMessage message = new SmsMessage("99286853", getMessageBody(sp, sid), smsProperties.getOnetimepasswordTtl());
                smsService.sendSms(message);
                //auditLog(AuditLogger.MINID_OTC_SENDT, new LogData(identity.getPersonNumber().getSsn(), mobileNumber));
            } catch (final MinIDSystemException mse) {
                //Her kan vi sikkert berre returnere til eit anna view istadenfor å bruke "setFeedback".

                // Don't terminate authentication, leave user the option of changing to pincode
                //setFeedback(MinidFeedbackType.WARNING, "auth.ui.error.sendingotc.messsage");
            }
        }
        //Trengst ikkje? Kan løysast i html?
        //setForceDigitalContactInfoShowModule(false);
    }


    public String checkOTCCode(String sid, String inputOneTimeCode) throws MinidUserNotFoundException {

        boolean isOneTimeCodeCorrect = false; //Assume false

        MinidUser user = minIDService.findByPersonNumber(new PersonNumber(minidPlusCache.getSSN(sid)));

        if (inputOneTimeCode.equalsIgnoreCase(minidPlusCache.getOTP(sid))) {
            isOneTimeCodeCorrect = true;
        }
        if (user.getCredentialErrorCounter() == MAX_NUMBER_OF_CREDENTIAL_ERRORS) {
            user.setOneTimeCodeLocked(true);
            return "Error, pin code locked";
        }
        if(user.isOneTimeCodeLocked()) {
            return "Error, pin code locked";
        }

        // Handles incorrect one time codes (and users that are not allowed to get temporary passwords)
        if (!isOneTimeCodeCorrect) { // Increments the error counter
            user.setCredentialErrorCounter(user.getCredentialErrorCounter() + 1);
            minIDService.updateContactInformation(user);
            if (checkIfLastTry(user)) {
                return "Error, last chance";
            }
            return "Error";
        }
        // resetting counters when setting user to authenticated.
        resetCountersOnIdentity(user);

        updateUserAfterSuccessfulLogin(user);

        return "Success";
    }

    protected void updateUserAfterSuccessfulLogin(MinidUser user) throws MinidUserNotFoundException {
        user.setLastLogin(new Date());
        minIDService.updateContactInformation(user);
    }

    protected boolean checkIfLastTry(MinidUser user) {
        return user.getCredentialErrorCounter() == MAX_NUMBER_OF_CREDENTIAL_ERRORS - 1;

    }

    /**
     * Resets the quarantine and credential error counters on MinidUser.
     */
    protected MinidUser resetCountersOnIdentity(MinidUser user) {
        if (user.getCredentialErrorCounter() > 0) {
            user.setCredentialErrorCounter(0);
        }
        return user;
    }


    private String getMessageBody(ServiceProvider sp, String sessionId) {
        if (isDummySp(sp)) {
            return getMessage(
                    "auth.ui.output.otc.message",
                    new String[]{minidPlusCache.getOTP(sessionId) });
        } else {
            final String messageBody = getMessage(
                    "auth.ui.output.otc.message.sp",
                    new String[]{minidPlusCache.getOTP(sessionId), sp.getName().trim() });
            return replaceEntities(messageBody);
        }
    }
/*
    protected void auditLog(final String messageId, final LogData logData) throws MinIDSystemException {
        try {
            auditLogger.log(getSSOSession(), messageId, logData);
        } catch (final AuthLoginException | IDPortenLogException loginExc) {
            throw new MinIDSystemException(IDPortenExceptionID.AUDITLOG_WRITE_FAILED,
                    "Audit logging of new minid user failed",
                    loginExc);
        }
    }*/

    /**
     * Checks if SP is a dummy or not a real SP.  That is:
     *
     * - it is null
     * - it is marked as dummy
     * - it does not have a name set
     * - the name set is "default"
     * - the name is the default configured for ID-porten
     */
    public boolean isDummySp(final ServiceProvider sp) {
        if (StringUtils.isEmpty(sp.getName()) || sp.isDummy() || "default".equals(sp.getName())) {
            return true;
        }
        return sp.getName().equals(serviceProviderDefaultName);
    }

    /**
     * Replaces predefined entities with predefined characters.  If an entity is
     * found and it's not predefined, it is replaced with " ".
     */
    public static String replaceEntities(final String message) {
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

    /**
     * Fetch message with given key and locale.
     *
     * @param key  message key
     * @param args Argument values defined in given message
     * @return Message for given key and locale, where argument placeholders are replaced with values from args.
     */
    public String getMessage(String key, Object[] args) {
        return messageSource.getMessage(key, args, new Locale("en_GB"));
    }

    }
