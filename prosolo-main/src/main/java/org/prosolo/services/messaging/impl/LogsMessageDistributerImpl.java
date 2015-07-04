package org.prosolo.services.messaging.impl;


import org.apache.log4j.Logger;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.messaging.LogsMessageDistributer;
import org.prosolo.services.messaging.MessageWrapperAdapter;
import org.prosolo.services.messaging.data.LogMessage;
import org.prosolo.services.messaging.data.MessageWrapper;
import org.prosolo.services.messaging.rabbitmq.ReliableProducer;
import org.prosolo.services.messaging.rabbitmq.impl.QueueNames;
import org.prosolo.services.messaging.rabbitmq.impl.ReliableProducerImpl;
import org.prosolo.web.ApplicationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.GsonBuilder;
import com.mongodb.DBObject;

/**
@author Zoran Jeremic Apr 4, 2015
 *
 */
@Service("org.prosolo.services.messaging.LogsMessageDistributer")
public class LogsMessageDistributerImpl implements LogsMessageDistributer{
	private static Logger logger = Logger .getLogger(LogsMessageDistributer.class.getName());
	private ReliableProducer reliableProducer;
	@Autowired private ApplicationBean applicationBean;
	
	private GsonBuilder gson;
	//private boolean initialized=false;
	
	public LogsMessageDistributerImpl(){
		 gson = new GsonBuilder();
		 gson.registerTypeAdapter(MessageWrapper.class, new MessageWrapperAdapter());

	}
	
	@Override
	public void distributeMessage(DBObject logObject){
		if(reliableProducer==null){
			 reliableProducer=new ReliableProducerImpl();
			 reliableProducer.setQueue(QueueNames.LOGS.name().toLowerCase());
			 reliableProducer.startAsynchronousPublisher();
		}
		LogMessage message = new LogMessage();
		logger.debug("\n timestamp: " + "));"+logObject.get("timestamp")+ 
	 			"\n eventType: " + logObject.get("eventType") + 
	 			"\n actorId: " + logObject.get("actorId") + 
	 			"\n actorFullname: " + logObject.get("actorFullname") + 
	 			"\n objectType: " + logObject.get("objectType") + 
	 			(((Long) logObject.get("objectId")) > 0 ? "\n objectId: " + logObject.get("objectId") : "") + 
	 			(logObject.get("objectTitle") != null ? "\n objectTitle: " + logObject.get("objectTitle") : "") + 
	 			(logObject.get("targetType") != null ? "\n targetType: " + logObject.get("targetType") : "") + 
				(((Long) logObject.get("targetId")) > 0 ? "\n targetId: " + logObject.get("targetId") : "") + 
				(logObject.get("reasonType") != null ? "\n reasonType: " + logObject.get("reasonType") : "") + 
				(((Long) logObject.get("reasonId")) > 0 ? "\n reasonId: " + logObject.get("reasonId") : "") + 
				(logObject.get("link") != null ? "\n link: " + logObject.get("link") : "") +
			 	"\n parameters: " + logObject.get("parameters"));
		message.setTimestamp((long) logObject.get("timestamp"));
		message.setEventType((String) logObject.get("eventType"));
		message.setActorId((long) logObject.get("actorId"));
		message.setActorFullname((String) logObject.get("actorFullname"));
		message.setObjectType((String) logObject.get("objectType"));
		message.setObjectId((long) logObject.get("objectId"));
		message.setObjectTitle((String) logObject.get("objectTitle"));
		message.setTargetType((String) logObject.get("targetType"));
		message.setTargetId((long) logObject.get("targetId"));
		message.setReasonType((String) logObject.get("reasonType"));
		message.setReasonId((long) logObject.get("reasonId"));
		message.setLink((String) logObject.get("link"));
		message.setParameters((DBObject) logObject.get("parameters"));
		
		wrapMessageAndSend(message);
		
	}
	
	@Override
	public void wrapMessageAndSend(LogMessage message){
		MessageWrapper wrapper = new MessageWrapper();
		wrapper.setSender(applicationBean.getServerIp());
		wrapper.setMessage(message);
		wrapper.setTimecreated(System.currentTimeMillis());
		String msg = gson.create().toJson(wrapper);
		logger.debug("Sending system message:"+msg);
		reliableProducer.send(msg);
	}
}

