package org.prosolo.web.competences.data;

import java.io.Serializable;

import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.services.nodes.data.PublishedStatus;
import org.prosolo.util.nodes.AnnotationUtil;

public class BasicCompetenceData implements Serializable{

	private static final long serialVersionUID = -1506416320540794846L;
	
	private long id;
	private String title;
	private String description;
	private int duration = 1;
	private int validity = 1;
	private boolean published;
	private String tagsString;
	private PublishedStatus status;
	
	public BasicCompetenceData() {
	
	}

	public BasicCompetenceData(Competence comp) { 
		this.id = comp.getId();
		this.title = comp.getTitle();
		this.description = comp.getDescription();
		this.validity = comp.getValidityPeriod();
		this.duration = comp.getDuration();
		this.tagsString = AnnotationUtil.getAnnotationsAsSortedCSV(comp.getTags());
		//published
		setPublishedStatus();
	}
	
	public void setPublishedStatus() {
		//change
		status = PublishedStatus.PUBLISHED;
	}
	
	public static BasicCompetenceData copyBasicCompetenceData(BasicCompetenceData data) {
		BasicCompetenceData comp = new BasicCompetenceData();
		comp.setId(data.getId());
		comp.setTitle(data.getTitle());
		comp.setDescription(data.getDescription());
		comp.setValidity(data.getValidity());
		comp.setDuration(data.getDuration());
		comp.setTagsString(data.getTagsString());
		comp.setPublished(data.isPublished());
		comp.setPublishedStatus();
		return comp;
	}

	/*
	 * GETTERS / SETTERS
	 */
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public int getValidity() {
		return validity;
	}

	public void setValidity(int validity) {
		this.validity = validity;
	}
	
	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}

	public PublishedStatus getStatus() {
		return status;
	}

	public void setStatus(PublishedStatus status) {
		this.status = status;
	}

	public String getTagsString() {
		return tagsString;
	}

	public void setTagsString(String tagsString) {
		this.tagsString = tagsString;
	}
	
}
