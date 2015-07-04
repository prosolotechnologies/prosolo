package org.prosolo.common.domainmodel.user.preferences;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;

import org.prosolo.common.domainmodel.user.TimeFrame;
import org.prosolo.common.domainmodel.user.preferences.UserPreference;

/**
 *
 * @author Nikola Milikic
 *
 */

@Entity
public class EmailPreferences extends UserPreference {
	
	private static final long serialVersionUID = 5199841817734359421L;
	
	private Collection<TimeFrame> notificationFrequency;
	
	public EmailPreferences() {
		notificationFrequency = new ArrayList<TimeFrame>();
	}

	/**
	 * @return the notificationFrequency
	 */
	@ElementCollection(targetClass = TimeFrame.class) 
	@Column(name="notificationFrequency")
	@CollectionTable(name = "user_EmailPreferences_frequency", joinColumns = @JoinColumn(name = "app_id"))
	@Enumerated(EnumType.STRING)
	public Collection<TimeFrame> getNotificationFrequency() {
		return notificationFrequency;
	}

	/**
	 * @param notificationFrequency the notificationFrequency to set
	 */
	public void setNotificationFrequency(Collection<TimeFrame> notificationFrequency) {
		if (null != notificationFrequency) {
			this.notificationFrequency = notificationFrequency;
		} 
	}
	
	/**
	 * 
	 * @param tf
	 */
	public void addNotificationFrequency(TimeFrame tf) {
		if ( tf != null && !getNotificationFrequency().contains(tf) )
			getNotificationFrequency().add(tf);
		else
			System.err.println("Did not succeed in adding new notification frequency.");
	}
	
	/**
	 * 
	 * @param tf
	 */
	public void removeNotificationFrequency(TimeFrame tf) {
		if ( tf != null && getNotificationFrequency().contains(tf) )
			getNotificationFrequency().remove(tf);
		else
			System.err.println("Did not succeed in removing new notification frequency.");
	}

}
