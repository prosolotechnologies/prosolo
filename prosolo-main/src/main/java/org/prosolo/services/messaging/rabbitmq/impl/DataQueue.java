package org.prosolo.services.messaging.rabbitmq.impl;

import java.util.ArrayDeque;
import java.util.Queue;

import org.springframework.stereotype.Service;

/**
@author Zoran Jeremic Sep 7, 2014
 */
@Service("org.prosolo.services.messaging.rabbitmq.DataQueue")
public class DataQueue {
	Queue<DataItem> dataQueue;
	long lastID;
	
	public DataQueue() {
		dataQueue = new ArrayDeque<DataItem>();
		lastID = 0;
	}
	
	// no persistence in this example: in a production system it should also 
	// store the lastID in a transactional way, upon the insertion of
	// a new item in the queue, saving the queue itself as well.
	// Furthermore, it should check and limit the maximum number of cached
	// items in dataQueue, to avoid to consume too many resources that would
	// lead the system to become unreliable.
	public synchronized long add(String data) {
		++lastID;
		dataQueue.add(new DataItem(data,lastID));
		return lastID;
	}
	// requeue an old item - no new ID is allocated
	public synchronized void add(DataItem item) {
		dataQueue.add(item);
	}
	
	public synchronized boolean isEmpty() {
		return dataQueue.isEmpty();
	}
	
	public synchronized DataItem peek() {
		return dataQueue.peek();
	}
	
	public synchronized DataItem remove() {
		return dataQueue.remove();
	}
}
