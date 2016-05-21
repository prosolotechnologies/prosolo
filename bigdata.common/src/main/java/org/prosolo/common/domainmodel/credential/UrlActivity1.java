package org.prosolo.common.domainmodel.credential;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Entity
public class UrlActivity1 extends Activity1 {

	private static final long serialVersionUID = 8370618847078805918L;
	
	private String url;
	private String linkName;
	private UrlActivityType urlType;
	
	public UrlActivity1() {
		
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
	public UrlActivityType getUrlType() {
		return urlType;
	}

	public void setUrlType(UrlActivityType type) {
		this.urlType = type;
	}
	
	
}
