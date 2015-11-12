package org.prosolo.web.students.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.prosolo.common.domainmodel.observations.Observation;
import org.prosolo.common.domainmodel.observations.Suggestion;
import org.prosolo.common.domainmodel.observations.Symptom;

public class ObservationData {

	private long id;
	private Date dateCreated;
	private String message;
	private String note;
	private List<SymptomData> symptoms;
	private List<SuggestionData> suggestions;
	private ObservationCreatorData createdBy;
	
	public ObservationData(Observation observation){
		this.id = observation.getId();
		this.dateCreated = observation.getCreationDate();
		this.message = observation.getMessage();
		this.note = observation.getNote();
		this.symptoms = getSymptoms(observation.getSymptoms());
		this.suggestions = getSuggestions(observation.getSuggestions());
		this.createdBy = new ObservationCreatorData(observation.getCreatedBy());
	}
	
	public ObservationData() {
	
	}

	private List<SuggestionData> getSuggestions(Set<Suggestion> suggestions) {
		List<SuggestionData> s = new ArrayList<>();
		for(Suggestion su:suggestions){
			s.add(new SuggestionData(su));
		}
		return s;
	}

	private List<SymptomData> getSymptoms(Set<Symptom> symptoms) {
		List<SymptomData> s = new ArrayList<>();
		for(Symptom sy:symptoms){
			s.add(new SymptomData(sy));
		}
		return s;
	}
	
	public String getFormattedDate(){
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
		return sdf.format(dateCreated);
		
	}

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public Date getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public List<SymptomData> getSymptoms() {
		return symptoms;
	}
	public void setSymptoms(List<SymptomData> symptoms) {
		this.symptoms = symptoms;
	}
	public List<SuggestionData> getSuggestions() {
		return suggestions;
	}
	public void setSuggestions(List<SuggestionData> suggestions) {
		this.suggestions = suggestions;
	}
	public ObservationCreatorData getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(ObservationCreatorData createdBy) {
		this.createdBy = createdBy;
	}
	
	
}
