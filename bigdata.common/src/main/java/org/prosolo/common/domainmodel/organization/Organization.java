package org.prosolo.common.domainmodel.organization;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.credential.CredentialCategory;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.learningStage.LearningStage;
import org.prosolo.common.domainmodel.user.User;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

/**
 * @author Bojan
 *
 * May 15, 2017
 */

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"title"})})
public class Organization extends BaseEntity {

	private static final long serialVersionUID = -144242317896188428L;

	private List<User> users;
	private List<Unit> units;
	private Set<LearningStage> learningStages;
	private Set<CredentialCategory> credentialCategories;

	private boolean learningInStagesEnabled;

	@OneToMany(mappedBy = "organization")
	public List<User> getUsers(){
		return users;
	}

	public void setUsers(List<User> users){
		this.users = users;
	}

	@OneToMany(mappedBy = "organization")
	public List<Unit> getUnits(){
		return units;
	}
	
	public void setUnits(List<Unit> units){
		this.units = units;
	}

	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isLearningInStagesEnabled() {
		return learningInStagesEnabled;
	}

	public void setLearningInStagesEnabled(boolean learningInStagesEnabled) {
		this.learningInStagesEnabled = learningInStagesEnabled;
	}

	@OneToMany(mappedBy = "organization", cascade = CascadeType.REMOVE, orphanRemoval = true)
	@OrderBy("order ASC")
	public Set<LearningStage> getLearningStages() {
		return learningStages;
	}

	public void setLearningStages(Set<LearningStage> learningStages) {
		this.learningStages = learningStages;
	}

	@OneToMany(mappedBy = "organization", cascade = CascadeType.REMOVE, orphanRemoval = true)
	public Set<CredentialCategory> getCredentialCategories() {
		return credentialCategories;
	}

	public void setCredentialCategories(Set<CredentialCategory> credentialCategories) {
		this.credentialCategories = credentialCategories;
	}
}
