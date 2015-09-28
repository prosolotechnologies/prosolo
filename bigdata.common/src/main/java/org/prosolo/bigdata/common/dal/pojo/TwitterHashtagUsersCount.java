package org.prosolo.bigdata.common.dal.pojo;

public class TwitterHashtagUsersCount {

	private String hashtag;

	private long users;

	public String getHashtag() {
		return hashtag;
	}

	public void setHashtag(String hashtag) {
		this.hashtag = hashtag;
	}

	public long getUsers() {
		return users;
	}

	public void setUsers(long users) {
		this.users = users;
	}

	public TwitterHashtagUsersCount(String hashtag, long users) {
		this.hashtag = hashtag;
		this.users = users;
	}

}