package org.prosolo.services.nodes.data;

import java.util.ArrayList;
import java.util.List;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.CredentialType1;

public class BasicCredentialData {

	private String title;
	private String description;
	
	private List<Tag> tags;
	private String tagsString;
	
	private String hashtagsString = "";
	
	private boolean published;
	private PublishedStatus status;
	
	private String typeString;
	private CredentialType1 type;
	
	private boolean mandatoryFlow;
	
	public BasicCredentialData() {
		this.tags = new ArrayList<Tag>();
		setCredentialStatus();
	}

	public static BasicCredentialData copyBasicCourseData(BasicCredentialData data) {
		BasicCredentialData course = new BasicCredentialData();
		course.setTitle(data.getTitle());
		course.setDescription(data.getDescription());
		course.setTags(new ArrayList<>(data.getTags()));
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

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
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

}
