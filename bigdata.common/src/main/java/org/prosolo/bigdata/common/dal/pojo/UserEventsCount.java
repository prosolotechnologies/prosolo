package org.prosolo.bigdata.common.dal.pojo;

public class UserEventsCount {
	
	private String type;
	
	private int count;
	
	private long date;
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public UserEventsCount(String type, long date, int count) {
		this.type = type;
		this.date = date;
		this.count = count;
	}	

}
