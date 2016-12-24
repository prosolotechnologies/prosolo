package org.prosolo.common.domainmodel.credential;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.UserGroup;

public class CompetenceUserGroup extends BaseEntity {

	private static final long serialVersionUID = -2117827525725237618L;
	
	private Competence1 competence;
	private UserGroup userGroup;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	public Competence1 getCompetence() {
		return competence;
	}
	
	public void setCompetence(Competence1 competence) {
		this.competence = competence;
	}
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	public UserGroup getUserGroup() {
		return userGroup;
	}
	
	public void setUserGroup(UserGroup userGroup) {
		this.userGroup = userGroup;
	}
	
}
