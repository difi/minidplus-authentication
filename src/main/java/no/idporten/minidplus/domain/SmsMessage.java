package no.idporten.minidplus.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Models an SMS message. Differs from other messages in that the service provider is associated with the sending of an
 * SMS (mercantile interest: they are responsible for paying). For this reason, an SMS message needs a message service
 * object.
 */
@Data
@NoArgsConstructor
public class SmsMessage extends Message {

    private int timeToLive;


    /**
     * Constructor. Sets object properties, also with TTL.
     *
     * @param toAddr Message recipient
     * @param body Message body
     * @param timeToLive Integer TTL
     */
    public SmsMessage(final String toAddr, final String body, final int timeToLive) {
        this.setTo(toAddr);
        this.setBody(body);
        this.timeToLive = timeToLive;
    }
    
}
