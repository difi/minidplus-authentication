package no.idporten.minidplus.domain;


import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Models a Message as used for MinSide messaging. This class may be subclassed to provide data-specific features such
 * as lazy loading.
 *
 * Note: this class must make no assumptions about how messages are stored or sent.
 *
 * @author jonathan.scudder
 * @version 2.0
 */
@Data
@NoArgsConstructor
public class Message {

    /** The address of the (intended) recipient of the message. */
    private String to;

    /** The address of the sender of the message. */
    private String from;

    /** The subject of the message. */
    private String subject;

    /** The body of the message. */
    private String body;

}
