package org.prosolo.common.domainmodel.credential;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;

@Entity
public class CompetenceUserGroup extends BaseEntity {

	private static final long serialVersionUID = -2117827525725237618L;
	
	private Competence1 competence;
	private UserGroup userGroup;
	private UserGroupPrivilege privilege = UserGroupPrivilege.Learn;
	//if true, it means that this user group is inherited from credential
	private boolean inherited;
	//if inherited is true, this references credential from which privilege is inherited, otherwise it should be null
	private Credential1 inheritedFrom;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	public Competence1 getCompetence() {
		return competence;
	}
	
	public void setCompetence(Competence1 competence) {
		this.competence = competence;
	}
	
	@JoinColumn(name = "user_group")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	public UserGroup getUserGroup() {
		return userGroup;
	}
	
	public void setUserGroup(UserGroup userGroup) {
		this.userGroup = userGroup;
	}
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public UserGroupPrivilege getPrivilege() {
		return privilege;
	}
	
	public void setPrivilege(UserGroupPrivilege privilege) {
		this.privilege = privilege;
	}

	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isInherited() {
		return inherited;
	}

	public void setInherited(boolean inherited) {
		this.inherited = inherited;
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	public Credential1 getInheritedFrom() {
		return inheritedFrom;
	}

	public void setInheritedFrom(Credential1 inheritedFrom) {
		this.inheritedFrom = inheritedFrom;
	}
	
	
}
