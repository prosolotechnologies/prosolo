package org.prosolo.common.domainmodel.credential;

import javax.persistence.Entity;

import org.prosolo.common.domainmodel.general.BaseEntity;

@Entity
public class ResourceLink extends BaseEntity {

	private static final long serialVersionUID = 6825538787075030983L;
	
	private String linkName;
	private String url;
	
	/*
	 * Name of a parameter that user id should be attached to. If it is null or empty, it should not be used.
	 */
	private String idParameterName;
	
	public ResourceLink() {
		
	}

	public String getLinkName() {
		return linkName;
	}

	public void setLinkName(String linkName) {
		this.linkName = linkName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getIdParameterName() {
		return idParameterName;
	}

	public void setIdParameterName(String idParameterName) {
		this.idParameterName = idParameterName;
	}

}
