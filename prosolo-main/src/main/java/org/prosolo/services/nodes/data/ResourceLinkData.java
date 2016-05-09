package org.prosolo.services.nodes.data;

public class ResourceLinkData {

	private long id;
	private String url;
	private String linkName;
	private ObjectStatus status;
	
	public ResourceLinkData() {
		
	}
	
	public void statusRemoveTransition() {
		setStatus(ObjectStatusTransitions.removeTransition(getStatus()));
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public ObjectStatus getStatus() {
		return status;
	}

	public void setStatus(ObjectStatus status) {
		this.status = status;
	}
	
}
