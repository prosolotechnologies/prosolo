package org.prosolo.common.domainmodel.activitywall;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.user.UserType;

@Entity
public class TwitterPostSocialActivity1 extends SocialActivity1 {
	
	private static final long serialVersionUID = 97877941855185685L;

	private boolean retweet;
	private String comment;
	private String nickname;
	private String name;
	private String postUrl;
	private String profileUrl;
	private String avatarUrl;
	private UserType userType;
	private Set<Tag> hashtags;

	@Column (name ="twitter_comment")
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	@Column (name ="twitter_poster_nickname")
	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	@Column (name ="twitter_poster_name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Column (name ="twitter_post_url")
	public String getPostUrl() {
		return postUrl;
	}

	public void setPostUrl(String postUrl) {
		this.postUrl = postUrl;
	}
	
	@Column (name ="twitter_poster_profile_url")
	public String getProfileUrl() {
		return profileUrl;
	}

	public void setProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
	}
	
	@Column (name ="twitter_poster_avatar_url")
	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}
	
	@Column (name ="twitter_user_type")
	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}
	
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "social_activity1_hashtags")
	public Set<Tag> getHashtags() {
		return hashtags;
	}

	public void setHashtags(Set<Tag> hashtags) {
		this.hashtags = hashtags;
	}

	@Type(type="true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isRetweet() {
		return retweet;
	}

	public void setRetweet(boolean retweet) {
		this.retweet = retweet;
	}

}
