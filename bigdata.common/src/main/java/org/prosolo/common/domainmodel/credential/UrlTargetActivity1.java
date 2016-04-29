package org.prosolo.common.domainmodel.credential;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Entity
public class UrlTargetActivity1 extends TargetActivity1 {

	private static final long serialVersionUID = -6193296173428145886L;
	
	private String url;
	private String linkName;
	private UrlActivityType type;
	
	public UrlTargetActivity1() {
		
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
	
	
}
