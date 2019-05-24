/**
 * 
 */
package org.prosolo.services.event;

import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.event.Event;
import org.prosolo.common.event.EventObserver;
import org.prosolo.common.event.EventQueue;
import org.prosolo.common.event.context.data.UserContextData;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * Class for creating the new Event @Event (actually its subclasses). After the
 * event is created and persisted, it is automatically propagated to the
 * CentralEventDispatcher @CentralEventDispatcher.
 * 
 * @author Nikola Milikic
 * 
 */
@Service("org.prosolo.services.event.EventFactory")
public class EventFactory extends org.prosolo.common.event.EventFactory {

    @Inject private CentralEventDispatcher centralEventDispatcher;

    public Event generateAndPublishEvent(EventType eventType, UserContextData context, BaseEntity object, BaseEntity target, Class<? extends EventObserver>[] observersToExclude, Map<String, String> parameters) {
        Event ev = super.generateEvent(eventType, context, object, target, observersToExclude, parameters);
        if (ev != null) {
            centralEventDispatcher.dispatchEvent(ev);
        }
        return ev;
    }


    public List<Event> generateAndPublishEvents(EventQueue eventQueue) {
        return generateAndPublishEvents(eventQueue, null);
    }

    public List<Event> generateAndPublishEvents(EventQueue eventQueue, Class<? extends EventObserver>[] observersToExclude) {
        List<Event> events = super.generateEvents(eventQueue, observersToExclude);
        if (!events.isEmpty()) {
            centralEventDispatcher.dispatchEvents(events);
        }
        return events;
    }

}
