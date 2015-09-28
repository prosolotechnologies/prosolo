package org.prosolo.bigdata.common.dal.pojo;

public class TwitterHashtagDailyCount {

	private String hashtag;

	private long date;

	private long count;

	public String getHashtag() {
		return hashtag;
	}

	public void setHashtag(String hashtag) {
		this.hashtag = hashtag;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public TwitterHashtagDailyCount(String hashtag, long date, long count) {
		this.hashtag = hashtag;
		this.date = date;
		this.count = count;
	}
	
}

