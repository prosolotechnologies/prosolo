/**
 * 
 */
package org.prosolo.web.search.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.web.activitywall.data.UserDataFactory;

/**
 * @author "Nikola Milikic"
 * 
 */
public class CompetenceData implements Serializable {

	private static final long serialVersionUID = 8427547746444478807L;

	private long id;
	private String title;
	private String description;
	private User creator;
	private UserData maker;
	private int validity;
	private List<Tag> tags;
	private Competence competence;
	
	public CompetenceData(Competence comp) {
		this.title = comp.getTitle();
		this.description = comp.getDescription();
	 
		this.creator = comp.getMaker();
		this.maker = UserDataFactory.createUserData(comp.getMaker());
 
		this.validity = comp.getValidityPeriod();
		this.tags = new ArrayList<Tag>(comp.getTags());
		this.competence = comp;
		this.setId(comp.getId());
	}

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

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		creator.getName();//initializeCreator to avoid lazy load problem
		this.creator = creator;
	}
	
	public UserData getMaker() {
		return maker;
	}

	public void setMaker(UserData maker) {
		this.maker = maker;
	}

	public int getValidity() {
		return validity;
	}

	public void setValidity(int validity) {
		this.validity = validity;
	}

	public Competence getCompetence() {
		return competence;
	}

	public void setCompetence(Competence competence) {
		this.competence = competence;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

}
