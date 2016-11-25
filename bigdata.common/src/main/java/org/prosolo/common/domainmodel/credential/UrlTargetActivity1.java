package org.prosolo.common.domainmodel.credential;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;

@Entity
public class UrlTargetActivity1 extends TargetActivity1 {

	private static final long serialVersionUID = -6193296173428145886L;
	
	private String url;
	private String linkName;
	private UrlActivityType type;
	//captions for videos
	private Set<ResourceLink> captions;
		
	public UrlTargetActivity1() {
		captions = new HashSet<>();
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getLinkName() {
		return linkName;
	}

	public void setLinkName(String linkName) {
		this.linkName = linkName;
	}

	@Enumerated(EnumType.STRING)
	public UrlActivityType getType() {
		return type;
	}

	public void setType(UrlActivityType type) {
		this.type = type;
	}
	
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<ResourceLink> getCaptions() {
		return captions;
	}

	public void setCaptions(Set<ResourceLink> captions) {
		this.captions = captions;
	}
	
}
