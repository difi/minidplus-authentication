package no.idporten.minidplus.service;

import lombok.RequiredArgsConstructor;
import no.difi.validation.MobileValidator;
import no.idporten.minidplus.config.SmsProperties;
import no.idporten.minidplus.domain.SmsMessage;
import org.springframework.stereotype.Component;

/**
 * Utility class for use with the PSWinCom xml over Http post interface.
 * @author est
 */
@Component
@RequiredArgsConstructor
public final class PSWinComUtils {

    private static final int NORWEGIAN_NUMBER_LENGTH = 8;
    private static final int STRING_BUFFER_LENGTH = 500;

    private final SmsProperties smsProperties;

    /**
     * A method to create the XML used as a request to PSWinCom.
     * @param sms the sms message.
     * @return the xml as a string.
     */

    public String createPSWinComXML(final SmsMessage sms) {

        final StringBuffer sb = new StringBuffer(STRING_BUFFER_LENGTH);
        sb.append("<?xml version=\"1.0\"?><!DOCTYPE SESSION SYSTEM \"pswincom_submit.dtd\"><SESSION><CLIENT>");
        sb.append(smsProperties.getPswincom().getUsername());
        sb.append("</CLIENT><PW>");
        sb.append(smsProperties.getPswincom().getPassword());
        sb.append("</PW><MSGLST><MSG><TEXT>");
        sb.append(sms.getBody());
        sb.append("</TEXT><RCV>");
        sb.append(MobileValidator.numberCleaner(sms.getTo()));
        sb.append("</RCV><SND>");
        sb.append(smsProperties.getSendernumber());
        sb.append("</SND><TTL>");
        sb.append(smsProperties.getOnetimepasswordTtl());
        sb.append("</TTL></MSG></MSGLST></SESSION>");

        return sb.toString();
    }
}
