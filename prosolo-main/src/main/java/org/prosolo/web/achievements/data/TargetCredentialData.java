package org.prosolo.web.achievements.data;

import java.io.Serializable;

import javax.inject.Inject;

import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author "Musa Paljos"
 *
 */
public class TargetCredentialData implements Serializable {

	private static final long serialVersionUID = 3744828596870425737L;

	private Long id;
	private String description;
	private String title;
	private boolean hiddenFromProfile;
	private String link;	
	

	public TargetCredentialData(long id, String title, String description, boolean hiddenFromProfile, UrlIdEncoder idEncoder) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.hiddenFromProfile = hiddenFromProfile;
		link =  "/credentials.xhtml?id=" + idEncoder.encodeId(id);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isHiddenFromProfile() {
		return hiddenFromProfile;
	}

	public void setHiddenFromProfile(boolean hiddenFromProfile) {
		this.hiddenFromProfile = hiddenFromProfile;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}


}