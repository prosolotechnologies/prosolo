package org.prosolo.services.nodes.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.prosolo.services.common.observable.StandardObservable;
import org.prosolo.services.nodes.util.TimeUtil;

public class ActivityData extends StandardObservable implements Serializable {

	private static final long serialVersionUID = 4976975810970581297L;
	
	private long activityId;
	private long competenceActivityId;
	private long targetActivityId;
	private String title;
	private String description;
	//target activity specific
	private boolean enrolled;
	private boolean completed;
	
	private int order;
	private int durationHours;
	private int durationMinutes;
	private String durationString;
	private boolean published;
	private boolean draft;
	private boolean hasDraft;
	private PublishedStatus status;
	
	private List<ResourceLinkData> links;
	private List<ResourceLinkData> files;
	private boolean uploadAssignment;
	
	private ObjectStatus objectStatus;
	
	private ActivityType activityType;
	
	//UrlActivity specific
	private String link;
	private String linkName;
	
	//ExternalToolActivity specific
	private String launchUrl;
	private String sharedSecret;
	private String consumerKey;
	private boolean acceptGrades;
	
	//TextActivity specific
	private String text;
	
	private long competenceId;
	private String competenceName;
	
	public ActivityData(boolean listenChanges) {
		this.listenChanges = listenChanges;
		links = new ArrayList<>();
		files = new ArrayList<>();
		activityType = ActivityType.TEXT;
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
	
	//setting activity status based on published flag
	public void setActivityStatus() {
		this.status = this.published ? PublishedStatus.PUBLISHED : PublishedStatus.DRAFT;
	}
	
	//setting published flag based on course status
	private void setPublished() {
		setPublished(status == PublishedStatus.PUBLISHED ? true : false);
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

	public boolean isDraft() {
		return draft;
	}

	public void setDraft(boolean draft) {
		this.draft = draft;
	}

	public boolean isHasDraft() {
		return hasDraft;
	}

	public void setHasDraft(boolean hasDraft) {
		this.hasDraft = hasDraft;
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

	public boolean isUploadAssignment() {
		return uploadAssignment;
	}

	public void setUploadAssignment(boolean uploadAssignment) {
		observeAttributeChange("uploadAssignment", this.uploadAssignment, uploadAssignment);
		this.uploadAssignment = uploadAssignment;
	}

	public ActivityType getActivityType() {
		return activityType;
	}

	public void setActivityType(ActivityType activityType) {
		this.activityType = activityType;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		observeAttributeChange("link", this.link, link);
		this.link = link;
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

	public String getText() {
		return text;
	}

	public void setText(String text) {
		observeAttributeChange("text", this.text, text);
		this.text = text;
	}
	
	//change tracking get methods

	public boolean isTitleChanged() {
		return changedAttributes.containsKey("title");
	}

	public boolean isDescriptionChanged() {
		return changedAttributes.containsKey("description");
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
	
	public boolean isTextChanged() {
		return changedAttributes.containsKey("text");
	}
	
	public boolean isDurationHoursChanged() {
		return changedAttributes.containsKey("durationHours");
	}
	
	public boolean isDurationMinutesChanged() {
		return changedAttributes.containsKey("durationMinutes");
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
	
}