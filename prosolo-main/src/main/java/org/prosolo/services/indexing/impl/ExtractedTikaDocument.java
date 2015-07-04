package org.prosolo.services.indexing.impl;

/**
 *
 * @author Zoran Jeremic, May 10, 2014
 *
 */
public class ExtractedTikaDocument {
	private String title;
	private String content;
	private String contentType;
	public ExtractedTikaDocument(String title, String content, String contentType){
		setTitle(title);
		setContent(content);
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}
