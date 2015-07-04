/**
 * 
 */
package org.prosolo.domainmodel.portfolio;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import org.prosolo.domainmodel.activities.TargetActivity;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.portfolio.AchievedCompetence;
import org.prosolo.domainmodel.portfolio.CompletedResource;

/**
 * @author "Nikola Milikic"
 * 
 */
@Entity
////@Table(name = "portfolio_ExternalCredit")
public class ExternalCredit extends CompletedResource {

	private static final long serialVersionUID = 7108024619109933356L;

	private String certificateLink;
	private Date start;
	private Date end;
	private List<TargetActivity> targetActivities;
	private List<AchievedCompetence> competences;
	
	public ExternalCredit() {
		targetActivities = new ArrayList<TargetActivity>();
		competences = new ArrayList<AchievedCompetence>();
	}

	public String getCertificateLink() {
		return certificateLink;
	}

	public void setCertificateLink(String certificateLink) {
		this.certificateLink = certificateLink;
	}
	
	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	@ManyToMany
	@JoinTable(name = "portfolio_ExternalCredit_TargetActivities")
	public List<TargetActivity> getTargetActivities() {
		return targetActivities;
	}

	public void setTargetActivities(List<TargetActivity> targetActivities) {
		this.targetActivities = targetActivities;
	}

	public boolean addTargetActivity(TargetActivity targetActivity) {
		if (targetActivity != null) {
			return getTargetActivities().add(targetActivity);
		}
		return false;
	}
	
	public boolean removeActivity(TargetActivity targetActivity) {
		if (targetActivity != null) {
			return getTargetActivities().remove(targetActivity);
		}
		return false;
	}
		
	@ManyToMany
	@JoinTable(name = "portfolio_ExternalCredit_Competences")
	public List<AchievedCompetence> getCompetences() {
		return competences;
	}

	public void setCompetences(List<AchievedCompetence> competences) {
		this.competences = competences;
	}
	
	public boolean addAchievedCompetence(AchievedCompetence comp) {
		if (comp != null) {
			return getCompetences().add(comp);
		}
		return false;
	}
	
	@Override
	@Transient
	public BaseEntity getResource() {
		return null;
	}

}
