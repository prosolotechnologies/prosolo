/**
 * 
 */
package org.prosolo.domainmodel.portfolio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cascade;
import org.prosolo.domainmodel.portfolio.AchievedCompetence;
import org.prosolo.domainmodel.portfolio.CompletedGoal;
import org.prosolo.domainmodel.portfolio.ExternalCredit;
import org.prosolo.domainmodel.portfolio.Portfolio;
import org.prosolo.domainmodel.user.User;

/**
 * @author "Nikola Milikic"
 *
 */
@Entity
//@Table(name="portfolio_Portfolio")
public class Portfolio implements Serializable {
	
	private static final long serialVersionUID = 6028542165277335365L;

	private User user;
	private Set<CompletedGoal> completedGoals;
	private Set<AchievedCompetence> competences;
	private List<ExternalCredit> externalCredits;
	
	public Portfolio(){
		completedGoals = new HashSet<CompletedGoal>();
		competences = new HashSet<AchievedCompetence>();
		externalCredits = new ArrayList<ExternalCredit>();
	}
	
	@Id @OneToOne 
	@JoinColumn (name="id") 
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@OneToMany(fetch = FetchType.LAZY)
	@Cascade({org.hibernate.annotations.CascadeType.MERGE, org.hibernate.annotations.CascadeType.PERSIST})
	@JoinTable(name = "portfolio_Portfolio_CompletedGoal")
	public Set<CompletedGoal> getCompletedGoals() {
		return completedGoals;
	}

	public void setCompletedGoals(Set<CompletedGoal> completedGoals) {
		this.completedGoals = completedGoals;
	}
	
	public boolean addCompletedGoal(CompletedGoal completedGoal) {
		if (completedGoal != null) {
			if (!getCompletedGoals().contains(completedGoal))
				return getCompletedGoals().add(completedGoal);
		}
		return false;
	}

	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE })
	@Cascade({org.hibernate.annotations.CascadeType.MERGE, org.hibernate.annotations.CascadeType.PERSIST})
	@JoinTable(name = "portfolio_Portfolio_AchievedCompetence")
	public Set<AchievedCompetence> getCompetences() {
		return competences;
	}

	public void setCompetences(Set<AchievedCompetence> competences) {
		this.competences = competences;
	}
	
	public boolean addAchievedCompetence(AchievedCompetence achievedCompetence) {
		if (achievedCompetence != null) {
			if (!getCompetences().contains(achievedCompetence))
				return getCompetences().add(achievedCompetence);
		}
		return false;
	}

	@OneToMany(fetch = FetchType.LAZY)
	@Cascade({org.hibernate.annotations.CascadeType.MERGE, org.hibernate.annotations.CascadeType.PERSIST})
	@JoinTable(name = "portfolio_Portfolio_ExternalCredits")
	//@Filter(name="undeleted", condition=":deleted = false")
	public List<ExternalCredit> getExternalCredits() {
		return externalCredits;
	}

	public void setExternalCredits(List<ExternalCredit> externalCredits) {
		this.externalCredits = externalCredits;
	}
	
	public boolean addExternalCredit(ExternalCredit externalCredit) {
		if (externalCredit != null) {
			return getExternalCredits().add(externalCredit);
		}
		return false;
	}
	
	public boolean removeExternalCredit(ExternalCredit externalCredit) {
		if (externalCredit != null) {
			Iterator<ExternalCredit> iterator = externalCredits.iterator();
			
			while (iterator.hasNext()) {
				ExternalCredit exCred = (ExternalCredit) iterator.next();
				
				if (exCred.getId() == externalCredit.getId()) {
					iterator.remove();
					return true;
				}
			}
		}
		return false;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = getUser().hashCode();
		result = (int) (prime * result+getUser().getId());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		
		Portfolio other = (Portfolio) obj;
		if (this.getUser().getId() > 0 && other.getUser().getId() > 0) {
			return this.getUser().getId() == other.getUser().getId();
		}  
		return true;
	}


}
