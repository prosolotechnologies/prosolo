package org.prosolo.services.messaging.rabbitmq.impl;


import org.apache.log4j.Logger;
import org.prosolo.common.messaging.MessageWrapperAdapter;
import org.prosolo.common.messaging.data.MessageWrapper;
import org.prosolo.common.messaging.data.SessionMessage;
import org.prosolo.common.messaging.rabbitmq.MessageWorker;
import org.prosolo.common.messaging.rabbitmq.WorkerException;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.messaging.impl.SessionMessageHandlerImpl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Deprecated
public class DefaultMessageWorker implements MessageWorker{
	private static Logger logger = Logger.getLogger(DefaultMessageWorker.class);
	GsonBuilder gson = new GsonBuilder();
	Gson simpleGson=new Gson();
	public DefaultMessageWorker(){
		gson.registerTypeAdapter(MessageWrapper.class, new MessageWrapperAdapter());
	}
	@Override
	public void handle(String message) throws WorkerException {
		MessageWrapper messageWrapper=gson.create().fromJson(message, MessageWrapper.class);
		if(messageWrapper.getMessage() instanceof SessionMessage){
			SessionMessage sessionMessage=(SessionMessage) messageWrapper.getMessage();
			logger.debug(message);
	   		ServiceLocator.getInstance().getService(SessionMessageHandlerImpl.class).handle(sessionMessage);
		}
	}

}
