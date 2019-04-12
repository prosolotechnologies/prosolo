package org.prosolo.search.util.credential;

import org.prosolo.web.util.ResourceBundleUtil;

/**
 * Filter used on Competency Library pages.
 *
 * @author Nikola Milikic
 * @date 2019-01-31
 * @since 1.3
 */
public enum CompetenceLibrarySearchFilter {

	ALL_COMPETENCES("All " + ResourceBundleUtil.getLabel("competence.plural").toLowerCase()),
	ENROLLED("Enrolled"),
	BOOKMARKS("Bookmarks");

	private String label;

	CompetenceLibrarySearchFilter(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
	
}