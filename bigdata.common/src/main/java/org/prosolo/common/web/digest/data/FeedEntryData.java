/**
 * 
 */
package org.prosolo.common.web.digest.data;

import java.io.Serializable;
import org.prosolo.common.domainmodel.feeds.FeedEntry;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.common.web.activitywall.data.UserData;


/**
 * @author "Nikola Milikic"
 * 
 */
public class FeedEntryData implements Serializable {

	private static final long serialVersionUID = -4231339733729391414L;
	
	private long id;
	private String title;
	private String description;
	private String image;
	private String link;
	private String date;
	private UserData maker;
	private String systemLink;
	
	public FeedEntryData(FeedEntry feedEntry) {
		this(feedEntry, feedEntry.getLink());
	}
	
	public FeedEntryData(FeedEntry feedEntry, String link) {
		this.id = feedEntry.getId();
		this.title = feedEntry.getTitle();
		this.description = feedEntry.getDescription();
		this.image = feedEntry.getImage();
		this.link = feedEntry.getLink();
		this.date = DateUtil.getPrettyDate(feedEntry.getDateCreated());
		this.systemLink = link;
		
	//	if (feedEntry.getMaker() != null)
	//		this.maker = new UserData(feedEntry.getMaker());
	}
	
	public FeedEntryData(){
		
	}
	
	

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

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

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
	public UserData getMaker() {
		return maker;
	}

	public void setMaker(UserData maker) {
		this.maker = maker;
	}

	public String getSystemLink() {
		return systemLink;
	}

	public void setSystemLink(String systemLink) {
		this.systemLink = systemLink;
	}

	@Override
	public String toString() {
		return "FeedEntryData [id=" + id + ", title=" + title + ", description=" + description + ", image=" + image + ", link=" + link + ", date="
				+ date + ", maker=" + maker + "]";
	}

}
