package org.prosolo.common.domainmodel.credential;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

import org.prosolo.common.domainmodel.credential.visitor.ActivityVisitor;

@Entity
public class UrlActivity1 extends Activity1 {

	private static final long serialVersionUID = 8370618847078805918L;
	
	private String url;
	private String linkName;
	private UrlActivityType urlType;
	//captions for videos
	private Set<ResourceLink> captions;
	
	public UrlActivity1() {
		captions = new HashSet<>();
	}
	
	@Override
	public void accept(ActivityVisitor visitor) {
		visitor.visit(this);
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
	
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<ResourceLink> getCaptions() {
		return captions;
	}

	public void setCaptions(Set<ResourceLink> captions) {
		this.captions = captions;
	}
	
	
}
