package org.prosolo.common.event;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2017-11-09
 * @since 1.2.0
 */
public class EventQueue {

    private List<EventData> events;

    private EventQueue() {
        events = new LinkedList<>();
    }

    public static EventQueue newEventQueue() {
        return new EventQueue();
    }

    public static EventQueue of(EventData event) {
        EventQueue queue = newEventQueue();
        queue.appendEvent(event);
        return queue;
    }

    /**
     * Adds event to the end of the queue
     *
     * @param event
     */
    public void appendEvent(EventData event) {
        events.add(event);
    }

    public void appendEvents(List<EventData> events) {
        this.events.addAll(events);
    }

    /**
     * Adds all events from the passed queue to the end of the queue
     *
     * @param eventQueue
     */
    public void appendEvents(EventQueue eventQueue) {
        events.addAll(eventQueue.getEvents());
    }

    public List<EventData> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public boolean isEmpty() {
        return events.isEmpty();
    }
}
