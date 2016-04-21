package org.prosolo.web.administration.data;

import java.io.Serializable;

@Deprecated
public class OrganizationData implements Serializable {

	private static final long serialVersionUID = 5001726153966660904L;

	private String name;
	private String description;
	private String abbreviatedName;
	private String uri;
	private long id;

	public OrganizationData() {}
	
//	public OrganizationData(Organization org) {
//		this.name = org.getTitle();
//		this.description = org.getDescription();
//		//this.uri = org.getUri();
//		this.abbreviatedName = org.getAbbreviatedName();
//		this.setId(org.getId());
//		
//	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getAbbreviatedName() {
		return abbreviatedName;
	}

	public void setAbbreviatedName(String abbreviatedName) {
		this.abbreviatedName = abbreviatedName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
//	public void updateOrganization(Organization organization){
//		if (organization!=null){
//		organization.setTitle(this.getName());
//		organization.setName(this.getName());
//		organization.setAbbreviatedName(this.getAbbreviatedName());
//		organization.setDescription(this.getDescription());
//		}
//	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
