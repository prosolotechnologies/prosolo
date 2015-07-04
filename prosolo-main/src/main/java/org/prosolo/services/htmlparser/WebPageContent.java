package org.prosolo.services.htmlparser;
/**
 * @author Zoran Jeremic 2013-08-17
 */
public class WebPageContent {
	private String title;
	private String url;
	private String content;
	public WebPageContent(String url2, String title2, String content2) {
		this.url=url2;
		this.title=title2;
		this.content=content2;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}

}
