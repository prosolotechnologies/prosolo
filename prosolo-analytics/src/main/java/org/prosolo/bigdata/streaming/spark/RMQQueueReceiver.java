package org.prosolo.bigdata.streaming.spark;


import java.util.ArrayList;

import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.receiver.Receiver;
import org.prosolo.bigdata.config.RabbitMQConfig;
import org.prosolo.bigdata.config.Settings;
import org.prosolo.bigdata.streaming.Topic;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.ShutdownSignalException;
 

/**
 * @author Zoran Jeremic Apr 3, 2015
 *
 */

public class RMQQueueReceiver extends Receiver<String> {

	public RMQQueueReceiver(StorageLevel storageLevel) {
		super(storageLevel);
		// TODO Auto-generated constructor stub
	}

	//private final static String QUEUE_NAME = "QueueName";
	private ConnectionFactory factory;
	QueueingConsumer consumer;
	//String hostname = "localhost";// define hostname for rabbitmq reciver
	RabbitMQConfig rabbitmqConfig = Settings.getInstance().config.rabbitMQConfig;
	Connection connection;
	Channel channel;

	public RMQQueueReceiver() {
		super(StorageLevel.MEMORY_AND_DISK_2());
	}

	@Override
	public void onStart() {
		new Thread() {
				@Override public void run() {
					try {
						receive();
					} catch (ShutdownSignalException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ConsumerCancelledException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				}.start();
	}

	protected void receive() throws IOException, ShutdownSignalException,
			ConsumerCancelledException, InterruptedException {
		System.out.println("RECEIVING");
		ArrayList<Address> addresses = new ArrayList<Address>();
		// for (int i = 0; i < rabbitMQConfig.hosts.length; ++i) {
		addresses.add(new Address(this.rabbitmqConfig.host,
				this.rabbitmqConfig.port));
		Address[] addrArr = new Address[1];
		addresses.toArray(addrArr);
		factory = new ConnectionFactory();
		factory.setVirtualHost(this.rabbitmqConfig.virtualHost);
		factory.setPort(this.rabbitmqConfig.port);
		factory.setUsername(this.rabbitmqConfig.username);
		factory.setPassword(this.rabbitmqConfig.password);
	//	factory.setHost(hostname);
		connection = factory.newConnection(addrArr);
		channel = connection.createChannel();
		channel.queueDeclare(Topic.LOGS.name().toLowerCase(), false, false, false, null);
		 System.out.println("DECLARE CHANNEL 3: exchange:");
		consumer = new QueueingConsumer(channel);
		channel.basicConsume(Topic.LOGS.name().toLowerCase(), true, consumer);
		while (!Thread.currentThread().isInterrupted()) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			String message = new String(delivery.getBody());
			store(message);
			System.out.println("STORED:"+message);
		
		}
	}

	@Override
	public void onStop() {
	}
}
