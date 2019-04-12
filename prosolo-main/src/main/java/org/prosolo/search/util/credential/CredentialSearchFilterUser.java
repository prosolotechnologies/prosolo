package org.prosolo.search.util.credential;

import org.prosolo.web.util.ResourceBundleUtil;

public enum CredentialSearchFilterUser {

	ALL("All " + ResourceBundleUtil.getLabel("credential.plural").toLowerCase()),
	ENROLLED("Enrolled"),
	BOOKMARKS("Bookmarks");
	
	private String label;
	
	private CredentialSearchFilterUser(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
	
}