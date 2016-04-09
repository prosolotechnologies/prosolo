package org.prosolo.services.nodes.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.services.common.observable.StandardObservable;
import org.prosolo.services.nodes.util.TimeUtil;

public class CompetenceData1 extends StandardObservable implements Serializable {

	private static final long serialVersionUID = 6562985459763765320L;
	
	private long credentialCompetenceId;
	private long competenceId;
	private String title;
	private String description;
	private long duration;
	private String durationString;
	private int order;
	private boolean published;
	private PublishedStatus status;
	private List<BasicActivityData> activities;
	private Set<Tag> tags;
	private String tagsString;
	private boolean studentAllowedToAddActivities;
	//true if this is data for draft version of credential
	private boolean draft;
	
	private boolean enrolled;
	private long targetCompId;
	private int progress;
	private ResourceCreator creator;
	
	private ObjectStatus objectStatus;
	
	private List<CredentialData> credentialsWithIncludedCompetence;
	
	public CompetenceData1(boolean listenChanges) {
		this.status = PublishedStatus.DRAFT;
		activities = new ArrayList<>();
		credentialsWithIncludedCompetence = new ArrayList<>();
		this.listenChanges = listenChanges;
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

	public List<BasicActivityData> getActivities() {
		return activities;
	}

	public void setActivities(List<BasicActivityData> activities) {
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

	public boolean isDraft() {
		return draft;
	}

	public void setDraft(boolean draft) {
		this.draft = draft;
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
	
}
