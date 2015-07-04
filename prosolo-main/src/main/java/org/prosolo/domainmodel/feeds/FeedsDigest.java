package org.prosolo.domainmodel.feeds;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;
import org.prosolo.domainmodel.activitywall.TwitterPostSocialActivity;
import org.prosolo.domainmodel.feeds.FeedEntry;
import org.prosolo.domainmodel.user.TimeFrame;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
@Entity
@DiscriminatorColumn (length = 50)
public class FeedsDigest implements Serializable {
	
	private static final long serialVersionUID = -2522142169180752546L;
	
	private long id;
	private List<FeedEntry> entries;
	private List<TwitterPostSocialActivity> tweets;
	private TimeFrame timeFrame;
	private Date dateCreated;
	
	public FeedsDigest() {
		this.entries = new ArrayList<FeedEntry>();
	}
	
	@Id
	@Column(name = "id", unique = true, nullable = false, insertable = false, updatable = false)
	@GeneratedValue(strategy = GenerationType.TABLE)
	@Type(type = "long")
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@ManyToMany
	public List<FeedEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<FeedEntry> entries) {
		this.entries = entries;
	}

	@Enumerated (EnumType.STRING)
	public TimeFrame getTimeFrame() {
		return timeFrame;
	}

	public void setTimeFrame(TimeFrame timeFrame) {
		this.timeFrame = timeFrame;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created", length = 19)
	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	@ManyToMany
	public List<TwitterPostSocialActivity> getTweets() {
		return tweets;
	}

	public void setTweets(List<TwitterPostSocialActivity> tweets) {
		this.tweets = tweets;
	}
	
}
