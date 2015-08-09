package org.prosolo.bigdata.common.dal.pojo;

public class RegisteredUsersCount {
	
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

	public RegisteredUsersCount(String type, int count, long date) {
		this.type = type;
		this.count = count;
		this.date = date;
	}	

}
