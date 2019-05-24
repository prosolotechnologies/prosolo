package org.prosolo.common.messaging.rabbitmq.impl;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.log4j.Logger;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.config.RabbitMQConfig;
import org.prosolo.common.messaging.rabbitmq.ReliableClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

//import org.prosolo.app.Settings;

/**
 @author Zoran Jeremic Sep 7, 2014
 */

public class ReliableClientImpl implements ReliableClient {
	private static Logger logger = Logger.getLogger(ReliableClient.class);
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
			addrArr[0]=addresses.get(0);
			//addresses.toArray(addrArr);
			try {
				// factory.setHost("127.0.0.1");
				factory.setHost(this.rabbitmqConfig.host);
				factory.setVirtualHost(this.rabbitmqConfig.virtualHost);
				factory.setPort(this.rabbitmqConfig.port);
				factory.setUsername(this.rabbitmqConfig.username);
				factory.setPassword(this.rabbitmqConfig.password);
				factory.setRequestedHeartbeat(7);
				this.connection = factory.newConnection();
				this.channel = this.connection.createChannel();

				this.channel.queueDeclare(this.queue,
						this.rabbitmqConfig.durableQueue,
						this.rabbitmqConfig.exclusiveQueue,
						this.rabbitmqConfig.autodeleteQueue, null);

				logger.debug("DECLARE CHANNEL: queue:"+this.queue+" routing key:"+this.rabbitmqConfig.routingKey+" durable:"+this.rabbitmqConfig.durableQueue
						+" exclusive:"+this.rabbitmqConfig.exclusiveQueue+" autodelete:"+this.rabbitmqConfig.autodeleteQueue);
				return;
			} catch (Exception e) {
				e.printStackTrace();
				// ignore errors. In a production case, it is important to
				// handle different kind of errors and give the application
				// some hint on what to do in case it is not possible to
				// connect after some timeouts, properly notifying persistent
				// errors
				logger.debug("Disconnect_3");
				this.disconnect();
				Thread.sleep(1000);
			}
		}
	}

	protected void disconnect() {
		logger.debug("Disconnecting connection");
		try {
			if (this.channel != null && this.channel.isOpen()) {
				this.channel.close();
				this.channel = null;
			}

			if (this.connection != null && this.connection.isOpen()) {
				this.connection.close();
				this.connection = null;
			}
		}catch(TimeoutException te){
			logger.error("Error", te);
		}

		catch (IOException e) {
			logger.error("error", e);
		}
	}

	@Override
	public String getQueue() {
		return this.queue;
	}

	@Override
	public void setQueue(String queue) {
		this.queue = CommonSettings.getInstance().config.rabbitMQConfig.queuePrefix+queue+CommonSettings.getInstance().config.getNamespaceSufix();
	}

	public boolean isConnected() {
		return connection != null && connection.isOpen();
	}

}
