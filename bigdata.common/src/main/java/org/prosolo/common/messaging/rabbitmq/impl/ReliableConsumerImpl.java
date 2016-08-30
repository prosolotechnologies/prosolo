package org.prosolo.common.messaging.rabbitmq.impl;


import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
	int maxRetry=3;

	Set<Long> moreReceivedItems;
	Map<Long,Integer> retriedItems;
	// MessageWorker worker;
	private MessageWorker worker;
	private ExecutorService exService;
	public static final boolean REQUEUE = true;
	public static final boolean DONT_REQUEUE = false;
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
		this.retriedItems=new HashMap<Long,Integer>();
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
					logger.debug("Handling delivery:"+new String(body));
					long messageId = 0;
					if (properties.getMessageId() != null) {
						messageId = Long.parseLong(properties.getMessageId());
						if (ReliableConsumerImpl.this.worker != null) {
							// if the message is not a re-delivery, sure it is
							// not a
							// retransmission
							//System.out.println("Is redeliver:"+envelope.isRedeliver()+" messageId:"+messageId);
							if (!envelope.isRedeliver()
									|| ReliableConsumerImpl.this
											.toBeWorked(messageId)) {
								try {

									latestMessageTime=System.currentTimeMillis();
									ReliableConsumerImpl.this.worker.handle(new String(body));
									
									// the message is ack'ed just after it has
									// been
									// secured (handled, stored in database...)
									ReliableConsumerImpl.this
											.setAsWorked(messageId);
											//ReliableConsumer.this.channel.basicAck(
										//	envelope.getDeliveryTag(), false);
								} catch(Exception ex){
								// ReliableConsumerImpl.this.channel.basicReject(
								// 			envelope.getDeliveryTag(), DONT_REQUEUE);
									logger.error("Exception in RabbitMQConsumer",ex);
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
	System.out.println("START ASYNCHRONOUSE CONSUMER CALLED");
		this.exService.execute(new Runnable() {

			@Override
			public void run() {
			//	boolean alive=true;
				try {
					for (;;) {
					//while(alive){
						ReliableConsumerImpl.this.waitForConnection();
						synchronized (this) {
							// this is very simple: reconnect every 5 seconds
							// always. This
							// could impact negatively
							// the performance. More sophisticated approach
							// would be,
							// reconnect if no messages have
							// been received for 1 second. Reconnect always
							// after say 51
							// minutes.
							this.wait(5000);
							long passedTime=System.currentTimeMillis()-latestMessageTime;
							if(passedTime>2000){
								logger.debug("Disconnect_1");
								ReliableConsumerImpl.this.disconnect();
							}
							
						}
					
					}
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

