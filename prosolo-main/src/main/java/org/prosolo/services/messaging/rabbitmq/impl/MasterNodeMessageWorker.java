package org.prosolo.services.messaging.rabbitmq.impl;

import org.apache.log4j.Logger;
import org.prosolo.services.messaging.MessageWrapperAdapter;
import org.prosolo.services.messaging.data.MessageWrapper;
import org.prosolo.services.messaging.data.SystemMessage;
import org.prosolo.services.messaging.impl.SystemMessageHandlerImpl;
import org.prosolo.services.messaging.rabbitmq.MessageWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.GsonBuilder;

/**
 * @author Zoran Jeremic Oct 17, 2014
 *
 */
@Service("org.prosolo.services.messaging.rabbitmq.MasterNodeMessageWorker")
public class MasterNodeMessageWorker  implements MessageWorker{
	private static Logger logger = Logger.getLogger(MasterNodeMessageWorker.class);
	@Autowired private SystemMessageHandlerImpl systemMessageHandler;
	GsonBuilder gson = new GsonBuilder();
	
	MasterNodeMessageWorker(){
		gson.registerTypeAdapter(MessageWrapper.class, new MessageWrapperAdapter());
	}
	@Override
	public void handle(String message) throws WorkerException {
		logger.debug(message);
		MessageWrapper messageWrapper=gson.create().fromJson(message, MessageWrapper.class);
		if(messageWrapper.getMessage() instanceof SystemMessage){
	 		systemMessageHandler.handle((SystemMessage) messageWrapper.getMessage());
		}
	}
}
