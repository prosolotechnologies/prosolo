package org.prosolo.bigdata.rabbitmq;

import org.apache.log4j.Logger;
 

import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.config.RabbitMQConfig;

import java.io.IOException;
import java.util.ArrayList;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
@author Zoran Jeremic Apr 3, 2015
 *
 */

public class ReliableClient {
	private final static Logger logger = Logger
			.getLogger(ReliableClient.class);
	RabbitMQConfig rabbitmqConfig = CommonSettings.getInstance().config.rabbitMQConfig;
	protected Connection connection;
	protected Channel channel;
	protected String queue;

	protected void waitForConnection() throws InterruptedException {
		while (true) {
			ConnectionFactory factory = new ConnectionFactory();
			ArrayList<Address> addresses = new ArrayList<Address>();
			// for (int i = 0; i < rabbitMQConfig.hosts.length; ++i) {
			addresses.add(new Address(this.rabbitmqConfig.host,
					this.rabbitmqConfig.port));
			// }

			// randomize the order used to try the servers: distribute their
			// usage
			// Collections.shuffle(addresses);
			Address[] addrArr = new Address[1];
			addresses.toArray(addrArr);

			try {
				// factory.setHost("127.0.0.1");
				factory.setVirtualHost(this.rabbitmqConfig.virtualHost);
				factory.setPort(this.rabbitmqConfig.port);
				factory.setUsername(this.rabbitmqConfig.username);
				factory.setPassword(this.rabbitmqConfig.password);
				this.connection = factory.newConnection(addrArr);

				this.channel = this.connection.createChannel();
				String exchange=this.queue;
				//String exchange=this.rabbitmqConfig.exchange;
				this.channel.exchangeDeclare(exchange,
						"direct", false);
				// Map<String, Object> args = new HashMap<String, Object>();
				// args.put("x-message-ttl", rabbitmqConfig.exchange);
				this.channel.queueDeclare(this.queue,
						this.rabbitmqConfig.durableQueue,
						this.rabbitmqConfig.exclusiveQueue,
						this.rabbitmqConfig.autodeleteQueue, null);
				logger.trace("DECLARE CHANNEL: exchange:"+exchange+" queue:"+this.queue+" routing key:"+this.rabbitmqConfig.routingKey+" durable:"+this.rabbitmqConfig.durableQueue
						 +" exclusive:"+this.rabbitmqConfig.exclusiveQueue+" autodelete:"+this.rabbitmqConfig.autodeleteQueue);
				this.channel.queueBind(this.queue,
						exchange,
						this.rabbitmqConfig.routingKey);
						//this.rabbitmqConfig.routingKey+" "+this.queue);
				return;
			} catch (Exception e) {
				e.printStackTrace();
				// ignore errors. In a production case, it is important to
				// handle different kind of errors and give the application
				// some hint on what to do in case it is not possible to
				// connect after some timeouts, properly notifying persistent
				// errors
		
				this.disconnect();
				Thread.sleep(1000);
			}
		}
	}

	protected void disconnect() {
		try {
			if (this.channel != null && this.channel.isOpen()) {
				this.channel.close();
				this.channel = null;
			}

			if (this.connection != null && this.connection.isOpen()) {
				this.connection.close();
				this.connection = null;
			}
		} catch (IOException e) {
			// just ignore
			e.printStackTrace();
		}
	}

	public String getQueue() {
		return this.queue;
	}

	public void setQueue(String queue) {
		this.queue = CommonSettings.getInstance().config.rabbitMQConfig.queuePrefix+queue+CommonSettings.getInstance().config.getNamespaceSufix();
	}

}

