package org.prosolo.web.util.page;

public enum PageSection {
	STUDENT(""),
	MANAGE("/manage"),
	ADMIN("/admin"),
	;

	private final String prefix;
	
	PageSection(String prefix) {
		this.prefix = prefix;
	}

	public String getPrefix() {
		return prefix;
	}
	
}
