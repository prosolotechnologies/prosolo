package org.prosolo.common.domainmodel.credential;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"competence", "user"})})
public class TargetCompetence1 extends BaseEntity {

	private static final long serialVersionUID = -841797705769618698L;

	private int progress;
	private Competence1 competence;
	private User user;
	private List<TargetActivity1> targetActivities;
	private boolean hiddenFromProfile;
	
	private Date dateCompleted;
	
	private long nextActivityToLearnId;

	private Set<CompetenceEvidence> evidences;
	private String evidenceSummary;
	
	public TargetCompetence1() {
		targetActivities = new ArrayList<>();
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
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
	@LazyCollection(LazyCollectionOption.EXTRA)
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
	
	public Date getDateCompleted() {
		return dateCompleted;
	}

	public void setDateCompleted(Date dateCompleted) {
		this.dateCompleted = dateCompleted;
	}
	
	public long getNextActivityToLearnId() {
		return nextActivityToLearnId;
	}

	public void setNextActivityToLearnId(long nextActivityToLearnId) {
		this.nextActivityToLearnId = nextActivityToLearnId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@OneToMany(mappedBy = "competence")
	public Set<CompetenceEvidence> getEvidences() {
		return evidences;
	}

	public void setEvidences(Set<CompetenceEvidence> evidences) {
		this.evidences = evidences;
	}

	@Column(length = 9000)
	public String getEvidenceSummary() {
		return evidenceSummary;
	}

	public void setEvidenceSummary(String evidenceSummary) {
		this.evidenceSummary = evidenceSummary;
	}
}
