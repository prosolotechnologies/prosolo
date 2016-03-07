/**
 * 
 */
package org.prosolo.web.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.course.CourseEnrollment;
import org.prosolo.common.domainmodel.course.Status;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.organization.Visible;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.nodes.data.activity.attachmentPreview.NodeData;
import org.prosolo.util.nodes.AnnotationUtil;
import org.prosolo.web.activitywall.data.UserDataFactory;
import org.prosolo.web.courses.data.CourseData;
import org.prosolo.web.goals.data.LastActivityAware;

/**
 * @author "Nikola Milikic"
 *
 */
public class GoalData implements Visible, LastActivityAware, Serializable, Comparable<GoalData> {

	private static final long serialVersionUID = 9011003015595088606L;

	protected long goalId;
	protected String title;
	protected String description;
	protected String dateStartedString;
	protected Date dateStarted;
	protected String dateCompletedString;
	protected Date dateCompleted;
	protected int progress;
	protected VisibilityType visibility;
	protected long badgeCount;
	protected long evaluationCount;
	protected long rejectedEvaluationCount;
	
	private long targetGoalId;
	private long completedGoalId;
	private boolean completed;
	private Date deadline;
	private boolean progressActivityDependent;
	private boolean freeToJoin;
	private String tagsString;
	private String hashtagsString;
	private CourseData course;
	private boolean connectedWithCourse;
	private boolean competencesCanNotBeCreated;

	private Collection<Tag> hashtags;
	private Collection<Tag> tags;
	private boolean selected;
	private boolean retaken;
	private Date lastActivity;
	
	protected UserData creator;
	
	public GoalData() { }
	
	public GoalData(LearningGoal goal) {
		this.goalId = goal.getId();
		this.title = goal.getTitle();
		this.description = goal.getDescription();
		this.completed = this.progress == 100;
		this.freeToJoin = goal.isFreeToJoin();
		this.deadline = goal.getDeadline();
		
		this.tags = goal.getTags();
		this.tagsString = AnnotationUtil.getAnnotationsAsSortedCSV(this.tags);
		
		this.hashtags = goal.getHashtags();
		this.hashtagsString = AnnotationUtil.getAnnotationsAsSortedCSV(this.hashtags);
		
		this.retaken = goal.getBasedOn() != null;
	}
	
	public GoalData(TargetLearningGoal targetGoal) {
		this(targetGoal.getLearningGoal());
		
		this.targetGoalId = targetGoal.getId();
		this.progress = targetGoal.getProgress();
		this.completed = this.progress == 100;
		this.progressActivityDependent = targetGoal.isProgressActivityDependent();
		this.visibility = targetGoal.getVisibility();
		
		CourseEnrollment courseEnrollment = targetGoal.getCourseEnrollment();
		
		if (courseEnrollment != null) {
			this.course = new CourseData(courseEnrollment.getCourse());
			this.course.setEnrollment(courseEnrollment);
			
			this.setConnectedWithCourse(!courseEnrollment.getStatus().equals(Status.WITHDRAWN));
			this.competencesCanNotBeCreated = !this.course.isStudentsCanAddNewCompetences();
			this.creator =  UserDataFactory.createUserData(courseEnrollment.getCourse().getMaker());
		} else {
			this.creator = UserDataFactory.createUserData(targetGoal.getLearningGoal().getMaker());
		}
	}
	
	public long getTargetGoalId() {
		return targetGoalId;
	}

	public void setTargetGoalId(long targetGoalId) {
		this.targetGoalId = targetGoalId;
	}

	public Date getDeadline() {
		return deadline;
	}
	
	public void setDeadline(Date deadline) {
		if (deadline != null)
			this.deadline = deadline;
	}
	
	public boolean isProgressActivityDependent() {
		return progressActivityDependent;
	}

	public void setProgressActivityDependent(boolean progressActivityDependent) {
		this.progressActivityDependent = progressActivityDependent;
	}

	public boolean isFreeToJoin() {
		return freeToJoin;
	}

	public void setFreeToJoin(boolean freeToJoin) {
		this.freeToJoin = freeToJoin;
	}
	
	public Collection<Tag> getTags() {
		return tags;
	}

	public void setTags(Collection<Tag> tags) {
		this.tags = tags;
	}

	public String getTagsString() {
		return tagsString;
	}

	public void setTagsString(String tagsString) {
		this.tagsString = tagsString;
	}
	public String getHashtagsString() {
		return hashtagsString;
	}

	public void setHashtagsString(String hashtagsString) {
		this.hashtagsString = hashtagsString;
	}

	public Collection<Tag> getHashtags() {
		return hashtags;
	}

	public void setHashtags(Collection<Tag> hashtags) {
		this.hashtags = hashtags;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
	
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isRetaken() {
		return retaken;
	}

	public void setRetaken(boolean retaken) {
		this.retaken = retaken;
	}

	public CourseData getCourse() {
		return course;
	}

	public void setCourse(CourseData course) {
		this.course = course;
	}
	
	public String getPrettyDeadline() {
		if (deadline != null) {
			return DateUtil.getPrettyDate(deadline);
		} else {
			return "";
		}
	}
	
	public boolean isConnectedWithCourse() {
		return connectedWithCourse;
	}

	public void setConnectedWithCourse(boolean connectedWithCourse) {
		this.connectedWithCourse = connectedWithCourse;
	}

	public boolean isCompetencesCanNotBeCreated() {
		return competencesCanNotBeCreated;
	}

	public void setCompetencesCanNotBeCreated(boolean competencesCanNotBeCreated) {
		this.competencesCanNotBeCreated = competencesCanNotBeCreated;
	}
	
	public NodeData getAsNodeData() {
		NodeData nodeData = new NodeData();
		nodeData.setId(goalId);
		nodeData.setClazz(LearningGoal.class);
		return nodeData;
	}

	public void resetAllFields() {
		this.title = "";
		this.description = "";
		this.progress = 0;
		this.completed = false;
		this.creator = null;
		this.deadline = null;
		this.tagsString = "";
		this.tags = null;
		this.hashtags=null;
		this.hashtagsString="";
	}
	
	public long getGoalId() {
		return goalId;
	}

	public void setGoalId(long goalId) {
		this.goalId = goalId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDateStartedString() {
		return dateStartedString;
	}

	public void setDateStartedString(String dateStartedString) {
		this.dateStartedString = dateStartedString;
	}

	public Date getDateStarted() {
		return dateStarted;
	}

	public void setDateStarted(Date dateStarted) {
		this.dateStarted = dateStarted;
	}

	public String getDateCompletedString() {
		return dateCompletedString;
	}

	public void setDateCompletedString(String dateCompletedString) {
		this.dateCompletedString = dateCompletedString;
	}

	public Date getDateCompleted() {
		return dateCompleted;
	}

	public void setDateCompleted(Date dateCompleted) {
		this.dateCompleted = dateCompleted;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public VisibilityType getVisibility() {
		return visibility;
	}

	public void setVisibility(VisibilityType visibility) {
		this.visibility = visibility;
	}
	
	public long getBadgeCount() {
		return badgeCount;
	}

	public void setBadgeCount(long badgeCount) {
		this.badgeCount = badgeCount;
	}
	
	public long getEvaluationCount() {
		return evaluationCount;
	}

	public void setEvaluationCount(long evaluationCount) {
		this.evaluationCount = evaluationCount;
	}
	
	public long getRejectedEvaluationCount() {
		return rejectedEvaluationCount;
	}

	public void setRejectedEvaluationCount(long rejectedEvaluationCount) {
		this.rejectedEvaluationCount = rejectedEvaluationCount;
	}

	public Date getLastActivity() {
		return lastActivity;
	}

	public void setLastActivity(Date lastActivity) {
		this.lastActivity = lastActivity;
	}

	public UserData getCreator() {
		return creator;
	}

	public void setCreator(UserData creator) {
		this.creator = creator;
	}

	public long getCompletedGoalId() {
		return completedGoalId;
	}

	public void setCompletedGoalId(long completedGoalId) {
		this.completedGoalId = completedGoalId;
	}

	@Override
	public String toString() {
		return "GoalData [goalId=" + goalId + ", title=" + title
				+ ", description=" + description + ", dateStartedString="
				+ dateStartedString + ", dateCompletedString="
				+ dateCompletedString + ", progress=" + progress
				+ ", visibility=" + visibility + ", badgeCount=" + badgeCount
				+ ", evaluationCount=" + evaluationCount + ", targetGoalId="
				+ targetGoalId + ", completedGoalId=" + completedGoalId
				+ ", completed=" + completed + ", deadline=" + deadline
				+ ", progressActivityDependent=" + progressActivityDependent
				+ ", freeToJoin=" + freeToJoin + ", tagsString=" + tagsString
				+ ", hashtagsString=" + hashtagsString + ", course=" + course
				+ ", conectedToCourse=" + connectedWithCourse
				+ ", competencesCanNotBeCreated=" + competencesCanNotBeCreated
				+ ", selected=" + selected + ", retaken=" + retaken
				+ ", lastActivity=" + lastActivity + ", creator=" + creator
				+ "]";
	}
	
	@Override
	public int compareTo(GoalData o) {
		if (this.getDateCompleted() != null && o.getDateCompleted() != null) {
			if (this.getDateCompleted().after(o.getDateCompleted())) {
				return -1;
			} else {
				return 1;
			}
		}
		return this.getTitle().compareToIgnoreCase(o.getTitle());
	}
	
}
