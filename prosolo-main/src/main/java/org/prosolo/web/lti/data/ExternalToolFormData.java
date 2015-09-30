package org.prosolo.web.lti.data;

import java.io.Serializable;

/**
 * @author Nikola Milikic
 * @version 0.5
 *			
 */
public class ExternalToolFormData implements Serializable {
	
	private static final long serialVersionUID = 8543622282202374274L;
	
	private String title;
	private String description;
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
}
