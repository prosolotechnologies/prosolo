package org.prosolo.common.domainmodel.feeds;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.feeds.FeedEntry;
import org.prosolo.common.domainmodel.feeds.FeedSource;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
@Entity
public class FeedEntry extends BaseEntity implements Comparable<FeedEntry> {
	
	private static final long serialVersionUID = 165699492057777684L;
	
	private String link;
	private String image;
	private double relevance;
	private FeedSource feedSource;
	
	// only feeds related to Twitter hashtags have this field set
	private List<String> hashtags;
	
	// when ProSolo user is the creator, we have this field set
	private User maker;

	// user this entry is for. It is personalized as relevance is calculated for each user separatelly
	private User subscribedUser;
	
	public FeedEntry() {
		this.hashtags = new ArrayList<String>();
	}
	
	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}
	
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public double getRelevance() {
		return relevance;
	}

	public void setRelevance(double relevance) {
		this.relevance = relevance;
	}

	@ElementCollection
	public List<String> getHashtags() {
		return hashtags;
	}

	public void setHashtags(List<String> hashtags) {
		this.hashtags = hashtags;
	}

	@OneToOne
	public User getMaker() {
		return maker;
	}

	public void setMaker(User maker) {
		this.maker = maker;
	}

	@OneToOne
	public FeedSource getFeedSource() {
		return feedSource;
	}

	public void setFeedSource(FeedSource feedSource) {
		this.feedSource = feedSource;
	}
	
	@OneToOne
	public User getSubscribedUser() {
		return subscribedUser;
	}

	public void setSubscribedUser(User subscribedUser) {
		this.subscribedUser = subscribedUser;
	}

	@Override
	public int compareTo(FeedEntry o) {
		if (relevance < o.getRelevance()) {
			return -1;
		} else if (relevance == o.getRelevance()) {
			return 0;
		} else {
			return 1;
		}
	}
}
