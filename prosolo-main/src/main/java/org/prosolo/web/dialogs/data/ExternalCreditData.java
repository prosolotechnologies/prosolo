/**
 * 
 */
package org.prosolo.web.dialogs.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.organization.Visible;
import org.prosolo.common.domainmodel.portfolio.ExternalCredit;
import org.prosolo.web.activitywall.data.ActivityWallData;

/**
 * @author "Nikola Milikic"
 *
 */
public class ExternalCreditData implements Serializable, Visible {

	private static final long serialVersionUID = 6733855323086190492L;

	private long id;
	private String title;
	private String description;
	private String certificateLink;
	private VisibilityType visibility;
	public Date start;
	public Date end;
	private List<ActivityWallData> activities;
	private List<Competence> competences;
	private ExternalCredit externalCredit;
	
	private long badgeCount;
	private long evaluationCount;
	private long rejectedEvaluationCount;
	
	public ExternalCreditData() {
		activities = new ArrayList<ActivityWallData>();
		competences = new ArrayList<Competence>();
	}
	
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
	
	public String getCertificateLink() {
		return certificateLink;
	}
	
	public String getCertificateName() {
		return certificateLink.substring(certificateLink.lastIndexOf("/")+1);
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

	public List<ActivityWallData> getActivities() {
		return activities;
	}

	public void setActivities(List<ActivityWallData> activities) {
		this.activities = activities;
	}

	public boolean addActivity(ActivityWallData activity) {
		if (activity != null) {
			return getActivities().add(activity);
		}
		return false;
	}
	
	public boolean removeActivity(ActivityWallData activity) {
		if (activity != null) {
			Iterator<ActivityWallData> iterator = getActivities().iterator();
			
			while (iterator.hasNext()) {
				ActivityWallData activityWallData = (ActivityWallData) iterator.next();
				
				if (activityWallData.equals(activity)) {
					iterator.remove();
					break;
				}
			}
		}
		return false;
	}
	
	private ActivityWallData activityToDelete;
	
	public void removeActivity(){
		removeActivity(activityToDelete);
	}
	
	public ActivityWallData getActivityToDelete() {
		return activityToDelete;
	}

	public void setActivityToDelete(ActivityWallData activityToDelete) {
		this.activityToDelete = activityToDelete;
	}

	public List<Competence> getCompetences() {
		return competences;
	}

	public void setCompetences(List<Competence> competences) {
		this.competences = competences;
	}

	public boolean addCompetence(Competence competence) {
		if (competence != null) {
			return getCompetences().add(competence);
		}
		return false;
	}
	
	public boolean removeCompetence(Competence competence) {
		if (competence != null) {
			Iterator<Competence> iterator = getCompetences().iterator();
			
			while (iterator.hasNext()) {
				Competence c = (Competence) iterator.next();
				
				if (c.equals(competence)) {
					iterator.remove();
					break;
				}
			}
		}
		return false;
	}

	public VisibilityType getVisibility() {
		return visibility;
	}

	public void setVisibility(VisibilityType visibility) {
		this.visibility = visibility;
	}

	public ExternalCredit getExternalCredit() {
		return externalCredit;
	}

	public void setExternalCredit(ExternalCredit externalCredit) {
		this.externalCredit = externalCredit;
	}

	public long getBadgeCount() {
		return badgeCount;
	}

	public void setBadgeCount(long badgeCount) {
		this.badgeCount = badgeCount;
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

	@Override
	public boolean equals(Object obj) {
		return ((ExternalCreditData) obj).getId() == this.id;
	}
	
}
