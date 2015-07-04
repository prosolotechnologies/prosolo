package org.prosolo.common.domainmodel.user.notifications;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.user.notifications.Notification;

/**
 * @author Zoran Jeremic Dec 29, 2013
 */
@Entity
@DiscriminatorValue("SActivityNotification")
public class SActivityNotification extends Notification {
	
	private static final long serialVersionUID = -3570393712747791441L;
	
	private SocialActivity socialActivity;
	
	@OneToOne
	public SocialActivity getSocialActivity() {
		return socialActivity;
	}

	public void setSocialActivity(SocialActivity socialActivity) {
		this.socialActivity = socialActivity;
	}
	
	@Transient
	public SocialActivity getObject() {
		return this.socialActivity;
	}
}
