package org.prosolo.common.domainmodel.user.oauth;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.socialNetworks.ServiceType;

/**
 * @author Zoran Jeremic 2013-08-04
 *
 */
@Entity
//@Table(name = "user_OauthAccessToken")
public class OauthAccessToken extends BaseEntity {
	
	private static final long serialVersionUID = -543306210225533534L;

	private long userId;
	private String profileName;
	private String profileLink;
	private String token;
	private String tokenSecret;
	private ServiceType service;
	private User user;

	public OauthAccessToken() { }
	
	public OauthAccessToken(String token, String tokenSecret) {
		this.token = token;
		this.tokenSecret = tokenSecret;
	}
	
	public String getProfileLink() {
		return profileLink;
	}

	public void setProfileLink(String profileLink) {
		this.profileLink = profileLink;
	}

	@Column(name = "token", nullable = true)
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Column(name = "tokenSecret", nullable = true)
	public String getTokenSecret() {
		return tokenSecret;
	}

	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret;
	}

	@OneToOne(fetch = FetchType.LAZY)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Enumerated(EnumType.STRING)
	public ServiceType getService() {
		return service;
	}

	public void setService(ServiceType service) {
		this.service = service;
	}

	@Column(name = "profileName", nullable = true)
	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	@Column(name = "userId")
	@Type(type = "long")
	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}
	
}
