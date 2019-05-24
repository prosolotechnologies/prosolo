package org.prosolo.common.messaging.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.prosolo.common.event.EventData;
import org.prosolo.common.event.EventQueue;

import java.util.List;

/**
 * It is used for sending application generated events
 * through messaging service (RabbitMQ)
 *
 * @author stefanvuckovic
 * @date 2019-04-23
 * @since 1.3.2
 */
@AllArgsConstructor
@Getter
@Setter
public class AppEventMessage extends SimpleMessage {

    private EventQueue events;

}
