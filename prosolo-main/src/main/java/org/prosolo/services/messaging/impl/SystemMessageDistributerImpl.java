package org.prosolo.services.messaging.impl;

import java.util.Map;

import org.apache.log4j.Logger;
import org.prosolo.common.messaging.rabbitmq.ReliableProducer;
import org.prosolo.common.messaging.rabbitmq.impl.ReliableProducerImpl;
import org.prosolo.services.messaging.MessageWrapperAdapter;
import org.prosolo.services.messaging.SystemMessageDistributer;
import org.prosolo.services.messaging.data.MessageWrapper;
import org.prosolo.services.messaging.data.ServiceType;
import org.prosolo.services.messaging.data.SystemMessage;
import org.prosolo.services.messaging.rabbitmq.impl.QueueNames;
import org.prosolo.web.ApplicationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.GsonBuilder;

/**
 * @author Zoran Jeremic Oct 17, 2014
 *
 */
@Service("org.prosolo.services.messaging.SystemMessageDistributer")
public class SystemMessageDistributerImpl implements SystemMessageDistributer{
	private static Logger logger = Logger .getLogger(SystemMessageDistributerImpl.class.getName());
	private ReliableProducer reliableProducer;
	@Autowired private ApplicationBean applicationBean;
	
	private GsonBuilder gson;
	//private boolean initialized=false;
	
	public SystemMessageDistributerImpl(){
		 gson = new GsonBuilder();
		 gson.registerTypeAdapter(MessageWrapper.class, new MessageWrapperAdapter());
		
	}
	
	@Override
	public void distributeMessage(ServiceType serviceType,  Map<String, String> parameters){
		if(reliableProducer==null){
			 reliableProducer=new ReliableProducerImpl();
			 reliableProducer.setQueue(QueueNames.SYSTEM.name().toLowerCase());
			 reliableProducer.startAsynchronousPublisher();
		//	reliableProducer.init(QueueNames.SYSTEM);
			// reliableProducer=ServiceLocator.getInstance().getService(ReliableProducer.class).init(QueueNames.SYSTEM);
		}
		SystemMessage message = new SystemMessage();
		message.setServiceType(serviceType);
		message.setParameters(parameters);
		wrapMessageAndSend(message);
		
	}
	
	@Override
	public void wrapMessageAndSend(SystemMessage message){
		MessageWrapper wrapper = new MessageWrapper();
		wrapper.setSender(applicationBean.getServerIp());
		wrapper.setMessage(message);
		wrapper.setTimecreated(System.currentTimeMillis());
		String msg = gson.create().toJson(wrapper);
		logger.debug("Sending system message:"+msg);
		reliableProducer.send(msg);
	}
}
