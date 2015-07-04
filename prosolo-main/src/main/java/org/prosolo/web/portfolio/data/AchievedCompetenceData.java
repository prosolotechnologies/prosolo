/**
 * 
 */
package org.prosolo.web.portfolio.data;

import java.io.Serializable;

import org.prosolo.domainmodel.organization.VisibilityType;
import org.prosolo.domainmodel.organization.Visible;
import org.prosolo.domainmodel.portfolio.AchievedCompetence;

/**
 * @author "Nikola Milikic"
 * 
 */
public class AchievedCompetenceData implements Serializable, Visible, Comparable<AchievedCompetenceData> {

	private static final long serialVersionUID = 4812207364722158862L;

	private long id;
	private String title;
	private String dateCompleted;
	private String status;
	private long targetCompetenceId;
	private long competenceId;
	private VisibilityType visibility;
	private long badgeCount;
	private long evaluationCount;
	private long rejectedEvaluationCount;
	
	private AchievedCompetence competence;
	
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

	public String getDateCompleted() {
		return dateCompleted;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public long getTargetCompetenceId() {
		return targetCompetenceId;
	}

	public void setTargetCompetenceId(long targetCompetenceId) {
		this.targetCompetenceId = targetCompetenceId;
	}
	
	public long getCompetenceId() {
		return competenceId;
	}

	public void setCompetenceId(long competenceId) {
		this.competenceId = competenceId;
	}

	public void setDateCompleted(String dateCompleted) {
		this.dateCompleted = dateCompleted;
	}

	public VisibilityType getVisibility() {
		return visibility;
	}

	public void setVisibility(VisibilityType visibility) {
		this.visibility = visibility;
	}

	public long getBadgeCount() {
		return badgeCount;
	}

	public void setBadgeCount(long badgeCount) {
		this.badgeCount = badgeCount;
	}
	
	public AchievedCompetence getCompetence() {
		return competence;
	}

	public long getEvaluationCount() {
		return evaluationCount;
	}

	public void setEvaluationCount(long evaluationCount) {
		this.evaluationCount = evaluationCount;
	}
	
	public long getRejectedEvaluationCount() {
		return rejectedEvaluationCount;
	}

	public void setRejectedEvaluationCount(long rejectedEvaluationCount) {
		this.rejectedEvaluationCount = rejectedEvaluationCount;
	}

	public void setCompetence(AchievedCompetence competence) {
		this.competence = competence;
	}

	@Override
	public String toString() {
		return "AchievedCompetenceData [id=" + id + ", title=" + title
				+ ", dateCompleted=" + dateCompleted + ", status=" + status
				+ ", targetCompetenceId=" + targetCompetenceId
				+ ", competenceId=" + competenceId + ", visibility="
				+ visibility + ", badgeCount=" + badgeCount
				+ ", evaluationCount=" + evaluationCount + ", competence="
				+ competence + "]";
	}
	
	@Override
	public int compareTo(AchievedCompetenceData o) {
		return this.getTitle().compareTo(o.getTitle());
	}
	
}
