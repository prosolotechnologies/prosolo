package org.prosolo.common.domainmodel.activitywall;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

/**
 * @author "Nikola Milikic"
 *
 */
@Entity
public class SocialActivityConfig extends BaseEntity {
	
	private static final long serialVersionUID = -8340671940531797591L;

	private boolean hidden = false;
	private SocialActivity1 socialActivity;
	private User user;
	
	@OneToOne
	@Cascade({CascadeType.MERGE})
	public SocialActivity1 getSocialActivity() {
		return socialActivity;
	}

	public void setSocialActivity(SocialActivity1 socialActivity) {
		this.socialActivity = socialActivity;
	}

	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	@OneToOne
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
