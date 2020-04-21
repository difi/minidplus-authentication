package no.idporten.minidplus.service;

import lombok.RequiredArgsConstructor;
import no.idporten.domain.sp.ServiceProvider;
import no.idporten.domain.user.MinidUser;
import no.idporten.domain.user.PersonNumber;
import no.idporten.minidplus.config.SmsProperties;
import no.idporten.minidplus.domain.SmsMessage;
import no.idporten.minidplus.exception.MinIDSystemException;
import no.minid.service.MinIDService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class AuthenticationService {

    /** The one-time password (in memory). */
    private transient String generatedOneTimeCode;

    @Value("${idporten.serviceprovider.default-name}")
    private String serviceProviderDefaultName;

    /**
     * Map from entity encodings to characters.
     */
    private static final Map<String, String> entityToCharacterMap = new HashMap<String, String>();

    /**
     * Regex matching unkown encodings.
     */
    public static final String unknownEntityRegex = "&#\\d*;";

    private final MessageSource messageSource;

    private final OTCPasswordService otcPasswordService;

    private final SmsService smsService;

    private final SmsProperties smsProperties;

    private final MinIDService minIDService;


    public String authenticateUser(String pid, String password, ServiceProvider sp) throws IOException {

        PersonNumber uid = new PersonNumber(pid);
        MinidUser identity = minIDService.findByPersonNumber(uid);

        if (identity == null) {
            throw new IllegalStateException("This state requires an associated identity");
        }

        if(!minIDService.validateUserPassword(uid, password)) {
            //throw error;
        }

        // Generates one time code and sends SMS with one time code to user's mobile phone number
        // Does not send one time code to users that are not allowed to get temporary passwords
        // Does not resend one time code
        if (generatedOneTimeCode == null) {
            generatedOneTimeCode = otcPasswordService.generateOTCPassword();

            try {
                final String mobileNumber = identity.getPhoneNumber().getNumber();
                final SmsMessage message = new SmsMessage(mobileNumber, getMessageBody(sp), smsProperties.getOnetimepasswordTtl());
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

        return "enterOTCPage";
    }

    public String checkOTCCode(String inputOneTimeCode) {
        if (inputOneTimeCode.equalsIgnoreCase(generatedOneTimeCode)) {
            return "successPage";
        }
        return "InvalidCode";
    }

    private String getMessageBody(ServiceProvider sp) {
        if (isDummySp(sp)) {
            return getMessage(
                    "auth.ui.output.otc.message",
                    new String[] { generatedOneTimeCode });
        } else {
            final String messageBody = getMessage(
                    "auth.ui.output.otc.message.sp",
                    new String[] { generatedOneTimeCode, sp.getName().trim() });
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
     *
     * @param sp sp
     * @return true if a dummy/not real sp
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
     *
     * @param message message text
     * @return new message text with replaced entities
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
     * @param key message key
     * @param args Argument values defined in given message
     * @return Message for given key and locale, where argument placeholders are replaced with values from args.
     */
    public String getMessage(String key, Object[] args) {
        return messageSource.getMessage(key, args, new Locale("eng"));
    }

}
