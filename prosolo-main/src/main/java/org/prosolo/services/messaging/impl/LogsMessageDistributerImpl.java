package org.prosolo.services.messaging.impl;


import com.google.gson.GsonBuilder;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.prosolo.common.messaging.MessageWrapperAdapter;
import org.prosolo.common.messaging.data.LogMessage;
import org.prosolo.common.messaging.data.MessageWrapper;
import org.prosolo.common.messaging.rabbitmq.QueueNames;
import org.prosolo.common.messaging.rabbitmq.ReliableProducer;
import org.prosolo.common.messaging.rabbitmq.impl.ReliableProducerImpl;
import org.prosolo.services.messaging.LogsMessageDistributer;
import org.prosolo.web.ApplicationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	public void distributeMessage(JSONObject logObject){
		if(reliableProducer==null){
			 reliableProducer=new ReliableProducerImpl();
			 reliableProducer.setQueue(QueueNames.LOGS.name().toLowerCase());
			 reliableProducer.init();
			// reliableProducer.startAsynchronousPublisher();
		}
		LogMessage message = new LogMessage();
		message.setTimestamp((long) logObject.get("timestamp"));
		message.setEventType((String) logObject.get("eventType"));
		message.setActorId((long) logObject.get("actorId"));
		message.setObjectType((String) logObject.get("objectType"));
		message.setObjectId((long) logObject.get("objectId"));
		message.setObjectTitle((String) logObject.get("objectTitle"));
		message.setTargetType((String) logObject.get("targetType"));
		message.setTargetId((long) logObject.get("targetId"));
		message.setReasonType((String) logObject.get("reasonType"));
		message.setLink((String) logObject.get("link"));
		message.setCourseId(logObject.get("courseId") != null ? (long) logObject.get("courseId") : 0);
		message.setTargetUserId((long) logObject.get("targetUserId"));
		message.setLearningContext((JSONObject)logObject.get("learningContext"));
		message.setParameters((JSONObject) logObject.get("parameters"));
		
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

