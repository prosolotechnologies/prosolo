package org.prosolo.bigdata.common.dal.pojo;

public class TwitterHashtagWeeklyAverage implements Comparable<TwitterHashtagWeeklyAverage> {

	private String hashtag;

	private long week;

	private double average;

	public String getHashtag() {
		return hashtag;
	}

	public void setHashtag(String hashtag) {
		this.hashtag = hashtag;
	}

	public long getWeek() {
		return week;
	}

	public void setWeek(long week) {
		this.week = week;
	}

	public double getAverage() {
		return average;
	}

	public void setAverage(double average) {
		this.average = average;
	}

	public TwitterHashtagWeeklyAverage(String hashtag, long week, double average) {
		this.hashtag = hashtag;
		this.week = week;
		this.average = average;
	}

	@Override
	public int compareTo(TwitterHashtagWeeklyAverage that) {
		if (this.week == that.week) {
			if (this.average == that.average) {
				return  -hashtag.compareTo(that.hashtag);
			} else {
				return average < that.average ? -1 : 1;
			}
		} else {
			return this.week < that.week ? -1 : 1;
		}
	}
	
}