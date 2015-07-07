/**
 * 
 */
package org.prosolo.web.goals.data;

import java.io.Serializable;

import org.prosolo.common.domainmodel.competences.Competence;

/**
 * @author "Nikola Milikic"
 * 
 */
public class CompetenceData implements Serializable {

	private static final long serialVersionUID = 5424735656154476246L;

	private long id;
	private String title;
	
	public CompetenceData(Competence competence) {
		this.id = competence.getId();
		this.title = competence.getTitle();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
