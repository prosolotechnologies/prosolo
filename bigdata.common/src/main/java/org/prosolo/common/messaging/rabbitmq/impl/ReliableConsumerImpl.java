package org.prosolo.common.messaging.rabbitmq.impl;


import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.prosolo.common.messaging.rabbitmq.MessageWorker;
import org.prosolo.common.messaging.rabbitmq.ReliableConsumer;
import org.prosolo.common.messaging.rabbitmq.WorkerException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
/**
@author Zoran Jeremic Apr 3, 2015
 *
 */

public class ReliableConsumerImpl extends ReliableClientImpl implements ReliableConsumer{
	private final static Logger logger = Logger
			.getLogger(ReliableConsumerImpl.class);
	long lastItem;
	long latestMessageTime=0;
	int counter=0;
	Set<Long> moreReceivedItems;
	// MessageWorker worker;
	private MessageWorker worker;
	private ExecutorService exService;
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
		super();
		this.lastItem = 0;
		this.moreReceivedItems = new HashSet<Long>();
	}

	@Override
	protected void waitForConnection() throws InterruptedException {
		super.waitForConnection();
		try {
			this.channel.basicConsume(this.queue, true, new Consumer() {

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
					long messageId = 0;
					if (properties.getMessageId() != null) {
						messageId = Long.parseLong(properties.getMessageId());
						if (ReliableConsumerImpl.this.worker != null) {
							// if the message is not a re-delivery, sure it is
							// not a
							// retransmission
									
							if (!envelope.isRedeliver()
									|| ReliableConsumerImpl.this
											.toBeWorked(messageId)) {
								try {
									counter++;
									latestMessageTime=System.currentTimeMillis();
									ReliableConsumerImpl.this.worker
											.handle(new String(body));
									
									// the message is ack'ed just after it has
									// been
									// secured (handled, stored in database...)
									ReliableConsumerImpl.this
											.setAsWorked(messageId);
									//ReliableConsumer.this.channel.basicAck(
										//	envelope.getDeliveryTag(), false);
								} catch (WorkerException e) {
									// the message worker has reported an
									// exception,
									// so the message
									// can not be considered to be handled
									// properly,
									// so requeue it
										e.printStackTrace();
								ReliableConsumerImpl.this.channel.basicReject(
											envelope.getDeliveryTag(), true);
								}catch(Exception ex){
												logger.error("EXCEPTION WITH MESSAGE:"+envelope.getDeliveryTag(),ex);
									ReliableConsumerImpl.this.channel.basicReject(
											envelope.getDeliveryTag(), true);
								}
							}
						}
					}
				}

				@Override
				public void handleRecoverOk(String consumerTag) {
					logger.debug("got recoverOK signal");
				}

				@Override
				public void handleShutdownSignal(String consumerTag,
						ShutdownSignalException cause) {
					 logger.trace("got shutdown signal");

				}

			});
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	protected void setAsWorked(Long messageId) {
		synchronized (this.moreReceivedItems) {
			if (this.lastItem + 1 == messageId) {
				this.lastItem = messageId;
			} else {
				this.moreReceivedItems.add(messageId);
				while (this.moreReceivedItems.contains(this.lastItem + 1)) {
					this.lastItem++;
					this.moreReceivedItems.remove(this.lastItem);
				}
			}
		}
	}

	protected boolean toBeWorked(Long messageId) {
		synchronized (this.moreReceivedItems) {
			return messageId > this.lastItem
					&& !this.moreReceivedItems.contains(messageId);
		}
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
		this.exService.execute(new Runnable() {

			@Override
			public void run() {
				try {
					for (;;) {
						ReliableConsumerImpl.this.waitForConnection();
						synchronized (this) {
							// this is very simple: reconnect every 5 seconds
							// always. This
							// could impact negatively
							// the performance. More sophisticated approach
							// would be,
							// reconnect if no messages have
							// been received for 1 second. Reconnect always
							// after say 5
							// minutes.
							this.wait(5000);
							long passedTime=System.currentTimeMillis()-latestMessageTime;
							if(passedTime>2000){
								ReliableConsumerImpl.this.disconnect();
							}
							
						}
					
					}
				} catch (InterruptedException ex) {
					// disconnect and exit
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

	public void StopAsynchronousConsumer() {
		this.exService.shutdownNow();
	}
}

