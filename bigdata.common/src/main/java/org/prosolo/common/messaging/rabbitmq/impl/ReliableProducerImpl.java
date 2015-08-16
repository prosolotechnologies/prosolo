package org.prosolo.common.messaging.rabbitmq.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.messaging.data.DataItem;
import org.prosolo.common.messaging.data.DataQueue;
import org.prosolo.common.messaging.rabbitmq.ReliableProducer;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.ConfirmListener;
/**
@author Zoran Jeremic Apr 3, 2015
 *
 */

public class ReliableProducerImpl  extends ReliableClientImpl implements ReliableProducer{
	private DataQueue dataQueue;
	private Map<Long, DataItem> pendingItems;
	private ExecutorService exService;

	public ReliableProducerImpl() {
		super();
		this.dataQueue = new DataQueue();
		this.pendingItems = new HashMap<Long, DataItem>();
	}

	public void init() {
		if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
			this.startAsynchronousPublisher();
		}
	}

	@Override
	protected void waitForConnection() throws InterruptedException {
		super.waitForConnection();
		try {
			this.channel.confirmSelect();
		} catch (IOException e) {
			// should never happen - not important for the example scope
			e.printStackTrace();
		}
		this.channel.addConfirmListener(new ConfirmListener() {

			@Override
			public void handleAck(long deliveryTag, boolean multiple)
					throws IOException {
				if (multiple) {
					ReliableProducerImpl.this.removeItemsUpto(deliveryTag);
				} else {
					ReliableProducerImpl.this.removeItem(deliveryTag);
				}
			}

			@Override
			public void handleNack(long deliveryTag, boolean multiple)
					throws IOException {
				if (multiple) {
					ReliableProducerImpl.this.requeueItemsUpto(deliveryTag);
				} else {
					ReliableProducerImpl.this.requeueItem(deliveryTag);
				}
			}

		});
	}

	protected void requeueItemsUpto(long deliveryTag) {
		synchronized (this.pendingItems) {
			Iterator<Map.Entry<Long, DataItem>> it = this.pendingItems
					.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Long, DataItem> entry = it.next();
				if (entry.getKey() <= deliveryTag) {
					this.dataQueue.add(entry.getValue());
					it.remove();
				}
			}
		}
	}

	protected void removeItemsUpto(long deliveryTag) {
		synchronized (this.pendingItems) {
			Iterator<Map.Entry<Long, DataItem>> it = this.pendingItems
					.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Long, DataItem> entry = it.next();
				if (entry.getKey() <= deliveryTag) {
					it.remove();
				}
			}
		}
	}

	protected void requeueItem(long deliveryTag) {
		synchronized (this.pendingItems) {
			DataItem item = this.pendingItems.get(deliveryTag);
			this.pendingItems.remove(deliveryTag);
			this.dataQueue.add(item);
		}
	}

	protected void removeItem(long deliveryTag) {
		synchronized (this.pendingItems) {
			this.pendingItems.remove(deliveryTag);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.prosolo.services.messaging.rabbitmq.impl.ReliableProducer#send(java
	 * .lang.String)
	 */
	@Override
	public void send(String data) {
		synchronized (this.dataQueue) {

			this.dataQueue.add(data);
			this.dataQueue.notify();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.prosolo.services.messaging.rabbitmq.impl.ReliableProducer#
	 * startAsynchronousPublisher()
	 */
	@Override
	public void startAsynchronousPublisher() {
		this.exService = Executors.newSingleThreadExecutor();
		this.exService.execute(new Runnable() {

			@Override
			public void run() {
				try {
					for (;;) {
						ReliableProducerImpl.this.waitForConnection();
						ReliableProducerImpl.this.publishFromLocalQueue();
						ReliableProducerImpl.this.disconnect();
					}
				} catch (InterruptedException ex) {
					// disconnect and exit
					ReliableProducerImpl.this.disconnect();
				}
			}

		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.prosolo.services.messaging.rabbitmq.impl.ReliableProducer#
	 * stopAsynchronousPublisher()
	 */

	public void stopAsynchronousPublisher() {
		this.exService.shutdownNow();
	}

	protected void publishFromLocalQueue() throws InterruptedException {
		try {
			for (;;) {
				synchronized (this.dataQueue) {
					if (this.dataQueue.isEmpty()) {
						this.dataQueue.wait(1000);
						// if the queue stays empty for more then one second,
						// disconnect and
						// wait offline
						if (this.dataQueue.isEmpty()) {
							this.disconnect();
							this.dataQueue.wait();
							this.waitForConnection();
						}
					}
				}
				DataItem item = this.dataQueue.peek();
				BasicProperties messageProperties = new BasicProperties.Builder()
						.messageId(Long.toString(item.getId())).deliveryMode(2)
						.expiration(this.rabbitmqConfig.messageExpiration)
						.build();
				long deliveryTag = this.channel.getNextPublishSeqNo();
				//System.out.println("PUBLISH:"+item.getData()+" "+this.queue);
				this.channel.basicPublish("", this.queue, messageProperties,
						item.getData().getBytes());
				// only after successfully publishing, move the item to the
				// container of pending items. They will be removed from it only
				// upon the
				// reception of the confirms from the broker.
				synchronized (this.pendingItems) {
					this.pendingItems.put(deliveryTag, item);
				}
				this.dataQueue.remove();
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
			}
		} catch (IOException e) {
			// do nothing: the connection will be closed and then retried
		}
	}
}

