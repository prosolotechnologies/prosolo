package org.prosolo.services.email.emailLinks.data;

public class LinkObjectData {

	private long id;
	private LinkObjectGeneralInfo info;
	
	public LinkObjectData() {
		
	}
	
	public LinkObjectData(long id, LinkObjectGeneralInfo info) {
		super();
		this.id = id;
		this.info = info;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public LinkObjectGeneralInfo getInfo() {
		return info;
	}

	public void setInfo(LinkObjectGeneralInfo info) {
		this.info = info;
	}
	
}
