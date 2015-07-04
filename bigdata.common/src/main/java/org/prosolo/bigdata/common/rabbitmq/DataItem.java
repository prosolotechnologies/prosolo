package org.prosolo.bigdata.common.rabbitmq;
/**
@author Zoran Jeremic Apr 3, 2015
 *
 */

public class DataItem {
	private String data;
	private long id;

	public DataItem(String data, long id) {
		this.id = id;
		this.data = data;
	}

	public String getData() {
		return this.data;
	}

	public long getId() {
		return this.id;
	}
}

