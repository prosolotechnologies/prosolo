package org.prosolo.bigdata.streaming;

import static org.junit.Assert.*;

import org.junit.Test;
import org.prosolo.bigdata.rabbitmq.ReliableProducer;

/**
@author Zoran Jeremic Apr 3, 2015
 *
 */

public class StreamingManagerImplTest {

	@Test
	public void testInitializeStreaming() {
		 produce();
	}
	public void produce(){
		ReliableProducer rabbitMQProducer = new ReliableProducer();
		 rabbitMQProducer.setQueue(Topic.LOGS.name().toLowerCase());
		 rabbitMQProducer.startAsynchronousPublisher();
		 for( int i=0;i<1000;i++){
			 String data="SOME MESSAGE "+i;
			 rabbitMQProducer.send(data);
			 try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
		 
	}

}

