package org.prosolo.common.messaging.rabbitmq.impl;


import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import org.apache.log4j.Logger;
import org.prosolo.common.messaging.rabbitmq.MessageWorker;
import org.prosolo.common.messaging.rabbitmq.ReliableConsumer;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 @author Zoran Jeremic Apr 3, 2015
 *
 */

public class ReliableConsumerImpl extends ReliableClientImpl implements ReliableConsumer{
	private final static Logger logger = Logger
			.getLogger(ReliableConsumerImpl.class);
	long latestMessageTime=0;
	int maxRetry=3;

	// MessageWorker worker;
	private MessageWorker worker;
	private ExecutorService exService;
	public static final boolean REQUEUE = true;
	public static final boolean DONT_REQUEUE = false;

	//when false, consumer manually sends acknowledge when finished with the task
	private boolean autoAck;


	@Override
	public void setWorker(MessageWorker worker) {
		this.worker = worker;
	}

	/*
	 * public void init(){
	 * if(Settings.getInstance().config.rabbitMQConfig.distributed){
	 * this.StartAsynchronousConsumer(); this.setWorker(new
	 * DefaultMessageWorker()); } }
	 */
	public ReliableConsumerImpl() {
		this(true);
	}

	public ReliableConsumerImpl(boolean autoAck) {
		super();
		this.autoAck = autoAck;
	}

	@Override
	protected void waitForConnection() throws InterruptedException {
		super.waitForConnection();
		try {
			this.channel.basicConsume(this.queue, this.autoAck, new Consumer() {

				@Override
				public void handleCancel(String consumerTag) throws IOException {
					logger.debug("got handleCancel signal");
				}

				@Override
				public void handleCancelOk(String consumerTag) {
					logger.debug("got handleCancelOk signal");
				}

				@Override
				public void handleConsumeOk(String consumerTag) {
				}

				@Override
				public void handleDelivery(String consumerTag,
										   Envelope envelope, BasicProperties properties,
										   byte[] body) throws IOException {
					logger.debug("Handling delivery:"+new String(body));
					try {
						if (ReliableConsumerImpl.this.worker != null) {
							latestMessageTime=System.currentTimeMillis();
							ReliableConsumerImpl.this.worker.handle(new String(body));
						}
						if (!autoAck) {
							ReliableConsumerImpl.this.channel.basicAck(envelope.getDeliveryTag(), false);
						}
					} catch (Exception ex) {
						if (!autoAck) {
							ReliableConsumerImpl.this.channel.basicNack(envelope.getDeliveryTag(), false, true);
						}
						logger.error("Exception in RabbitMQConsumer", ex);
					}
				}

				@Override
				public void handleRecoverOk(String consumerTag) {
					logger.debug("got recoverOK signal");
				}

				@Override
				public void handleShutdownSignal(String consumerTag,
												 ShutdownSignalException cause) {
					logger.debug("got shutdown signal");

				}

			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		//finally{
		//this.disconnect();
		//}

	}
	/*
	 * (non-Javadoc)
	 *
	 * @see org.prosolo.services.messaging.rabbitmq.impl.ReliableConsumer#
	 * StartAsynchronousConsumer()
	 */
	@Override
	public void StartAsynchronousConsumer() {
		this.exService = Executors.newSingleThreadExecutor();
		logger.info("START ASYNCHRONOUS CONSUMER CALLED");
		this.exService.execute(new Runnable() {
			@Override
			public void run() {
				try {
					ReliableConsumerImpl.this.waitForConnection();
				} catch (InterruptedException ex) {
					//alive=false;
					// disconnect and exit
					logger.debug("Disconnect_2");
					ReliableConsumerImpl.this.disconnect();
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.prosolo.services.messaging.rabbitmq.impl.ReliableConsumer#
	 * StopAsynchronousConsumer()
	 */
	@Override
	public void StopAsynchronousConsumer() {
		this.exService.shutdownNow();
	}
}

