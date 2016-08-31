package org.prosolo.common.domainmodel.credential;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Activity1 extends BaseEntity {

	private static final long serialVersionUID = 15293664172196082L;
	
	private long duration;
	private boolean published;
	private Set<ResourceLink> links;
	private Set<ResourceLink> files;
	/**
	 * @deprecated since v0.5
	 */
	@Deprecated
	private boolean uploadAssignment;
	private ActivityResultType resultType;
	private LearningResourceType type;
	
	private Activity1 draftVersion;
	/** 
	 * means that this credential instance is just a draft
	 * version of some other credential
	 */
	private boolean draft;
	/**
	 * tells if credential has draft version of
	 * credential which means that credential was
	 * published once but is changed and has draft
	 * version
	 */
	private boolean hasDraft;
	
	private User createdBy;
	
	private GradingOptions gradingOptions;
	
	public Activity1() {
		links = new HashSet<>();
		files = new HashSet<>();
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}

	@OneToOne(fetch = FetchType.LAZY)
	public Activity1 getDraftVersion() {
		return draftVersion;
	}

	public void setDraftVersion(Activity1 draftVersion) {
		this.draftVersion = draftVersion;
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

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<ResourceLink> getLinks() {
		return links;
	}

	public void setLinks(Set<ResourceLink> links) {
		this.links = links;
	}

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<ResourceLink> getFiles() {
		return files;
	}

	public void setFiles(Set<ResourceLink> files) {
		this.files = files;
	}

	public boolean isUploadAssignment() {
		return uploadAssignment;
	}

	public void setUploadAssignment(boolean uploadAssignment) {
		this.uploadAssignment = uploadAssignment;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}
	
	@Enumerated(EnumType.STRING)
	public LearningResourceType getType() {
		return type;
	}

	public void setType(LearningResourceType type) {
		this.type = type;
	}
	
	@Enumerated(EnumType.STRING)
	public ActivityResultType getResultType() {
		return resultType;
	}

	public void setResultType(ActivityResultType resultType) {
		this.resultType = resultType;
	}

	@OneToOne
	public GradingOptions getGradingOptions() {
		return gradingOptions;
	}

	public void setGradingOptions(GradingOptions gradingOptions) {
		this.gradingOptions = gradingOptions;
	}
	
}
