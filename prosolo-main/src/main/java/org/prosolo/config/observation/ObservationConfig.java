package org.prosolo.config.observation;

import java.util.List;

public class ObservationConfig {

	private List<String> symptoms;
	private List<String> suggestions;
	
	public List<String> getSymptoms() {
		return symptoms;
	}
	public void setSymptoms(List<String> symptoms) {
		this.symptoms = symptoms;
	}
	public List<String> getSuggestions() {
		return suggestions;
	}
	public void setSuggestions(List<String> suggestions) {
		this.suggestions = suggestions;
	}
	
}
