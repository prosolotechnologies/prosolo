package org.prosolo.services.nodes.data;

public class ResourceLinkData {

	private long id;
	private String url;
	private boolean urlInvalid;
	private String linkName;
	private boolean linkNameInvalid;
	/*
	 * name of uploaded file
	 */
	private String fetchedTitle;
	private ObjectStatus status;
	private String idParamName;
	
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

	public String getFetchedTitle() {
		return fetchedTitle;
	}

	public void setFetchedTitle(String fetchedTitle) {
		this.fetchedTitle = fetchedTitle;
	}

	public boolean isUrlInvalid() {
		return urlInvalid;
	}

	public void setUrlInvalid(boolean urlInvalid) {
		this.urlInvalid = urlInvalid;
	}

	public boolean isLinkNameInvalid() {
		return linkNameInvalid;
	}

	public void setLinkNameInvalid(boolean linkNameInvalid) {
		this.linkNameInvalid = linkNameInvalid;
	}

	public String getIdParamName() {
		return idParamName;
	}

	public void setIdParamName(String idParamName) {
		this.idParamName = idParamName;
	}
	
}
