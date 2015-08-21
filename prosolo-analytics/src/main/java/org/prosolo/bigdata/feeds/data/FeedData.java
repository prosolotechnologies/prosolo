package org.prosolo.bigdata.feeds.data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
public class FeedData {
	
	private String title;
	private String link;
	private String description;
	private String language;
	private String copyright;
	private List<FeedMessageData> entries = new ArrayList<FeedMessageData>();
	
	public FeedData() {}
	
	public FeedData(String title, String link, String description, String language, String copyright) {
		this.title = title;
		this.link = link;
		this.description = description;
		this.language = language;
		this.copyright = copyright;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getLink() {
		return link;
	}
	
	public void setLink(String link) {
		this.link = link;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}
	
	public String getCopyright() {
		return copyright;
	}
	
	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}
	
	public List<FeedMessageData> getEntries() {
		return entries;
	}
	
	public void setEntries(List<FeedMessageData> entries) {
		this.entries = entries;
	}
	
	@Override
	public String toString() {
		return "Feed [copyright=" + copyright + ", description=" + description + ", language=" + language + ", link=" + link + ", title=" + title
				+ "]";
	}
}
