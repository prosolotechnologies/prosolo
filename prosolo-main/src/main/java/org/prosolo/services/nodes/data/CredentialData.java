package org.prosolo.services.nodes.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.CredentialType1;

public class CredentialData implements Serializable {

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
	private CredentialType1 type;
	private boolean mandatoryFlow;
	private String durationString;
	private ResourceCreator creator;
	private List<CompetenceData1> competences;
	
	//target credential data
	private boolean enrolled;
	private long targetCredId;
	private int progress;
	
	
	public CredentialData() {
		setCredentialStatus();
		tags = new HashSet<>();
		hashtags = new HashSet<>();
		competences = new ArrayList<>();
	}
	
	public static CredentialData copyBasicCourseData(CredentialData data) {
		CredentialData course = new CredentialData();
		course.setTitle(data.getTitle());
		course.setDescription(data.getDescription());
		course.setTagsString(data.getTagsString());
		course.setPublished(data.isPublished());
		course.setCredentialStatus();
		course.setHashtagsString(data.getHashtagsString());
		course.setType(data.getType());
		course.setMandatoryFlow(data.isMandatoryFlow());
		return course;
	}
	
	public void setAdditionalValues() {
		setPublished();
		setCredentialTypeFromString(typeString);
	}
	
	//setting course status based on published flag
	public void setCredentialStatus() {
		this.status = this.published ? PublishedStatus.PUBLISHED : PublishedStatus.DRAFT;
	}
	
	//setting published flag based on course status
	public void setPublished() {
		this.published = status == PublishedStatus.PUBLISHED ? true : false;
	}
	
	public void setCredentialTypeFromString(String typeString) {
		type = CredentialType1.valueOf(typeString.toUpperCase());
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

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
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

	public PublishedStatus getStatus() {
		return status;
	}

	public void setStatus(PublishedStatus status) {
		this.status = status;
	}

	public CredentialType1 getType() {
		return type;
	}

	public void setType(CredentialType1 type) {
		this.type = type;
	}

	public boolean isMandatoryFlow() {
		return mandatoryFlow;
	}

	public void setMandatoryFlow(boolean mandatoryFlow) {
		this.mandatoryFlow = mandatoryFlow;
	}

	public String getTypeString() {
		return typeString;
	}

	public void setTypeString(String typeString) {
		this.typeString = typeString;
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

}
