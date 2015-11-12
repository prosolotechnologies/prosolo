package org.prosolo.web.students.data;

import org.prosolo.common.domainmodel.observations.Suggestion;

public class SuggestionData {

	private long id;
	private String description;

	public SuggestionData(){
		
	}
	
	public SuggestionData(Suggestion s){
		this.id = s.getId();
		this.description = s.getDescription();
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
