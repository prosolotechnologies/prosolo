package org.prosolo.services.nodes.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.services.common.observable.StandardObservable;
import org.prosolo.services.nodes.util.TimeUtil;

/** For all fields that can be updated changes can be tracked */
public class CredentialData extends StandardObservable implements Serializable {

	private static final long serialVersionUID = -8784334832131740545L;
	
	private long id;
	private String title;
	private String description;
	private Set<Tag> tags;
	private String tagsString;
	private Set<Tag> hashtags;
	private String hashtagsString = "";
	private boolean published;
	private PublishedStatus status;
	private String typeString;
	private LearningResourceType type;
	private boolean mandatoryFlow;
	private long duration;
	private String durationString;
	private ResourceCreator creator;
	private List<CompetenceData1> competences;
	//true if this is data for draft version of credential
	private boolean draft;
	private boolean hasDraft;
	private boolean studentsCanAddCompetences;
	private boolean manuallyAssingStudents;
	private int defaultNumberOfStudentsPerInstructor;
	
	//target credential data
	private boolean enrolled;
	private long targetCredId;
	private int progress;
	private long nextCompetenceToLearnId;
	private long nextActivityToLearnId;
	
	private boolean bookmarkedByCurrentUser;
	
	public CredentialData(boolean listenChanges) {
		this.status = PublishedStatus.DRAFT;
		competences = new ArrayList<>();
		this.listenChanges = listenChanges;
	}
	
	/**
	 * This method needed to be overriden to deal with collection of competences because
	 * super method does not take into account collections
	 */
	@Override
	public boolean hasObjectChanged() {
		boolean changed = super.hasObjectChanged();
		if(!changed) {
			for(CompetenceData1 cd : getCompetences()) {
				if(cd.getObjectStatus() != ObjectStatus.UP_TO_DATE) {
					return true;
				}
			}
		}
		return changed;
	}
	
	/**
	 * Returns true if credential is draft and it is not a draft version, so it
	 * means that it is original version that is created as draft - has never been published
	 * @return
	 */
	public boolean isFirstTimeDraft() {
		return !published && !draft && !hasDraft;
	}
	
	public void calculateDurationString() {
		durationString = TimeUtil.getHoursAndMinutesInString(this.duration);
	}
	
	//setting course status based on published flag
	public void setCredentialStatus() {
		this.status = this.published ? PublishedStatus.PUBLISHED : PublishedStatus.DRAFT;
	}
	
	//setting published flag based on course status
	private void setPublished() {
		setPublished(status == PublishedStatus.PUBLISHED ? true : false);
	}
	
	private void setCredentialTypeFromString() {
		type = LearningResourceType.valueOf(typeString.toUpperCase());
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

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		observeAttributeChange("published", this.published, published);
		this.published = published;
	}

	public String getTagsString() {
		return tagsString;
	}

	public void setTagsString(String tagsString) {
		observeAttributeChange("tagsString", this.tagsString, tagsString);
		this.tagsString = tagsString;
	}

	public String getHashtagsString() {
		return hashtagsString;
	}

	public void setHashtagsString(String hashtagsString) {
		observeAttributeChange("hashtagsString", this.hashtagsString, hashtagsString);
		this.hashtagsString = hashtagsString;
	}

	public PublishedStatus getStatus() {
		return status;
	}

	public void setStatus(PublishedStatus status) {
		this.status = status;
		setPublished();
	}

	public LearningResourceType getType() {
		return type;
	}

	public void setType(LearningResourceType type) {
		this.type = type;
	}

	public boolean isMandatoryFlow() {
		return mandatoryFlow;
	}

	public void setMandatoryFlow(boolean mandatoryFlow) {
		observeAttributeChange("mandatoryFlow", this.mandatoryFlow, mandatoryFlow);
		this.mandatoryFlow = mandatoryFlow;
	}

	public String getTypeString() {
		return typeString;
	}

	public void setTypeString(String typeString) {
		this.typeString = typeString;
		setCredentialTypeFromString();
	}

	public long getId() {
		return id;
	}

	public void setId(long credId) {
		this.id = credId;
	}

	public List<CompetenceData1> getCompetences() {
		return competences;
	}

	public void setCompetences(List<CompetenceData1> competences) {
		this.competences = competences;
	}

	public ResourceCreator getCreator() {
		return creator;
	}

	public void setCreator(ResourceCreator creator) {
		this.creator = creator;
	}

	public boolean isEnrolled() {
		return enrolled;
	}

	public void setEnrolled(boolean enrolled) {
		this.enrolled = enrolled;
	}

	public long getTargetCredId() {
		return targetCredId;
	}

	public void setTargetCredId(long targetCredId) {
		this.targetCredId = targetCredId;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public String getDurationString() {
		return durationString;
	}
	
	public void setDurationString(String durationString) {
		this.durationString = durationString;
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

	public boolean isHashtagsStringChanged() {
		return changedAttributes.containsKey("hashtagsString");
	}

	public boolean isPublishedChanged() {
		return changedAttributes.containsKey("published");
	}

	public boolean isStatusChanged() {
		return changedAttributes.containsKey("status");
	}

	public boolean isMandatoryFlowChanged() {
		return changedAttributes.containsKey("mandatoryFlow");
	}
	
	public boolean isDurationChanged() {
		return changedAttributes.containsKey("duration");
	}
	
	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	public Set<Tag> getHashtags() {
		return hashtags;
	}

	public void setHashtags(Set<Tag> hashtags) {
		this.hashtags = hashtags;
	}

	public boolean isDraft() {
		return draft;
	}

	public void setDraft(boolean draft) {
		this.draft = draft;
	}

	public boolean isStudentsCanAddCompetences() {
		return studentsCanAddCompetences;
	}

	public void setStudentsCanAddCompetences(boolean studentsCanAddCompetences) {
		this.studentsCanAddCompetences = studentsCanAddCompetences;
	}

	public boolean isManuallyAssingStudents() {
		return manuallyAssingStudents;
	}

	public void setManuallyAssingStudents(boolean manuallyAssingStudents) {
		this.manuallyAssingStudents = manuallyAssingStudents;
	}

	public int getDefaultNumberOfStudentsPerInstructor() {
		return defaultNumberOfStudentsPerInstructor;
	}

	public void setDefaultNumberOfStudentsPerInstructor(int defaultNumberOfStudentsPerInstructor) {
		this.defaultNumberOfStudentsPerInstructor = defaultNumberOfStudentsPerInstructor;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		observeAttributeChange("duration", this.duration, duration);
		this.duration = duration;
		calculateDurationString();
	}

	public boolean isHasDraft() {
		return hasDraft;
	}

	public void setHasDraft(boolean hasDraft) {
		this.hasDraft = hasDraft;
	}

	public boolean isBookmarkedByCurrentUser() {
		return bookmarkedByCurrentUser;
	}

	public void setBookmarkedByCurrentUser(boolean bookmarkedByCurrentUser) {
		this.bookmarkedByCurrentUser = bookmarkedByCurrentUser;
	}

	public long getNextActivityToLearnId() {
		return nextActivityToLearnId;
	}

	public void setNextActivityToLearnId(long nextActivityToLearnId) {
		this.nextActivityToLearnId = nextActivityToLearnId;
	}

	public long getNextCompetenceToLearnId() {
		return nextCompetenceToLearnId;
	}

	public void setNextCompetenceToLearnId(long nextCompetenceToLearnId) {
		this.nextCompetenceToLearnId = nextCompetenceToLearnId;
	}
	
}
