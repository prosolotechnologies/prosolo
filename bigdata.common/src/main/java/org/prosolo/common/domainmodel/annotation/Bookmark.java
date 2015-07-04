package org.prosolo.common.domainmodel.annotation;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.prosolo.common.domainmodel.annotation.AnnotationType;
import org.prosolo.common.domainmodel.annotation.SimpleAnnotation;

@Entity
public class Bookmark extends SimpleAnnotation {

	public Bookmark(AnnotationType annType) {
		super(annType);
	}

	private static final long serialVersionUID = 9183456963247569128L;

	/**
	 * the URL of the bookmarked resource (e.g., a web page)
	 */
	private String bookmarkURL;

	public void setBookmarkURL(String bookmarkURL) {
		this.bookmarkURL = bookmarkURL;
	}

	@Column(name = "bookmarkURL", nullable = true)
	public String getBookmarkURL() {
		return bookmarkURL;
	}
	
}
