package org.prosolo.services.messaging.rabbitmq.impl;


import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.services.messaging.MessageWrapperAdapter;
import org.prosolo.services.messaging.data.MessageWrapper;
import org.prosolo.services.messaging.data.SessionMessage;
import org.prosolo.services.messaging.data.SystemMessage;
import org.prosolo.services.messaging.impl.SessionMessageHandlerImpl;
import org.prosolo.services.messaging.impl.SystemMessageHandlerImpl;
import org.prosolo.services.messaging.rabbitmq.MessageWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Service("org.prosolo.services.messaging.rabbitmq.DefaultMessageWorker")
public class DefaultMessageWorker implements MessageWorker{
	private static Logger logger = Logger.getLogger(DefaultMessageWorker.class);
	@Autowired private SessionMessageHandlerImpl sessionMessageHandler;
	@Autowired private SystemMessageHandlerImpl systemMessageHandler;
	GsonBuilder gson = new GsonBuilder();
	Gson simpleGson=new Gson();
	DefaultMessageWorker(){
		gson.registerTypeAdapter(MessageWrapper.class, new MessageWrapperAdapter());
	}
	@Override
	public void handle(String message) throws WorkerException {
	 	System.out.println("HANDLING MESSAGE:"+message);
		MessageWrapper messageWrapper=gson.create().fromJson(message, MessageWrapper.class);
		if(messageWrapper.getMessage() instanceof SessionMessage){
			SessionMessage sessionMessage=(SessionMessage) messageWrapper.getMessage();
			logger.debug(message);
//			System.out.println("RECEIVED MESSAGE FROM:"+messageWrapper.getSender()+" MESSAGE:"+simpleGson.toJson(sessionMessage));
	   		sessionMessageHandler.handle(sessionMessage);
		}else if(messageWrapper.getMessage() instanceof SystemMessage){
			if(Settings.getInstance().config.rabbitmq.masterNode){
				systemMessageHandler.handle((SystemMessage) messageWrapper.getMessage());
			}
	 		
		}
	}

}
