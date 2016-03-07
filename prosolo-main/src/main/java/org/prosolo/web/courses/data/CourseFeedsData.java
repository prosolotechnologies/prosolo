package org.prosolo.web.courses.data;

public class CourseFeedsData {
	
	private String userFullName;
	private String feedLink;
	private boolean included;
	
	public CourseFeedsData() {
		
	}
	
	public CourseFeedsData(String firstName, String lastName, String feed, boolean included) {
		setFullName(firstName, lastName);
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
	
}
