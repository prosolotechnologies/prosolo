package org.prosolo.services.nodes.data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.services.common.observable.StandardObservable;
import org.prosolo.services.nodes.util.TimeUtil;

public class CompetenceData1 extends StandardObservable implements Serializable {

	private static final long serialVersionUID = 6562985459763765320L;
	
	private static Logger logger = Logger.getLogger(CompetenceData1.class);
	
	private long credentialCompetenceId;
	private long competenceId;
	private String title;
	private String description;
	private long duration;
	private String durationString;
	private int order;
	private PublishedStatus status;
	private ActivityData activityToShowWithDetails;
	private boolean activitiesInitialized;
	private List<ActivityData> activities;
	private Set<Tag> tags;
	private String tagsString;
	private boolean studentAllowedToAddActivities;
	private String typeString;
	private LearningResourceType type;
	
	private boolean enrolled;
	private long targetCompId;
	private int progress;
	private long nextActivityToLearnId;
	private ResourceCreator creator;
	
	private long credentialId;
	private String credentialTitle;
	
	private ObjectStatus objectStatus;
	
	private boolean published;
	private ResourceVisibility visibility;
	private Date scheduledPublicDate;
	private String scheduledPublicDateValue;
	
	private List<CredentialData> credentialsWithIncludedCompetence;
	private long instructorId;
	
	private boolean canEdit;
	private boolean canAccess;
	
	public CompetenceData1(boolean listenChanges) {
		this.status = PublishedStatus.DRAFT;
		this.visibility = ResourceVisibility.UNPUBLISH;
		activities = new ArrayList<>();
		credentialsWithIncludedCompetence = new ArrayList<>();
		this.listenChanges = listenChanges;
	}
	
	public void setVisibility(boolean published, Date scheduledPublicDate) {
		if(published) {
			if(scheduledPublicDate == null) {
				this.visibility = ResourceVisibility.PUBLISHED;
			} else {
				this.visibility = ResourceVisibility.SCHEDULED_UNPUBLISH;
			}
		} else {
			if(scheduledPublicDate == null) {
				this.visibility = ResourceVisibility.UNPUBLISH;
			} else {
				this.visibility = ResourceVisibility.SCHEDULED_PUBLISH;
			}
		}
	}
	
	public boolean isCompVisible() {
		return this.visibility == ResourceVisibility.PUBLISHED ? true : false;
	}
	
	@Override
	public boolean hasObjectChanged() {
		boolean changed = super.hasObjectChanged();
		if(!changed) {
			for(ActivityData bad : getActivities()) {
				if(bad.getObjectStatus() != ObjectStatus.UP_TO_DATE) {
					return true;
				}
			}
		}
		return changed;
	}
	
	private void setCompetenceTypeFromString() {
		type = LearningResourceType.valueOf(typeString.toUpperCase());
	}
	
	/** 
	 * Sets object status based on order - if order changed
	 * from initial value, status should be changed too
	*/
	public void statusChangeTransitionBasedOnOrderChange() {
		if(isOrderChanged()) {
			setObjectStatus(ObjectStatusTransitions.changeTransition(getObjectStatus()));
		} else {
			setObjectStatus(ObjectStatusTransitions.upToDateTransition(getObjectStatus()));
		}
	}
	
	public void statusRemoveTransition() {
		setObjectStatus(ObjectStatusTransitions.removeTransition(getObjectStatus()));
	}
	
	public void statusBackFromRemovedTransition() {
		if(isOrderChanged()) {
			setObjectStatus(ObjectStatus.CHANGED);
		} else {
			setObjectStatus(ObjectStatus.UP_TO_DATE);
		}
	}
	
	//setting competence status based on published flag
	public void setCompStatus() {
		this.status = this.published ? PublishedStatus.PUBLISHED : PublishedStatus.DRAFT;
	}
	
	//setting published flag based on competence status
	private void setPublished() {
		setPublished(status == PublishedStatus.PUBLISHED ? true : false);
	}
	
	public void calculateDurationString() {
		durationString = TimeUtil.getHoursAndMinutesInString(this.duration);
	}

	public long getCredentialCompetenceId() {
		return credentialCompetenceId;
	}

	public void setCredentialCompetenceId(long credentialCompetenceId) {
		this.credentialCompetenceId = credentialCompetenceId;
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
		observeAttributeChange("title", this.title, title);
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		observeAttributeChange("description", this.description, description);
		this.description = description;
	}

	public String getDurationString() {
		return durationString;
	}
	
	public void setDurationString(String durationString) {
		this.durationString = durationString;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		observeAttributeChange("order", this.order, order);
		this.order = order;
	}

	public ActivityData getActivityToShowWithDetails() {
		return activityToShowWithDetails;
	}

	public void setActivityToShowWithDetails(ActivityData activityToShowWithDetails) {
		this.activityToShowWithDetails = activityToShowWithDetails;
	}

	public List<ActivityData> getActivities() {
		return activities;
	}

	public void setActivities(List<ActivityData> activities) {
		this.activities = activities;
	}

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		observeAttributeChange("published", this.published, published);
		this.published = published;
	}

	public PublishedStatus getStatus() {
		return status;
	}

	public void setStatus(PublishedStatus status) {
		this.status = status;
		setPublished();
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public ResourceCreator getCreator() {
		return creator;
	}

	public void setCreator(ResourceCreator creator) {
		this.creator = creator;
	}

	public ObjectStatus getObjectStatus() {
		return objectStatus;
	}

	public void setObjectStatus(ObjectStatus objectStatus) {
		observeAttributeChange("objectStatus", this.objectStatus, objectStatus);
		this.objectStatus = objectStatus;
	}

	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	public String getTagsString() {
		return tagsString;
	}

	public void setTagsString(String tagsString) {
		observeAttributeChange("tagsString", this.tagsString, tagsString);
		this.tagsString = tagsString;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		observeAttributeChange("duration", this.duration, duration);
		this.duration = duration;
		calculateDurationString();
	}
	
	public boolean isStudentAllowedToAddActivities() {
		return studentAllowedToAddActivities;
	}

	public void setStudentAllowedToAddActivities(boolean studentAllowedToAddActivities) {
		observeAttributeChange("studentAllowedToAddActivities", this.studentAllowedToAddActivities, 
				studentAllowedToAddActivities);
		this.studentAllowedToAddActivities = studentAllowedToAddActivities;
	}
	
	public Date getScheduledPublicDate() {
		return scheduledPublicDate;
	}

	public void setScheduledPublicDate(Date scheduledPublicDate) {
		observeAttributeChange("scheduledPublicDate", this.scheduledPublicDate, scheduledPublicDate, 
				(Date d1, Date d2) -> d1 == null ? d2 == null : d1.compareTo(d2) == 0);
		this.scheduledPublicDate = scheduledPublicDate;
	}

	public ResourceVisibility getVisibility() {
		return visibility;
	}

	public void setVisibility(ResourceVisibility visibility) {
		observeAttributeChange("visibility", this.visibility, visibility);
		this.visibility = visibility;
	}

	public String getScheduledPublicDateValue() {
		return scheduledPublicDateValue;
	}

	public void setScheduledPublicDateValue(String scheduledPublicDateValue) {
		this.scheduledPublicDateValue = scheduledPublicDateValue;
		if(StringUtils.isNotBlank(scheduledPublicDateValue)) {
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
			Date d = null;
			try {
				d = sdf.parse(scheduledPublicDateValue);
			} catch(Exception e) {
				logger.error(String.format("Could not parse scheduled publish time : %s", scheduledPublicDateValue), e);
			}
			setScheduledPublicDate(d);
		}
	}

	public boolean isEnrolled() {
		return enrolled;
	}

	public void setEnrolled(boolean enrolled) {
		this.enrolled = enrolled;
	}

	public long getTargetCompId() {
		return targetCompId;
	}

	public void setTargetCompId(long targetCompId) {
		this.targetCompId = targetCompId;
	}

	public List<CredentialData> getCredentialsWithIncludedCompetence() {
		return credentialsWithIncludedCompetence;
	}

	public void setCredentialsWithIncludedCompetence(List<CredentialData> credentialsWithIncludedCompetence) {
		this.credentialsWithIncludedCompetence = credentialsWithIncludedCompetence;
	}

	public long getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(long credentialId) {
		this.credentialId = credentialId;
	}

	public String getCredentialTitle() {
		return credentialTitle;
	}

	public void setCredentialTitle(String credentialTitle) {
		this.credentialTitle = credentialTitle;
	}

	public boolean isActivitiesInitialized() {
		return activitiesInitialized;
	}

	public void setActivitiesInitialized(boolean activitiesInitialized) {
		this.activitiesInitialized = activitiesInitialized;
	}

	public long getNextActivityToLearnId() {
		return nextActivityToLearnId;
	}

	public void setNextActivityToLearnId(long nextActivityToLearnId) {
		this.nextActivityToLearnId = nextActivityToLearnId;
	}

	public String getTypeString() {
		return typeString;
	}

	public void setTypeString(String typeString) {
		this.typeString = typeString;
		setCompetenceTypeFromString();
	}
	
	public LearningResourceType getType() {
		return type;
	}

	public void setType(LearningResourceType type) {
		this.type = type;
	}

	public long getInstructorId() {
		return instructorId;
	}

	public void setInstructorId(long instructorId) {
		this.instructorId = instructorId;
	}
	
	public boolean isCanEdit() {
		return canEdit;
	}

	public void setCanEdit(boolean canEdit) {
		this.canEdit = canEdit;
	}
	
	public boolean isCanAccess() {
		return canAccess;
	}

	public void setCanAccess(boolean canAccess) {
		this.canAccess = canAccess;
	}
	
	//change tracking get methods
	
	public boolean isTitleChanged() {
		return changedAttributes.containsKey("title");
	}

	public boolean isDescriptionChanged() {
		return changedAttributes.containsKey("description");
	}

	public boolean isTagsStringChanged() {
		return changedAttributes.containsKey("tagsString");
	}

	public boolean isPublishedChanged() {
		return changedAttributes.containsKey("published");
	}
	
	public boolean isObjectStatusChanged() {
		return changedAttributes.containsKey("objectStatus");
	}
	
	public boolean isOrderChanged() {
		return changedAttributes.containsKey("order");
	}
	
	public boolean isDurationChanged() {
		return changedAttributes.containsKey("duration");
	}
	
	public boolean isStudentAllowedToAddActivitiesChanged() {
		return changedAttributes.containsKey("studentAllowedToAddActivities");
	}
	
	public boolean isScheduledPublicDateChanged() {
		return changedAttributes.containsKey("scheduledPublicDate");
	}
	
}
