package org.prosolo.bigdata.common.dal.pojo;

public class TwitterHashtagWeeklyAverage implements Comparable<TwitterHashtagWeeklyAverage> {

	private long day;

	private String hashtag;

	private double average;
	
	public long getDay() {
		return day;
	}

	public void setDay(long day) {
		this.day = day;
	}

	public String getHashtag() {
		return hashtag;
	}
	
	public void setHashtag(String hashtag) {
		this.hashtag = hashtag;
	}
	
	public double getAverage() {
		return average;
	}

	public void setAverage(double average) {
		this.average = average;
	}

	public TwitterHashtagWeeklyAverage(long day, String hashtag, double average) {
		this.hashtag = hashtag;
		this.day = day;
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