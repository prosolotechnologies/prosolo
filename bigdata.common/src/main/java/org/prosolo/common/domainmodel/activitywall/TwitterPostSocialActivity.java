package org.prosolo.common.domainmodel.activitywall;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.UserType;
import org.prosolo.common.domainmodel.user.socialNetworks.ServiceType;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;

@Entity
public class TwitterPostSocialActivity extends SocialActivity {
	
	private static final long serialVersionUID = -8313801609063501958L;

	private String nickname;
	private String name;
	private ServiceType serviceType;
	private String postUrl;
	private String profileUrl;
	private String avatarUrl;
	private String link;
	private UserType userType;
	
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

	@Column (name ="twitter_post_service_type")
	@Enumerated(EnumType.STRING)
	public ServiceType getServiceType() {
		return serviceType;
	}

	public void setServiceType(ServiceType serviceType) {
		this.serviceType = serviceType;
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

	@Override
	@Transient
	public BaseEntity getObject() {
		return null;
	}

	@Override
	public void setObject(BaseEntity object) {
	}

	@Override
	public void setTarget(BaseEntity object) {
	}

	@Override
	@Transient
	public BaseEntity getTarget() {
		return null;
	}

}
