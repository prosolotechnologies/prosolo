package org.prosolo.services.messaging.impl;

import org.apache.log4j.Logger;
import org.prosolo.common.messaging.MessageWrapperAdapter;
import org.prosolo.common.messaging.data.AnalyticalServiceMessage;
import org.prosolo.common.messaging.data.MessageWrapper;
import org.prosolo.common.messaging.rabbitmq.QueueNames;
import org.prosolo.common.messaging.rabbitmq.ReliableProducer;
import org.prosolo.common.messaging.rabbitmq.impl.ReliableProducerImpl;
import org.prosolo.services.messaging.AnalyticalServiceMessageDistributer;
import org.prosolo.services.messaging.LogsMessageDistributer;
import org.prosolo.web.ApplicationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.GsonBuilder;

/**
@author Zoran Jeremic Apr 12, 2015
 *
 */
@Service("org.prosolo.services.messaging.AnalyticalServiceMessageDistributer")
public class AnalyticalServiceMessageDistributerImpl implements AnalyticalServiceMessageDistributer{
	@Autowired private ApplicationBean applicationBean;
	private static Logger logger = Logger .getLogger(LogsMessageDistributer.class.getName());
	private GsonBuilder gson;
	private ReliableProducer reliableProducer;
	
	public AnalyticalServiceMessageDistributerImpl(){
		 gson = new GsonBuilder();
		 gson.registerTypeAdapter(MessageWrapper.class, new MessageWrapperAdapter());

	}
	
	@Override
	public void distributeMessage(AnalyticalServiceMessage message){
		if(reliableProducer==null){
			 reliableProducer=new ReliableProducerImpl();
			 reliableProducer.setQueue(QueueNames.ANALYTICS.name().toLowerCase());
			 reliableProducer.startAsynchronousPublisher();
		}		
		wrapMessageAndSend(message);
		
	}
	
	@Override
	public void wrapMessageAndSend(AnalyticalServiceMessage message){
		MessageWrapper wrapper = new MessageWrapper();
		wrapper.setSender(applicationBean.getServerIp());
		wrapper.setMessage(message);
		wrapper.setTimecreated(System.currentTimeMillis());
		String msg = gson.create().toJson(wrapper);
		logger.debug("Sending analytical message:"+msg);
		reliableProducer.send(msg);
	}

}

