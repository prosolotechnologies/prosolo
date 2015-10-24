package org.prosolo.bigdata.common.dal.pojo;

public class TwitterHashtagUsersCount {

	private String hashtag;

	private Long users;

	public TwitterHashtagUsersCount(String hashtag, Long users) {
		super();
		this.hashtag = hashtag;
		this.users = users;
	}

	public String getHashtag() {
		return hashtag;
	}

	public void setHashtag(String hashtag) {
		this.hashtag = hashtag;
	}

	public Long getUsers() {
		return users;
	}

	public void setUsers(Long users) {
		this.users = users;
	}

}
