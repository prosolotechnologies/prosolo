package org.prosolo.web.manage.students.data.observantions;

import org.prosolo.common.domainmodel.observations.Observation;
import org.prosolo.common.domainmodel.observations.Suggestion;
import org.prosolo.common.domainmodel.observations.Symptom;
import org.prosolo.common.util.date.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class ObservationData {

	private long id;
	private Date dateCreated;
	private String message;
	private String note;
	private List<SymptomData> symptoms = new ArrayList<>();
	private List<SuggestionData> suggestions = new ArrayList<>();
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
	
	public void removeCurrentAndAddSymptomsFromList(List<SymptomData> symptoms) {
		this.symptoms.clear();
		this.symptoms.addAll(symptoms);
	}
	
	public void removeCurrentAndAddSuggestionsFromList(List<SuggestionData> suggestions) {
		this.suggestions.clear();
		this.suggestions.addAll(suggestions);
	}

	public long getTime() {
		return DateUtil.getMillisFromDate(dateCreated);
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
