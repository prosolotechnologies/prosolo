package org.prosolo.common.domainmodel.credential;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.prosolo.common.domainmodel.general.BaseEntity;

@Entity
public class TargetCompetence1 extends BaseEntity {

	private static final long serialVersionUID = -841797705769618698L;

	private int progress;
	private TargetCredential1 targetCredential;
	private Competence1 competence;
	private List<TargetActivity1> targetActivities;
	private boolean hiddenFromProfile;
	
	public TargetCompetence1() {
		
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public TargetCredential1 getTargetCredential() {
		return targetCredential;
	}

	public void setTargetCredential(TargetCredential1 targetCredential) {
		this.targetCredential = targetCredential;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public Competence1 getCompetence() {
		return competence;
	}

	public void setCompetence(Competence1 competence) {
		this.competence = competence;
	}

	@OneToMany(mappedBy = "targetCompetence")
	public List<TargetActivity1> getTargetActivities() {
		return targetActivities;
	}

	public void setTargetActivities(List<TargetActivity1> targetActivities) {
		this.targetActivities = targetActivities;
	}

	public boolean isHiddenFromProfile() {
		return hiddenFromProfile;
	}

	public void setHiddenFromProfile(boolean hiddenFromProfile) {
		this.hiddenFromProfile = hiddenFromProfile;
	}
	
}
