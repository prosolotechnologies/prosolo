package org.prosolo.common.domainmodel.observations;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.user.User;

@Entity
public class Observation {

	private long id;
	private String message;
	private String note;
	private Set<Symptom> symptoms;
	private Set<Suggestion> suggestions;
	private User createdBy;
	private User createdFor;
	private Date creationDate;
	private boolean edited;
	
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
	
	@Column(length = 90000)
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	@Column(length = 90000)
	public String getNote() {
		return note;
	}
	
	public void setNote(String note) {
		this.note = note;
	}
	
	@ManyToMany
	@JoinTable(name = "observation_symptom")
	public Set<Symptom> getSymptoms() {
		return symptoms;
	}
	
	public void setSymptoms(Set<Symptom> symptoms) {
		this.symptoms = symptoms;
	}
	
	@ManyToMany
	@JoinTable(name = "observation_suggestion")
	public Set<Suggestion> getSuggestions() {
		return suggestions;
	}
	
	public void setSuggestions(Set<Suggestion> suggestions) {
		this.suggestions = suggestions;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	public User getCreatedBy() {
		return createdBy;
	}
	
	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	public User getCreatedFor() {
		return createdFor;
	}
	
	public void setCreatedFor(User createdFor) {
		this.createdFor = createdFor;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public boolean isEdited() {
		return edited;
	}

	public void setEdited(boolean edited) {
		this.edited = edited;
	}

}
