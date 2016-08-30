package org.prosolo.config.observation;

public class SuggestionConfig {

	private String description;
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		
		SuggestionConfig other = (SuggestionConfig) obj;
		if(this.description == null || other.description == null){
			return false;
		}
		return (this.description.equals(other.description));
	}
	
}
