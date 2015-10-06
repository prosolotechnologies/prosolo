package org.prosolo.bigdata.common.dal.pojo;

public class TwitterHashtagWeeklyAverage implements Comparable<TwitterHashtagWeeklyAverage> {

	private String hashtag;

	private long timestamp;

	private double average;
	
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

	public TwitterHashtagWeeklyAverage(String hashtag, long timestamp, double average) {
		this.hashtag = hashtag;
		this.timestamp = timestamp;
		this.average = average;
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