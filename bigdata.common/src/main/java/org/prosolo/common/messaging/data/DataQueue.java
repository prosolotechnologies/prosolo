package org.prosolo.common.messaging.data;

import java.util.ArrayDeque;
import java.util.Queue;
/**
@author Zoran Jeremic Apr 3, 2015
 *
 */

public class DataQueue {
	Queue<DataItem> dataQueue;
	long lastID;

	public DataQueue() {
		this.dataQueue = new ArrayDeque<DataItem>();
		this.lastID = 0;
	}

	// no persistence in this example: in a production system it should also
	// store the lastID in a transactional way, upon the insertion of
	// a new item in the queue, saving the queue itself as well.
	// Furthermore, it should check and limit the maximum number of cached
	// items in dataQueue, to avoid to consume too many resources that would
	// lead the system to become unreliable.
	public synchronized long add(String data) {
		++this.lastID;
		this.dataQueue.add(new DataItem(data, this.lastID));
		return this.lastID;
	}

	// requeue an old item - no new ID is allocated
	public synchronized void add(DataItem item) {
		this.dataQueue.add(item);
	}

	public synchronized boolean isEmpty() {
		return this.dataQueue.isEmpty();
	}

	public synchronized DataItem peek() {
		return this.dataQueue.peek();
	}

	public synchronized DataItem remove() {
		return this.dataQueue.remove();
	}
}

