package org.prosolo.common.domainmodel.lti;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.prosolo.common.domainmodel.user.User;

@Entity
public class LtiUser extends BaseLtiEntity {

	private static final long serialVersionUID = 8711140105168832463L;
	
	
	private String userId;
	private String name;
	private String email;
	private LtiConsumer consumer;
	private User user;

	public LtiUser(){

	}
	
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public LtiConsumer getConsumer() {
		return consumer;
	}

	public void setConsumer(LtiConsumer consumer) {
		this.consumer = consumer;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	

}