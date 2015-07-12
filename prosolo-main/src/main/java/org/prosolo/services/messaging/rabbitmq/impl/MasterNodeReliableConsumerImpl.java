package org.prosolo.services.messaging.rabbitmq.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.services.messaging.rabbitmq.MasterNodeReliableConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * @author Zoran Jeremic Oct 17, 2014
 *
 */
@Service("org.prosolo.services.messaging.rabbitmq.MasterNodeReliableConsumer")
public class MasterNodeReliableConsumerImpl  extends ReliableClientImpl implements MasterNodeReliableConsumer {
	private static Logger logger = Logger.getLogger(MasterNodeReliableConsumer.class);
	 long lastItem;
	  Set<Long> moreReceivedItems;

	  @Autowired private MasterNodeMessageWorker worker;
	  private ExecutorService exService;
	  @Override
	  public void init(){
		  System.out.println("Initializing master node worker");
		  if(rabbitmqConfig.distributed){
			  this.StartAsynchronousConsumer();
		  }
	  }
	  public MasterNodeReliableConsumerImpl() {
	    super();
	    System.out.println("Constructor for master node worker");
	    lastItem = 0;
	    moreReceivedItems = new HashSet<Long>();
	  }

	  @Override
	  protected void waitForConnection() throws InterruptedException {
	    super.waitForConnection();
	    try {
	      channel.basicConsume(rabbitmqConfig.queuePrefix+rabbitmqConfig.queue+CommonSettings.getInstance().config.getNamespaceSufix(), false, new Consumer() {

	        @Override
	        public void handleCancel(String consumerTag) throws IOException {

	        }

	        @Override
	        public void handleCancelOk(String consumerTag) {

	        }

	        @Override
	        public void handleConsumeOk(String consumerTag) {

	        }

	        @Override
	        public void handleDelivery(String consumerTag, Envelope envelope,
	            BasicProperties properties, byte[] body) throws IOException {

	          long messageId = Long.parseLong(properties.getMessageId());
	          if (worker != null) {
	            // if the message is not a re-delivery, sure it is not a
	            // retransmission
	            if (!envelope.isRedeliver() || toBeWorked(messageId)) {
	              try {
	                worker.handle(new String(body));
	                // the message is ack'ed just after it has been
	                // secured (handled, stored in database...)
	                setAsWorked(messageId);
	                channel.basicAck(envelope.getDeliveryTag(), false);
	              } catch (WorkerException e) {
	                // the message worker has reported an exception,
	                // so the message
	                // can not be considered to be handled properly,
	                // so requeue it
	                channel.basicReject(envelope.getDeliveryTag(), true);
	              }
	            }
	          }
	        }

	        @Override
	        public void handleRecoverOk(String consumerTag) {
	          }

	        @Override
	        public void handleShutdownSignal(String consumerTag,
	            ShutdownSignalException cause) {
	        	logger.info("Handing shutdown signal");
        		try {
					Thread.sleep(2000);
				   waitForConnection();
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	        }

	      });
	    } catch (IOException e) {
	      e.printStackTrace();
	    }

	  }

	  protected void setAsWorked(Long messageId) {
	    synchronized (moreReceivedItems) {
	      if (lastItem + 1 == messageId) {
	        lastItem = messageId;
	      } else {
	        moreReceivedItems.add(messageId);
	        while (moreReceivedItems.contains(lastItem + 1)) {
	          lastItem++;
	          moreReceivedItems.remove(lastItem);
	        }
	      }
	    }
	  }

	  protected boolean toBeWorked(Long messageId) {
	    synchronized (moreReceivedItems) {
	      return messageId > lastItem && !moreReceivedItems.contains(messageId);
	    }
	  }

	  /* (non-Javadoc)
	 * @see org.prosolo.services.messaging.rabbitmq.impl.ReliableConsumer#StartAsynchronousConsumer()
	 */
	@Override
	public void StartAsynchronousConsumer() {
	    exService = Executors.newSingleThreadExecutor();
	    exService.execute(new Runnable() {

	      @Override
	      public void run() {
	        try {
	        	System.out.println("Started Master Node consumer");
	          for (;;) {
	            waitForConnection();
	            synchronized (this) {
	              // this is very simple: reconnect every 5 seconds always. This
	              // could impact negatively
	              // the performance. More sophisticated approach would be,
	              // reconnect if no messages have
	              // been received for 1 second. Reconnect always after say 5
	              // minutes.
	              this.wait(5000);
	            }
	            disconnect();
	          }
	        } catch (InterruptedException ex) {
	          // disconnect and exit
	          disconnect();
	        }
	      }
	    });
	  }

	  /* (non-Javadoc)
	 * @see org.prosolo.services.messaging.rabbitmq.impl.ReliableConsumer#StopAsynchronousConsumer()
	 */
	@Override
	public void StopAsynchronousConsumer() {
	    exService.shutdownNow();
	  }
}
