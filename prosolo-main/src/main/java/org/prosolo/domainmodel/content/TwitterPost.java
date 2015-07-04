package org.prosolo.domainmodel.content;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;

import org.prosolo.domainmodel.annotation.Tag;
import org.prosolo.domainmodel.content.Post;

@Entity
public class TwitterPost extends Post {

	private static final long serialVersionUID = 7606987253017367231L;

	private Set<Tag> hashtags;
	
	private long tweetId;
	private String creatorName;
	private String screenName;
	private String userUrl;
	private String profileImage;
	
	public TwitterPost() {
		hashtags = new HashSet<Tag>();
	}
	
	@ManyToMany(fetch = FetchType.LAZY)
	public Set<Tag> getHashtags() {
		return hashtags;
	}

	public void setHashtags(Set<Tag> hashtags) {
		this.hashtags = hashtags;
	}

	public long getTweetId() {
		return tweetId;
	}

	public void setTweetId(long tweetId) {
		this.tweetId = tweetId;
	}

	public String getCreatorName() {
		return creatorName;
	}

	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getUserUrl() {
		return userUrl;
	}

	public void setUserUrl(String userUrl) {
		this.userUrl = userUrl;
	}

	public String getProfileImage() {
		return profileImage;
	}

	public void setProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}
	
}
