package org.prosolo.bigdata.common.dal.pojo;

public class TwitterHashtagWeeklyAverage implements Comparable<TwitterHashtagWeeklyAverage> {

	private String hashtag;

	private long timestamp;

	private double average;
	
	private boolean disabled;

	public String getHashtag() {
		return hashtag;
	}

	public void setHashtag(String hashtag) {
		this.hashtag = hashtag;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public double getAverage() {
		return average;
	}

	public void setAverage(double average) {
		this.average = average;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public TwitterHashtagWeeklyAverage(String hashtag, long timestamp, double average, boolean disabled) {
		this.hashtag = hashtag;
		this.timestamp = timestamp;
		this.average = average;
		this.disabled = disabled;
	}

	@Override
	public int compareTo(TwitterHashtagWeeklyAverage that) {
		if (this.average == that.average) {
			return -hashtag.compareTo(that.hashtag);
		} else {
			return average < that.average ? -1 : 1;
		}
	}
	
}