package org.prosolo.common.domainmodel.activitywall;

import javax.persistence.*;

import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;

@Entity
public class CompetenceCompleteSocialActivity extends SocialActivity1 {

	private static final long serialVersionUID = 9134195596253201117L;
	
	private TargetCompetence1 targetCompetenceObject;
	private Credential1 parentCredential;

	@ManyToOne(fetch = FetchType.LAZY)
	public TargetCompetence1 getTargetCompetenceObject() {
		return targetCompetenceObject;
	}

	public void setTargetCompetenceObject(TargetCompetence1 targetCompetenceObject) {
		this.targetCompetenceObject = targetCompetenceObject;
	}

	@ManyToOne
	public Credential1 getParentCredential() {
		return parentCredential;
	}

	public void setParentCredential(Credential1 parentCredential) {
		this.parentCredential = parentCredential;
	}
}
