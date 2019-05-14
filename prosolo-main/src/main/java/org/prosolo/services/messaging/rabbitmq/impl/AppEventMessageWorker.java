package org.prosolo.services.messaging.rabbitmq.impl;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.log4j.Logger;
import org.prosolo.common.messaging.MessageWrapperAdapter;
import org.prosolo.common.messaging.data.AppEventMessage;
import org.prosolo.common.messaging.data.MessageWrapper;
import org.prosolo.common.messaging.data.SessionMessage;
import org.prosolo.common.messaging.rabbitmq.MessageWorker;
import org.prosolo.common.messaging.rabbitmq.WorkerException;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.messaging.impl.SessionMessageHandlerImpl;

public class AppEventMessageWorker implements MessageWorker {

	private static Logger logger = Logger.getLogger(AppEventMessageWorker.class);

	private Gson gson;
	private EventFactory eventFactory;

	public AppEventMessageWorker(EventFactory eventFactory) {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(MessageWrapper.class, new MessageWrapperAdapter());
		gson = builder.create();
		this.eventFactory = eventFactory;
	}

	@Override
	public void handle(String message) throws WorkerException {
	    MessageWrapper messageWrapper = gson.fromJson(message, MessageWrapper.class);
		if (messageWrapper.getMessage() instanceof AppEventMessage) {
			logger.debug("App event message received: " + message);
			AppEventMessage msg = (AppEventMessage) messageWrapper.getMessage();
			eventFactory.generateAndPublishEvents(msg.getEvents());
		}
	}

}
