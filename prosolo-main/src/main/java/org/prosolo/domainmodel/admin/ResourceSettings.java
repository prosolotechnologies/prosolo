/**
 * 
 */
package org.prosolo.domainmodel.admin;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Type;

/**
 * @author "Nikola Milikic"
 *
 */
@Entity
public class ResourceSettings implements Serializable {

	private static final long serialVersionUID = -6511094458400187064L;

	private long id;
	
	private boolean userCanCreateCompetence;
	private boolean selectedUsersCanDoEvaluation;
	private boolean individualCompetencesCanNotBeEvaluated;
	
	// used when evaluating a goal
	private boolean goalAcceptanceDependendOnCompetence;

	@Id
	@Column(name = "id", unique = true, nullable = false, insertable = false, updatable = false)
	@GeneratedValue(strategy = GenerationType.TABLE)
	@Type(type = "long")
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Type(type="true_false")
	public boolean isUserCanCreateCompetence() {
		return userCanCreateCompetence;
	}

	public void setUserCanCreateCompetence(boolean userCanCreateCompetence) {
		this.userCanCreateCompetence = userCanCreateCompetence;
	}

	@Type(type="true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isSelectedUsersCanDoEvaluation() {
		return selectedUsersCanDoEvaluation;
	}

	public void setSelectedUsersCanDoEvaluation(boolean selectedUsersCanDoEvaluation) {
		this.selectedUsersCanDoEvaluation = selectedUsersCanDoEvaluation;
	}

	@Type(type="true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isGoalAcceptanceDependendOnCompetence() {
		return goalAcceptanceDependendOnCompetence;
	}

	public void setGoalAcceptanceDependendOnCompetence(
			boolean goalAcceptanceDependendOnCompetence) {
		this.goalAcceptanceDependendOnCompetence = goalAcceptanceDependendOnCompetence;
	}

	@Type(type="true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isIndividualCompetencesCanNotBeEvaluated() {
		return individualCompetencesCanNotBeEvaluated;
	}

	public void setIndividualCompetencesCanNotBeEvaluated(
			boolean individualCompetencesCanNotBeEvaluated) {
		this.individualCompetencesCanNotBeEvaluated = individualCompetencesCanNotBeEvaluated;
	}
	
}
