package org.prosolo.messaging;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.Ignore;
import org.junit.Test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 *
 * @author Zoran Jeremic, Sep 1, 2014
 *
 */
public class ProducerTest {
	private final static String QUEUE_NAME = "hello";
	@Test
	public void producerTest(){
		try {
			Producer producer=new Producer();
			while(true){
				for(int i=0;i<1000000;i++){
					producer.send("test message:"+i);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Ignore
	@Test
	public void sendSimpleMessage(){
		 ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");
	    Connection connection;
	    Channel channel;
	    
		try {
			try {
				connection = factory.newConnection();
				channel = connection.createChannel();
				channel.queueDeclare(QUEUE_NAME, false, false, false, null);
				String message = "Hello World!";
				channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
				System.out.println(" [x] Sent '" + message + "'");
				channel.close();
				connection.close();
			} catch (TimeoutException e) {
				e.printStackTrace();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
