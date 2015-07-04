package org.prosolo.services.messaging.rabbitmq.impl;

/**
@author Zoran Jeremic Sep 7, 2014
 */

public class DataItem {
	private String data;
	private long id;
	//private String queue;
	
	public DataItem(String data, long id) {
		this.id = id;
		this.data = data;
		//this.queue=queue;
	}

	public String getData() {
		return data;
	}

	public long getId() {
		return id;
	}

//	public String getQueue() {
//		return queue;
//	}
//
//	public void setQueue(String queue) {
//		this.queue = queue;
//	}
}
