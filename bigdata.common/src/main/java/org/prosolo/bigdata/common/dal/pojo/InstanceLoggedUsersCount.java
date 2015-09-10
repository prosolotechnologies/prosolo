package org.prosolo.bigdata.common.dal.pojo;

public class InstanceLoggedUsersCount {
	
	private String instance;
	
	private long timestamp;
	
	private long count;

	public String getInstance() {
		return instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public InstanceLoggedUsersCount(String instance, long timestamp, long count) {
		super();
		this.instance = instance;
		this.timestamp = timestamp;
		this.count = count;
	}

}
