package org.prosolo.common.domainmodel.activitywall;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.prosolo.common.domainmodel.credential.TargetCompetence1;

@Entity
public class CompetenceCompleteSocialActivity extends SocialActivity1 {

	private static final long serialVersionUID = 9134195596253201117L;
	
	private TargetCompetence1 targetCompetenceObject;

	@ManyToOne(fetch = FetchType.LAZY)
	public TargetCompetence1 getTargetCompetenceObject() {
		return targetCompetenceObject;
	}

	public void setTargetCompetenceObject(TargetCompetence1 targetCompetenceObject) {
		this.targetCompetenceObject = targetCompetenceObject;
	}

	

}
