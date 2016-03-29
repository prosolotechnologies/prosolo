package org.prosolo.services.nodes.data;

import java.util.List;

public class CompetenceData1 {

	private long credentialCompetenceId;
	private long competenceId;
	private String title;
	private String description;
	private String durationString;
	private int order;
	private boolean published;
	private PublishedStatus status;
	private List<BasicActivityData> activities;
	
	private int progress;
	private ResourceCreator creator;
	
	public CompetenceData1() {
		setCompStatus();
	}
	
	//setting competence status based on published flag
	public void setCompStatus() {
		this.status = this.published ? PublishedStatus.PUBLISHED : PublishedStatus.DRAFT;
	}
	
	//setting published flag based on course status
	public void setPublished() {
		this.published = status == PublishedStatus.PUBLISHED ? true : false;
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
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
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
		this.published = published;
		setCompStatus();
	}

	public PublishedStatus getStatus() {
		return status;
	}

	public void setStatus(PublishedStatus status) {
		this.status = status;
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
	
}
