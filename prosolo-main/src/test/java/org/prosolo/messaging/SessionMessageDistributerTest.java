package org.prosolo.messaging;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.prosolo.common.messaging.rabbitmq.ReliableConsumer;
import org.prosolo.common.messaging.rabbitmq.ReliableProducer;
import org.prosolo.common.messaging.rabbitmq.impl.ReliableConsumerImpl;
import org.prosolo.common.messaging.rabbitmq.impl.ReliableProducerImpl;
import org.prosolo.core.stress.TestContext;
import org.prosolo.services.messaging.SessionMessageDistributer;
import org.prosolo.services.messaging.rabbitmq.impl.DefaultMessageWorker;
import org.springframework.beans.factory.annotation.Autowired;

public class SessionMessageDistributerTest extends TestContext{
	
	@Autowired SessionMessageDistributer distributer;
	private ReliableConsumer reliableConsumer;
	private ReliableProducer reliableProducer;
	
	@Test
	public void testDistributeMessageTest() {
		reliableConsumer=new ReliableConsumerImpl();
		reliableConsumer.setWorker(new DefaultMessageWorker());
		//reliableConsumer.init(QueueNames.SESSION);
		reliableConsumer.StartAsynchronousConsumer();
		
		reliableProducer=new ReliableProducerImpl();
		//reliableProducer.init(QueueNames.SESSION);
		//reliableProducer.startAsynchronousPublisher();
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("updateStatusWall","updateStatusWall value");
		parameters.put("updateGoalWall", "update goal wall");
		parameters.put("connectGoalNoteToStatus", "connectGoalNoteToStatus value");
		for(int i=0;i<3;i++){
		//distributer.distributeMessage(ServiceType.UPDATEUSERSOCIALACTIVITYINBOX, i, i+100, null, parameters);
			reliableProducer.send("{some message}");
			
		}
		try {
			Thread.sleep(50000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
