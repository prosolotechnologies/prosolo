package org.prosolo.bigdata.common.dal.pojo;

public class UserEventDailyCount {

	private long user;

	private String type;

	private long count;

	private long date;

	public long getUser() {
		return user;
	}

	public void setUser(long user) {
		this.user = user;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public UserEventDailyCount(long user, String type, long date, long count) {
		this.user = user;
		this.type = type;
		this.date = date;
		this.count = count;
	}

}
