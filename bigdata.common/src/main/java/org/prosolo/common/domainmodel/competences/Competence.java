package org.prosolo.common.domainmodel.competences;
 
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;

import org.prosolo.common.domainmodel.activities.CompetenceActivity;
import org.prosolo.common.domainmodel.general.Node;

@Entity
////@Table(name="comp_Competence")

public class Competence extends Node {

	private static final long serialVersionUID = -207748172449208770L;

	private CompetenceType type;
	private int validityPeriod;
	private int duration;
	
	private Competence basedOn;

	private List<Competence> prerequisites;
	private List<Competence> corequisites;
	private List<CompetenceActivity> activities;

	public Competence() {
		this.type = CompetenceType.REGULAR;
		this.prerequisites = new ArrayList<Competence>(); 
		this.corequisites = new ArrayList<Competence>(); 
		this.activities = new ArrayList<CompetenceActivity>();
	}
	
	 

	public Competence(CompetenceType type) {
		super();
		this.type = type;
	}
	
	@Enumerated(EnumType.STRING)
	public CompetenceType getType() {
		return type;
	}

	public void setType(CompetenceType type) {
		this.type = type;
	}

	public int getValidityPeriod() {
		return validityPeriod;
	}

	public void setValidityPeriod(int validityPeriod) {
		this.validityPeriod = validityPeriod;
	}
	
	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	@OneToOne(fetch = FetchType.LAZY, cascade={CascadeType.MERGE})
	public Competence getBasedOn() {
		return basedOn;
	}

	public void setBasedOn(Competence basedOn) {
		this.basedOn = basedOn;
	}

	@OneToMany
	public List<Competence> getPrerequisites() {
		return prerequisites;
	}

	public void setPrerequisites(List<Competence> prerequisites) {
		this.prerequisites = prerequisites;
	}

	@OneToMany
	public List<Competence> getCorequisites() {
		return corequisites;
	}

	public void setCorequisites(List<Competence> corequisites) {
		this.corequisites = corequisites;
	}

	@OneToMany(mappedBy="competence", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("activityPosition ASC")
	public List<CompetenceActivity> getActivities() {
		return activities;
	}

	public void setActivities(List<CompetenceActivity> activities) {
		this.activities = activities;
	}
	
	public void addActivity(CompetenceActivity activity) {
		if (activity != null && !this.activities.contains(activity)) {
			this.activities.add(activity);
		}
	}
	
}
