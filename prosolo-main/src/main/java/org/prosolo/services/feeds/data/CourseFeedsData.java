package org.prosolo.services.feeds.data;

public class CourseFeedsData {
	
	private String userFullName;
	private long id;
	private String feedLink;
	private boolean included;
	
	public CourseFeedsData() {
		
	}
	
	public CourseFeedsData(String firstName, String lastName, long id, String feed, boolean included) {
		setFullName(firstName, lastName);
		this.id = id;
		this.feedLink = feed;
		this.included = included;
	}
	
	public void setFullName(String name, String lastName) {
		this.userFullName = name + (lastName != null ? " " + lastName : "");
	}

	public String getUserFullName() {
		return userFullName;
	}

	public void setUserFullName(String userFullName) {
		this.userFullName = userFullName;
	}

	public String getFeedLink() {
		return feedLink;
	}

	public void setFeedLink(String feedLink) {
		this.feedLink = feedLink;
	}

	public boolean isIncluded() {
		return included;
	}

	public void setIncluded(boolean included) {
		this.included = included;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
}
