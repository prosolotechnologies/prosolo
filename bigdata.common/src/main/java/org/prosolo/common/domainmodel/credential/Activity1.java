package org.prosolo.common.domainmodel.credential;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

import org.prosolo.common.domainmodel.general.BaseEntity;

@Entity
public class Activity1 extends BaseEntity {

	private static final long serialVersionUID = 15293664172196082L;
	
	private long duration;
	private boolean published;
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
	
	public Activity1() {
		
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

}
