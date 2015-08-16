package org.prosolo.bigdata.events;

import org.prosolo.bigdata.events.pojo.AnalyticsEvent;
import org.prosolo.bigdata.events.pojo.DefaultEvent;
import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.streaming.Topic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.log4j.Logger;

/**
 * @author Zoran Jeremic Apr 5, 2015
 *
 */

public class EventFactory {

	private final static Logger logger = Logger.getLogger(EventFactory.class
			.getName());

	public static DefaultEvent createLogEvent(Topic eventTopic, String message) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(LogEvent.class,
				new LogEventDeserializer());
		Gson gson = gsonBuilder.create();
		LogEvent event = gson.fromJson(message, LogEvent.class);
		event.setTopic(eventTopic);
		return event;
	}

	public static DefaultEvent createAnalyticsEvent(Topic eventTopic,
			String message) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(AnalyticsEvent.class,
				new AnalyticsEventDeserializer());
		Gson gson = gsonBuilder.create();
		AnalyticsEvent event = gson.fromJson(message, AnalyticsEvent.class);
		event.setTopic(eventTopic);
		return event;
	}

}
