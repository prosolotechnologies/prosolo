package org.prosolo.common.domainmodel.user.oauth;

import javax.persistence.*;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.oauth.OpenIDProvider;

/**
 * 
 * @author Zoran Jeremic, Aug 9, 2014
 * 
 */
@Entity
public class OpenIDAccount extends BaseEntity {

	private static final long serialVersionUID = 8305738484620825563L;

	private User user;
	private String validatedId;
	private OpenIDProvider openIDProvider;

	public String getValidatedId() {
		return validatedId;
	}

	public void setValidatedId(String validatedId) {
		this.validatedId = validatedId;
	}

	@OneToOne(fetch = FetchType.LAZY)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Enumerated
	@Column(nullable = false)
	public OpenIDProvider getOpenIDProvider() {
		return openIDProvider;
	}

	public void setOpenIDProvider(OpenIDProvider openIDProvider) {
		this.openIDProvider = openIDProvider;
	}
}
