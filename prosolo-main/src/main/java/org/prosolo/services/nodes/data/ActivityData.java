package org.prosolo.services.nodes.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.credential.ScoreCalculation;
import org.prosolo.services.common.observable.StandardObservable;
import org.prosolo.services.nodes.util.TimeUtil;

public class ActivityData extends StandardObservable implements Serializable {

	private static final long serialVersionUID = 4976975810970581297L;
	
	/*
	 * this is special version field that should not be changed. it should be copied from 
	 * a database record and never changed again.
	 */
	private long version = -1;
	private long activityId;
	private long competenceActivityId;
	private long targetActivityId;
	private String title;
	private String description;
	private Date dateCreated;
	//target activity specific
	private boolean enrolled;
	private boolean completed;
	private ActivityResultData resultData;
	private int maxPoints;
	private String maxPointsString = ""; // needed because field can also be empty on the html page
	
	private int order;
	private int durationHours;
	private int durationMinutes;
	private String durationString;
	private long creatorId;
	
	private List<ResourceLinkData> links;
	private List<ResourceLinkData> files;
	private List<ResourceLinkData> captions;
	private String typeString;
	private LearningResourceType type;
	
	private ObjectStatus objectStatus;
	
	private ActivityType activityType;
	
	//UrlActivity specific
	private String videoLink;
	private String slidesLink;
	private String linkName;
	private String embedId;
	
	//ExternalToolActivity specific
	private String launchUrl;
	private String sharedSecret;
	private String consumerKey;
	private boolean acceptGrades;
	private boolean openInNewWindow;
	private boolean visibleForUnenrolledStudents;
	private ScoreCalculation scoreCalculation;
	
	//TextActivity specific
	private String text;
	
	private long competenceId;
	private String competenceName;
	
	private List<ActivityResultData> studentResults;
	private GradeData gradeOptions;
	
	private boolean studentCanSeeOtherResponses;
	private boolean studentCanEditResponse;
	
	private boolean canEdit;
	private boolean canAccess;
	private boolean autograde;
	
	private int difficulty;
	
	//indicates that competence was once published
	private boolean oncePublished;
	
	public ActivityData(boolean listenChanges) {
		this.listenChanges = listenChanges;
		links = new ArrayList<>();
		files = new ArrayList<>();
		captions = new ArrayList<>();
		activityType = ActivityType.TEXT;
		resultData = new ActivityResultData(listenChanges);
		gradeOptions = new GradeData();
	}
	
	@Override
	public void startObservingChanges() {
		super.startObservingChanges();
		getResultData().startObservingChanges();
	}
	
	@Override
	public boolean hasObjectChanged() {
		boolean changed = super.hasObjectChanged();
		if(!changed) {
			for(ResourceLinkData rl : getLinks()) {
				if(rl.getStatus() != ObjectStatus.UP_TO_DATE) {
					return true;
				}
			}
			
			for(ResourceLinkData rl : getFiles()) {
				if(rl.getStatus() != ObjectStatus.UP_TO_DATE) {
					return true;
				}
			}
			
			for(ResourceLinkData rl : getCaptions()) {
				if(rl.getStatus() != ObjectStatus.UP_TO_DATE) {
					return true;
				}
			}
			
			if(getResultData().hasObjectChanged()) {
				return true;
			}
		}
		return changed;
	}
	
	private void setActivityTypeFromString() {
		type = LearningResourceType.valueOf(typeString.toUpperCase());
	}
	
	public long getTargetOrRegularActivityId() {
		return targetActivityId != 0 ? targetActivityId : activityId;
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
	
	public void calculateDurationString() {
		durationString = TimeUtil.getHoursAndMinutesInString(
				this.durationHours * 60 + this.durationMinutes);
	}

	public long getActivityId() {
		return activityId;
	}

	public void setActivityId(long activityId) {
		this.activityId = activityId;
	}

	public long getCompetenceActivityId() {
		return competenceActivityId;
	}

	public void setCompetenceActivityId(long competenceActivityId) {
		this.competenceActivityId = competenceActivityId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		observeAttributeChange("title", this.title, title);
		this.title = title;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
	
	public String getMaxPointsString() {
		return maxPointsString;
	}

	public void setMaxPointsString(String maxPointsString) {
		observeAttributeChange("maxPointsString", this.maxPointsString, maxPointsString);
		this.maxPointsString = maxPointsString;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		observeAttributeChange("order", this.order, order);
		this.order = order;
	}

	public ObjectStatus getObjectStatus() {
		return objectStatus;
	}

	public void setObjectStatus(ObjectStatus objectStatus) {
		observeAttributeChange("objectStatus", this.objectStatus, objectStatus);
		this.objectStatus = objectStatus;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		observeAttributeChange("description", this.description, description);
		this.description = description;
	}
	
	public int getDurationHours() {
		return durationHours;
	}

	public void setDurationHours(int durationHours) {
		observeAttributeChange("durationHours", this.durationHours, durationHours);
		this.durationHours = durationHours;
	}

	public int getDurationMinutes() {
		return durationMinutes;
	}

	public void setDurationMinutes(int durationMinutes) {
		observeAttributeChange("durationMinutes", this.durationMinutes, durationMinutes);
		this.durationMinutes = durationMinutes;
	}

	public String getDurationString() {
		return durationString;
	}

	public void setDurationString(String durationString) {
		this.durationString = durationString;
	}

	public long getTargetActivityId() {
		return targetActivityId;
	}

	public void setTargetActivityId(long targetActivityId) {
		this.targetActivityId = targetActivityId;
	}
	
	public boolean isEnrolled() {
		return enrolled;
	}

	public void setEnrolled(boolean enrolled) {
		this.enrolled = enrolled;
	}

	public long getCompetenceId() {
		return competenceId;
	}

	public void setCompetenceId(long competenceId) {
		this.competenceId = competenceId;
	}

	public String getCompetenceName() {
		return competenceName;
	}

	public void setCompetenceName(String competenceName) {
		this.competenceName = competenceName;
	}
	
	//think which attributes should be tracked
	
	public List<ResourceLinkData> getLinks() {
		return links;
	}

	public void setLinks(List<ResourceLinkData> links) {
		this.links = links;
	}

	public List<ResourceLinkData> getFiles() {
		return files;
	}

	public void setFiles(List<ResourceLinkData> files) {
		this.files = files;
	}

	public ActivityType getActivityType() {
		return activityType;
	}

	public void setActivityType(ActivityType activityType) {
		observeAttributeChange("activityType", this.activityType, activityType);
		this.activityType = activityType;
	}

	public String getVideoLink() {
		return videoLink;
	}

	public void setVideoLink(String videoLink) {
		observeAttributeChange("videoLink", this.videoLink, videoLink);
		this.videoLink = videoLink;
	}

	public String getSlidesLink() {
		return slidesLink;
	}

	public void setSlidesLink(String slidesLink) {
		observeAttributeChange("slidesLink", this.slidesLink, slidesLink);
		this.slidesLink = slidesLink;
	}

	public String getLinkName() {
		return linkName;
	}

	public void setLinkName(String linkName) {
		observeAttributeChange("linkName", this.linkName, linkName);
		this.linkName = linkName;
	}

	public String getLaunchUrl() {
		return launchUrl;
	}

	public void setLaunchUrl(String launchUrl) {
		observeAttributeChange("launchUrl", this.launchUrl, launchUrl);
		this.launchUrl = launchUrl;
	}

	public String getSharedSecret() {
		return sharedSecret;
	}

	public void setSharedSecret(String sharedSecret) {
		observeAttributeChange("sharedSecret", this.sharedSecret, sharedSecret);
		this.sharedSecret = sharedSecret;
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		observeAttributeChange("consumerKey", this.consumerKey, consumerKey);
		this.consumerKey = consumerKey;
	}

	public boolean isAcceptGrades() {
		return acceptGrades;
	}

	public void setAcceptGrades(boolean acceptGrades) {
		observeAttributeChange("acceptGrades", this.acceptGrades, acceptGrades);
		this.acceptGrades = acceptGrades;
	}
	
	public boolean isOpenInNewWindow() {
		return openInNewWindow;
	}

	public void setOpenInNewWindow(boolean openInNewWindow) {
		observeAttributeChange("openInNewWindow", this.openInNewWindow, openInNewWindow);
		this.openInNewWindow = openInNewWindow;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		observeAttributeChange("text", this.text, text);
		this.text = text;
	}
	
	public String getEmbedId() {
		return embedId;
	}

	public void setEmbedId(String embedId) {
		this.embedId = embedId;
	}
	
	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	public String getTypeString() {
		return typeString;
	}

	public void setTypeString(String typeString) {
		this.typeString = typeString;
		setActivityTypeFromString();
	}
	
	public LearningResourceType getType() {
		return type;
	}

	public void setType(LearningResourceType type) {
		this.type = type;
	}
	
	public long getCreatorId() {
		return creatorId;
	}
	
	public void setCreatorId(long creatorId) {
		this.creatorId = creatorId;
	}
	
	public List<ResourceLinkData> getCaptions() {
		return captions;
	}

	public void setCaptions(List<ResourceLinkData> captions) {
		this.captions = captions;
	}
	
	public boolean isVisibleForUnenrolledStudents() {
		return visibleForUnenrolledStudents;
	}

	public void setVisibleForUnenrolledStudents(boolean visibleForUnenrolledStudents) {
		observeAttributeChange("visibleForUnenrolledStudents", this.visibleForUnenrolledStudents, visibleForUnenrolledStudents);
		this.visibleForUnenrolledStudents = visibleForUnenrolledStudents;
	}
	
	public ScoreCalculation getScoreCalculation() {
		return scoreCalculation;
	}

	public void setScoreCalculation(ScoreCalculation scoreCalculation) {
		observeAttributeChange("scoreCalculation", this.scoreCalculation, scoreCalculation);
		this.scoreCalculation = scoreCalculation;
	}
	
	//change tracking get methods

	public boolean isTitleChanged() {
		return changedAttributes.containsKey("title");
	}

	public boolean isDescriptionChanged() {
		return changedAttributes.containsKey("description");
	}
	
	public boolean isObjectStatusChanged() {
		return changedAttributes.containsKey("objectStatus");
	}
	
	public boolean isOrderChanged() {
		return changedAttributes.containsKey("order");
	}
	
	public boolean isUploadAssignmentChanged() {
		return changedAttributes.containsKey("uploadAssignment");
	}
	
	public boolean isLinkChanged() {
		return changedAttributes.containsKey("link");
	}

	public boolean isLinkNameChanged() {
		return changedAttributes.containsKey("linkName");
	}
	
	public boolean isLaunchUrlChanged() {
		return changedAttributes.containsKey("launchUrl");
	}
	
	public boolean isSharedSecretChanged() {
		return changedAttributes.containsKey("sharedSecret");
	}
	
	public boolean isConsumerKeyChanged() {
		return changedAttributes.containsKey("consumerKey");
	}
	
	public boolean isAcceptGradesChanged() {
		return changedAttributes.containsKey("acceptGrades");
	}
	
	public boolean isOpenInNewWindowChanged() {
		return changedAttributes.containsKey("openInNewWindow");
	}
	
	public boolean isTextChanged() {
		return changedAttributes.containsKey("text");
	}
	
	public boolean isDurationHoursChanged() {
		return changedAttributes.containsKey("durationHours");
	}
	
	public boolean isDurationMinutesChanged() {
		return changedAttributes.containsKey("durationMinutes");
	}
	
	public boolean isActivityTypeChanged() {
		return changedAttributes.containsKey("activityType");
	}
	
	public boolean isDifficultyChanged() {
		return changedAttributes.containsKey("difficulty");
	}
	
	public boolean isAutogradeChanged() {
		return changedAttributes.containsKey("autograde");
	}
	
	//special methods to retrieve duration before update
	public Optional<Integer> getDurationHoursBeforeUpdate() {
		Integer dur = (Integer) changedAttributes.get("durationHours");
		if(dur == null) {
			return Optional.empty();
		} else {
			return Optional.of(dur);
		}
	}
	
	public Optional<Integer> getDurationMinutesBeforeUpdate() {
		Integer dur = (Integer) changedAttributes.get("durationMinutes");
		if(dur == null) {
			return Optional.empty();
		} else {
			return Optional.of(dur);
		}
	}
	
	/**
	 * Retrieves activity type before update if it is changed. Otherwise value is empty.
	 * 
	 * @return
	 */
	public Optional<ActivityType> getActivityTypeBeforeUpdate() {
		ActivityType type = (ActivityType) changedAttributes.get("activityType");
		if(type == null) {
			return Optional.empty();
		} else {
			return Optional.of(type);
		}
	}

	public ActivityResultData getResultData() {
		return resultData;
	}

	public void setResultData(ActivityResultData resultData) {
		this.resultData = resultData;
	}

	public List<ActivityResultData> getStudentResults() {
		return studentResults;
	}

	public void setStudentResults(List<ActivityResultData> studentResults) {
		this.studentResults = studentResults;
	}

	public GradeData getGradeOptions() {
		return gradeOptions;
	}

	public void setGradeOptions(GradeData gradeOptions) {
		this.gradeOptions = gradeOptions;
	}

	public boolean isStudentCanSeeOtherResponses() {
		return studentCanSeeOtherResponses;
	}

	public void setStudentCanSeeOtherResponses(boolean studentCanSeeOtherResponses) {
		observeAttributeChange("studentCanSeeOtherResponses", this.studentCanSeeOtherResponses, studentCanSeeOtherResponses);
		this.studentCanSeeOtherResponses = studentCanSeeOtherResponses;
	}

	public boolean isStudentCanEditResponse() {
		return studentCanEditResponse;
	}

	public void setStudentCanEditResponse(boolean studentCanEditResponse) {
		observeAttributeChange("studentCanEditResponse", this.studentCanEditResponse, studentCanEditResponse);
		this.studentCanEditResponse = studentCanEditResponse;
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

	public int getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(int difficulty) {
		observeAttributeChange("difficulty", this.difficulty, difficulty);
		this.difficulty = difficulty;
	}
	
	public boolean isAutograde() {
		return autograde;
	}

	public void setAutograde(boolean autograde) {
		observeAttributeChange("autograde", this.autograde, autograde);
		this.autograde = autograde;
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

	public boolean isOncePublished() {
		return oncePublished;
	}

	public void setOncePublished(boolean oncePublished) {
		this.oncePublished = oncePublished;
	}

	public int getMaxPoints() {
		return maxPoints;
	}

	public void setMaxPoints(int maxPoints) {
		this.maxPoints = maxPoints;
	}
}
