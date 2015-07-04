package org.prosolo.services.feeds.data;

import java.util.Date;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
public class FeedMessageData {
	
	private String title;
	private String description;
	private String link;
	private String author;
	private String guid;
	private Date pubDate;
	private String thumbnail;
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getLink() {
		return link;
	}
	
	public void setLink(String link) {
		this.link = link;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getGuid() {
		return guid;
	}
	
	public void setGuid(String guid) {
		this.guid = guid;
	}
	
	public Date getPubDate() {
		return pubDate;
	}
	
	public void setPubDate(Date pubDate) {
		this.pubDate = pubDate;
	}
	
	public String getThumbnail() {
		return thumbnail;
	}
	
	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}
	
	@Override
	public String toString() {
		return "FeedMessageData [title=" + title + ", description=" + description + ", link=" + link + ", author=" + author + ", guid=" + guid
				+ ", pubDate=" + pubDate + ", thumbnail=" + thumbnail + "]";
	}
	
}