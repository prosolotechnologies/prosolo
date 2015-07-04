/**
 * 
 */
package org.prosolo.web.activitywall.data;

import java.util.List;

import org.prosolo.domainmodel.activities.ExternalToolActivity;
import org.prosolo.domainmodel.activities.ResourceActivity;
import org.prosolo.domainmodel.activities.TargetActivity;
import org.prosolo.domainmodel.activities.UploadAssignmentActivity;
import org.prosolo.domainmodel.outcomes.Outcome;
import org.prosolo.domainmodel.outcomes.SimpleOutcome;
import org.prosolo.services.activityWall.impl.data.SocialActivityData;
import org.prosolo.util.date.DateUtil;
import org.prosolo.web.competences.data.ActivityType;
import org.prosolo.web.goals.cache.ActionDisabledReason;
import org.prosolo.web.goals.cache.CompetenceDataCache;

/**
 * @author "Nikola Milikic"
 * 
 */
public class ActivityWallData extends SocialActivityData implements Comparable<ActivityWallData> {
	
	private static final long serialVersionUID = -3304036709805911648L;

	private long id;
	private boolean completed;
	private boolean mandatory;
	private boolean canBeAdded = true;
	private String dateCompleted;
	private double result = -1;
	private boolean acceptGrades;
	
	private NodeData activity;
	private long position;
	private ActivityType activityType;

	private ActionDisabledReason canNotBeMarkedAsCompleted = ActionDisabledReason.NONE;
	
	private List<ActivityWallData> relatedActivities;
	
	private CompetenceDataCache compData;
	
	public ActivityWallData(TargetActivity targetActivity) {
		this.id = targetActivity.getId();
		setActivity(new NodeData(targetActivity));
		setDateCreated(targetActivity.getDateCreated());
		
		this.activity = new NodeData(targetActivity.getActivity());
		this.mandatory = targetActivity.getActivity().isMandatory();
		
		if (targetActivity.getActivity() instanceof UploadAssignmentActivity) {
			this.canNotBeMarkedAsCompleted = ActionDisabledReason.COMPLETION_DISABLED_FOR_UPLOAD_ACTIVITY;
			this.activityType = ActivityType.ASSIGNMENTUPLOAD;
		} else if (targetActivity.getActivity() instanceof ExternalToolActivity) {
			System.out.println("EXTERNAL TOOL:"+((ExternalToolActivity) targetActivity.getActivity()).isAcceptGrades());

			this.activityType = ActivityType.EXTERNALTOOL;
			if (((ExternalToolActivity) targetActivity.getActivity()).isAcceptGrades()) {
				this.acceptGrades = true;
			}
		} else if (targetActivity.getActivity() instanceof ResourceActivity) {
			this.activityType=ActivityType.RESOURCE;
		}
		
		if (targetActivity.isCompleted() && !targetActivity.getOutcomes().isEmpty()) {
			Outcome outcome = targetActivity.getOutcomes().get(targetActivity.getOutcomes().size() - 1);
			this.setResult(((SimpleOutcome) outcome).getResult());
		}
	}
	
	public ActivityWallData() {}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getWhen() {
		return DateUtil.getTimeAgoFromNow(getDateCreated());
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
	
	public boolean isCanBeAdded() {
		return canBeAdded;
	}

	public void setCanBeAdded(boolean canBeAdded) {
		this.canBeAdded = canBeAdded;
	}

	public String getDateCompleted() {
		return dateCompleted;
	}

	public void setDateCompleted(String dateCompleted) {
		this.dateCompleted = dateCompleted;
	}

	public NodeData getActivity() {
		return activity;
	}

	public void setActivity(NodeData activity) {
		this.activity = activity;
	}

	public long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
	}
	
	public ActionDisabledReason getCanNotBeMarkedAsCompleted() {
		return canNotBeMarkedAsCompleted;
	}

	public void setCanNotBeMarkedAsCompleted(
			ActionDisabledReason canNotBeMarkedAsCompleted) {
		this.canNotBeMarkedAsCompleted = canNotBeMarkedAsCompleted;
	}

	public CompetenceDataCache getCompData() {
		return compData;
	}

	public void setCompData(CompetenceDataCache compData) {
		this.compData = compData;
	}
	
	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}
	
	public double getResult() {
		return result;
	}
	
	public boolean isAcceptGrades() {
		return acceptGrades;
	}
	
	public void setAcceptGrades(boolean acceptGrades) {
		this.acceptGrades = acceptGrades;
	}
	
	public void setResult(double result) {
		this.result = result;
	}
	
	public ActivityType getActivityType() {
		return activityType;
	}
	
	public void setActivityType(ActivityType activityType) {
		this.activityType = activityType;
	}
	
	public String getScore() {
		if (this.acceptGrades && this.result == -1) {
			return "Result not available yet.";
		} else if (this.result > -1) {
			return String.valueOf(this.result * 100) + " %";
		} else
			return null;
	}
	
	public boolean isHaveScore() {
		if (this.acceptGrades) {
			return true;
		} else if (this.result > -1) {
			return true;
		}
		return false;
	}
	
	public boolean isType(String actType) {
		if (actType.equals(this.activityType.name())) {
			return true;
		} else
			return false;
	}
	
	public List<ActivityWallData> getRelatedActivities() {
		return relatedActivities;
	}

	public void setRelatedActivities(List<ActivityWallData> relatedActivities) {
		this.relatedActivities = relatedActivities;
	}

	@Override
	public int compareTo(ActivityWallData o) {
		return Long.valueOf(position).compareTo(o.getPosition());
	}
	
}
