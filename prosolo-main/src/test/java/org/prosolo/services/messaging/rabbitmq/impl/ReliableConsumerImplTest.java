package org.prosolo.services.messaging.rabbitmq.impl;

import org.junit.Test;
import org.prosolo.core.stress.TestContext;
import org.prosolo.services.messaging.rabbitmq.ReliableConsumer;
import org.springframework.beans.factory.annotation.Autowired;

public class ReliableConsumerImplTest  extends TestContext {

	@Autowired private ReliableConsumer reliableConsumer;
	@Autowired private DefaultMessageWorker messageWorker;
	private static long count = 0;
	@Test
	public void testStartAsynchronousConsumer() {
		/*reliableConsumer.setWorker(new MessageWorker() {
	        @Override
	        public void handle(String message) throws WorkerException {
	          System.out.println(count+". received: " + message);
	          ++count;
	        }
	      });*/
		//reliableConsumer.setWorker(messageWorker);
		reliableConsumer.StartAsynchronousConsumer();
		while(true){
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
