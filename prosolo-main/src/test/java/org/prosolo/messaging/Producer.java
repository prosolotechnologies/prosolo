package org.prosolo.messaging;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * 
 * @author Zoran Jeremic, Sep 1, 2014
 * 
 */
public class Producer {
	final String ROUTING_KEY = "myRK";
	final String EXCHANGE = "myExchange";
	final String QUEUE="myQueue";
	ConnectionFactory factory = null;
	Connection connection = null;
	Channel channel = null;
	BasicProperties.Builder builder = null;
	BasicProperties properties = null;

	public Producer() throws IOException {
		factory = new ConnectionFactory();
		factory.setHost("127.0.0.1");
		factory.setVirtualHost("/prosolo");
		factory.setUsername("prosolo");
		factory.setPassword("prosolo@2014");
		connection = factory.newConnection();
		channel = connection.createChannel();
		
		channel.exchangeDeclare(EXCHANGE, "direct", true);
		channel.queueDeclare(QUEUE, true, false, false, null);
		channel.queueBind(QUEUE, EXCHANGE, ROUTING_KEY);
		System.out.println("Producer initialized");
	}

	public void send(String message) throws IOException {

		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("HL7A", 1);
		builder = new BasicProperties.Builder();
		properties = builder.deliveryMode(2).headers(headers)
				.contentType("text/plain").build();
		channel.basicPublish(EXCHANGE, ROUTING_KEY, properties,
				message.getBytes());
		System.out.println("Message published...:"+message);
	}

	public void destroy() throws Exception {
		channel.close();
		connection.close();
	}
}
