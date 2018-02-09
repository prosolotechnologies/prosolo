package org.prosolo.services.nodes.data;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.LearningPathType;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.assessment.data.AssessmentTypeConfig;
import org.prosolo.services.assessment.data.LearningResourceAssessmentSettings;
import org.prosolo.services.common.observable.StandardObservable;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;
import org.prosolo.services.nodes.data.organization.LearningStageData;
import org.prosolo.services.nodes.util.TimeUtil;

import java.io.Serializable;
import java.util.*;

public class CompetenceData1 extends StandardObservable implements Serializable {

	private static final long serialVersionUID = 6562985459763765320L;
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CompetenceData1.class);
	
	/*
	 * this is special version field that should not be changed. it should be copied from 
	 * a database record and never be changed again.
	 */
	private long version = -1;
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
	private boolean archived;
	
	private List<CredentialData> credentialsWithIncludedCompetence;
	private long instructorId;
	
	private boolean bookmarkedByCurrentUser;
	
	private Date datePublished;
	
	private long numberOfStudents;

	private LearningPathType learningPathType = LearningPathType.ACTIVITY;

	//target competence data
	//if evidence based learning path, student can post evidences
	private List<LearningEvidenceData> evidences;

	//learning stage info
	private boolean learningStageEnabled;
	private LearningStageData learningStage;
	
	//by default competence can be unpublished
	private boolean canUnpublish = true;

	//assessment
	private LearningResourceAssessmentSettings assessmentSettings;
	private List<AssessmentTypeConfig> assessmentTypes;

	public CompetenceData1(boolean listenChanges) {
		this.status = PublishedStatus.DRAFT;
		activities = new ArrayList<>();
		credentialsWithIncludedCompetence = new ArrayList<>();
		tags = new HashSet<>();
		evidences = new ArrayList<>();
		assessmentSettings = new LearningResourceAssessmentSettings();
		assessmentTypes = new ArrayList<>();
		this.listenChanges = listenChanges;
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

			if (getAssessmentSettings().hasObjectChanged()) {
				return true;
			}

			for (AssessmentTypeConfig atc : getAssessmentTypes()) {
				if (atc.hasObjectChanged()) {
					return true;
				}
			}
		}
		return changed;
	}

	@Override
	public void startObservingChanges() {
		super.startObservingChanges();
		getAssessmentSettings().startObservingChanges();
		for (AssessmentTypeConfig atc : getAssessmentTypes()) {
			atc.startObservingChanges();
		}
	}

	public long getPublishedTime() {
		return DateUtil.getMillisFromDate(datePublished);
	}
	
	public boolean isScheduledPublish() {
		if(datePublished != null && datePublished.after(new Date())) {
			return true;
		}
		return false;
	}
	
	public void addActivity(ActivityData activity) {
		if(activity != null) {
			activities.add(activity);
		}
	}
	
	public boolean isUniversityCreated() {
		return type == LearningResourceType.UNIVERSITY_CREATED;
	}
	
	public boolean isUserCreated() {
		return type == LearningResourceType.USER_CREATED;
	}
	
	public boolean isCompleted() {
		return progress == 100;
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
	
	/**
	 * setting competence status based on published flag and datePublished field, so these two field must be set
	 * before calling this method.
	 */
	public void setCompStatus() {
		this.status = this.published 
				? PublishedStatus.PUBLISHED 
				: this.datePublished != null ? PublishedStatus.UNPUBLISHED : PublishedStatus.DRAFT;
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

	public boolean isLearningPathChanged() {
		return changedAttributes.containsKey("learningPathType");
	}

	public boolean isBookmarkedByCurrentUser() {
		return bookmarkedByCurrentUser;
	}

	public void setBookmarkedByCurrentUser(boolean bookmarkedByCurrentUser) {
		this.bookmarkedByCurrentUser = bookmarkedByCurrentUser;
	}

	public Date getDatePublished() {
		return datePublished;
	}

	public void setDatePublished(Date datePublished) {
		this.datePublished = datePublished;
	}

	public long getNumberOfStudents() {
		return numberOfStudents;
	}

	public void setNumberOfStudents(long numberOfStudents) {
		this.numberOfStudents = numberOfStudents;
	}

	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	public long getVersion() {
		return version;
	}

	/**
	 * Setting version is only allowed if version is -1. Generally version should not 
	 * be changed except when data is being populated.
	 * 
	 * @param version
	 */
	public void setVersion(long version) {
		if(this.version == -1) {
			this.version = version;
		}
	}

	public boolean isCanUnpublish() {
		return canUnpublish;
	}

	public void setCanUnpublish(boolean canUnpublish) {
		this.canUnpublish = canUnpublish;
	}

	public LearningPathType getLearningPathType() {
		return learningPathType;
	}

	public void setLearningPathType(LearningPathType learningPathType) {
		observeAttributeChange("learningPathType", this.learningPathType, learningPathType);
		this.learningPathType = learningPathType;
	}

	public List<LearningEvidenceData> getEvidences() {
		return evidences;
	}

	public void setEvidences(List<LearningEvidenceData> evidences) {
		this.evidences = evidences;
	}

	public boolean isLearningStageEnabled() {
		return learningStageEnabled;
	}

	public void setLearningStageEnabled(boolean learningStageEnabled) {
		this.learningStageEnabled = learningStageEnabled;
	}

	public LearningStageData getLearningStage() {
		return learningStage;
	}

	public void setLearningStage(LearningStageData learningStage) {
		this.learningStage = learningStage;
	}

	public LearningResourceAssessmentSettings getAssessmentSettings() {
		return assessmentSettings;
	}

	public List<AssessmentTypeConfig> getAssessmentTypes() {
		return assessmentTypes;
	}

	public void setAssessmentTypes(List<AssessmentTypeConfig> assessmentTypes) {
		this.assessmentTypes = assessmentTypes;
	}
}
