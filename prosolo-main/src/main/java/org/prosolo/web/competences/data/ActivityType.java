package org.prosolo.web.competences.data;

/**
@author Zoran Jeremic Nov 17, 2013
 */

public enum ActivityType {
	RESOURCE ("Resource"),
	ASSIGNMENTUPLOAD("Assignment"),
	EXTERNALTOOL("External LTI Tool");
	
	private final String label;
	private ActivityType(String label){
		this.label=label;
	}
	public String getLabel(){
		return this.label;
	}
}
