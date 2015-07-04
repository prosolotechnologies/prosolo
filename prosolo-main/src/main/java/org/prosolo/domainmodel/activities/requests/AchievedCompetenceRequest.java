package org.prosolo.domainmodel.activities.requests;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.domainmodel.activities.requests.Request;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.portfolio.AchievedCompetence;

@Entity
public class AchievedCompetenceRequest extends Request {

	private static final long serialVersionUID = 5272071162853877L;

	private AchievedCompetence achievedCompetenceResource;
	
	@OneToOne
	public AchievedCompetence getAchievedCompetenceResource() {
		return achievedCompetenceResource;
	}

	public void setAchievedCompetenceResource(AchievedCompetence achievedCompetenceResource) {
		this.achievedCompetenceResource = achievedCompetenceResource;
	}

	@Override
	@Transient
	public BaseEntity getResource() {
		return achievedCompetenceResource;
	}

	@Override
	public void setResource(BaseEntity resource) {
		this.achievedCompetenceResource = (AchievedCompetence) resource;
	}
	 

}
