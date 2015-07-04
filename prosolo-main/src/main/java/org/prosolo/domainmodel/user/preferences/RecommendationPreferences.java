package org.prosolo.domainmodel.user.preferences;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

import org.prosolo.domainmodel.user.TimeFrame;
import org.prosolo.domainmodel.user.UserDefinedPriority;
import org.prosolo.domainmodel.user.UserPriorityType;
import org.prosolo.domainmodel.user.preferences.UserPreference;

@Entity
public class RecommendationPreferences extends UserPreference {

	private static final long serialVersionUID = -6862674564470870706L;
	
	private TimeFrame timeFrame;
	private Collection<UserDefinedPriority> userPriorities;
	
	public RecommendationPreferences() {
		setUserPriorities(new ArrayList<UserDefinedPriority>());
	}
	
	@Enumerated(EnumType.STRING)
	public TimeFrame getTimeFrame() {
		return timeFrame;
	}

	public void setTimeFrame(TimeFrame timeFrame) {
		this.timeFrame = timeFrame;
	}

	/**
	 * @param userPriorities the userPriorities to set
	 */
	public void setUserPriorities(Collection<UserDefinedPriority> userPriorities) {
		this.userPriorities = userPriorities;
	}

	/**
	 * @return the userPriorities
	 */
	@OneToMany
	@JoinTable(name="user_RecommendationPreferences_userPriorities")
	public Collection<UserDefinedPriority> getUserPriorities() {
		return userPriorities;
	}
	
	public void addUserPriority(UserDefinedPriority priority) {
		if ( priority != null ) 
			getUserPriorities().add(priority);
	}
	
	public UserDefinedPriority getPriorityOfType(UserPriorityType priorityType) {
		if (priorityType == null)
			return null;
		
		for (UserDefinedPriority priority : userPriorities) {
			if (priority.getPriorityType() != null)
				if (priority.getPriorityType().equals(priorityType))
				return priority;
		}
		return null;
	}
}
