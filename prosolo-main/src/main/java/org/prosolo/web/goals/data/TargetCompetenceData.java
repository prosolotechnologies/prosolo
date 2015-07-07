/**
 * 
 */
package org.prosolo.web.goals.data;

import java.io.Serializable;
import java.util.Date;

import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.web.goals.cache.ActionDisabledReason;

/**
 * @author "Nikola Milikic"
 * 
 */
public class TargetCompetenceData implements LastActivityAware, Serializable {

	private static final long serialVersionUID = 5424735656154476246L;

	private long id;
	private long competenceId;
	private String title;
	private ActionDisabledReason shouldNotBeDeleted;
	private ActionDisabledReason canNotBeMarkedAsCompleted;
	private boolean userHasCompetence;
	private VisibilityType visibility;

	// used for learning progress section
	private long targetGoalId;
	private Date lastActivity;

	public TargetCompetenceData(TargetCompetence targetComp) {
		this.id = targetComp.getId();
		this.competenceId = targetComp.getCompetence().getId();
		this.title = targetComp.getTitle();
		this.visibility = targetComp.getVisibility();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getCompetenceId() {
		return competenceId;
	}

	public void setCompetenceId(long competenceId) {
		this.competenceId = competenceId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public ActionDisabledReason getShouldNotBeDeleted() {
		return shouldNotBeDeleted;
	}

	public void setShouldNotBeDeleted(ActionDisabledReason shouldNotBeDeleted) {
		this.shouldNotBeDeleted = shouldNotBeDeleted;
	}

	public ActionDisabledReason getCanNotBeMarkedAsCompleted() {
		return canNotBeMarkedAsCompleted;
	}

	public void setCanNotBeMarkedAsCompleted(
			ActionDisabledReason canNotBeMarkedAsCompleted) {
		this.canNotBeMarkedAsCompleted = canNotBeMarkedAsCompleted;
	}

	public boolean isUserHasCompetence() {
		return userHasCompetence;
	}

	public void setUserHasCompetence(boolean userHasCompetence) {
		this.userHasCompetence = userHasCompetence;
	}
	
	public long getTargetGoalId() {
		return targetGoalId;
	}

	public void setTargetGoalId(long targetGoalId) {
		this.targetGoalId = targetGoalId;
	}

	public Date getLastActivity() {
		return lastActivity;
	}

	public void setLastActivity(Date lastActivity) {
		this.lastActivity = lastActivity;
	}

	public VisibilityType getVisibility() {
		return visibility;
	}

	public void setVisibility(VisibilityType visibility) {
		this.visibility = visibility;
	}
	
}
