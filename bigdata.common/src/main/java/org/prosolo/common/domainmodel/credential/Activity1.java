package org.prosolo.common.domainmodel.credential;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.prosolo.common.domainmodel.general.BaseEntity;

@Entity
public class Activity1 extends BaseEntity {

	private static final long serialVersionUID = 15293664172196082L;
	
	private int orderInCompetence;
	private long duration;
	private Competence1 competence;
	private boolean published;
	private Activity1 draftVersion;
	private boolean draft;
	
	public Activity1() {
		
	}

	public int getOrderInCompetence() {
		return orderInCompetence;
	}

	public void setOrderInCompetence(int orderInCompetence) {
		this.orderInCompetence = orderInCompetence;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public Competence1 getCompetence() {
		return competence;
	}

	public void setCompetence(Competence1 competence) {
		this.competence = competence;
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

}
