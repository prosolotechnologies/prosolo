/**
 * 
 */
package org.prosolo.web.digest.data;

import java.io.Serializable;

import org.prosolo.common.domainmodel.activitywall.TwitterPostSocialActivity;
import org.prosolo.common.domainmodel.feeds.FeedEntry;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.web.activitywall.data.UserData;

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
	
	public FeedEntryData(FeedEntry feedEntry) {
		this.id = feedEntry.getId();
		this.title = feedEntry.getTitle();
		this.description = feedEntry.getDescription();
		this.image = feedEntry.getImage();
		this.link = feedEntry.getLink();
		this.date = DateUtil.getPrettyDate(feedEntry.getDateCreated());
		
		if (feedEntry.getMaker() != null)
			this.maker = new UserData(feedEntry.getMaker());
	}
	
	public FeedEntryData(TwitterPostSocialActivity tweetEntry) {
		this.id = tweetEntry.getId();
		this.title = tweetEntry.getText();
		this.link = tweetEntry.getPostUrl();
		this.date = DateUtil.getPrettyDate(tweetEntry.getDateCreated());
		
		if (tweetEntry.getMaker() != null)
			this.maker = new UserData(tweetEntry.getMaker());
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

	@Override
	public String toString() {
		return "FeedEntryData [id=" + id + ", title=" + title + ", description=" + description + ", image=" + image + ", link=" + link + ", date="
				+ date + ", maker=" + maker + "]";
	}

}
