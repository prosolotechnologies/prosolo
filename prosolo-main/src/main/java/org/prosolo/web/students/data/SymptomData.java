package org.prosolo.web.students.data;

import org.prosolo.common.domainmodel.observations.Symptom;

public class SymptomData {

	private long id;
	private String description;
	
	public SymptomData(){
		
	}
	
	public SymptomData(Symptom s){
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
