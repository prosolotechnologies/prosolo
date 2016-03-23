package org.prosolo.services.email.emailLinks.data;

public class Tuple {

	private long id;
	private String value;
	
	public Tuple(long id, String value) {
		this.id = id;
		this.value = value;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
