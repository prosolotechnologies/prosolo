package org.prosolo.common.domainmodel.user.preferences;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.user.TimeFrame;
import org.prosolo.common.domainmodel.user.preferences.UserPreference;

/**
 * @author Zoran Jeremic 2013-08-15
 * 
 */
@Entity
public class FeedsPreferences extends UserPreference {

	private static final long serialVersionUID = 4442713474763193465L;

	private FeedSource personalBlogSource;
	private List<FeedSource> subscribedRssSources;
	private TimeFrame updatePeriod;


	public FeedsPreferences() {
		subscribedRssSources = new ArrayList<FeedSource>();
	}

	@OneToMany
	public List<FeedSource> getSubscribedRssSources() {
		return subscribedRssSources;
	}

	public void setSubscribedRssSources(List<FeedSource> subscribedRssSource) {
		this.subscribedRssSources = subscribedRssSource;
	}

	public void addSubscribedRssSource(FeedSource blogSource) {
		if (!subscribedRssSources.contains(blogSource)) {
			subscribedRssSources.add(blogSource);
		}
	}
	
	@OneToOne
	public FeedSource getPersonalBlogSource() {
		return personalBlogSource;
	}
	
	public void setPersonalBlogSource(FeedSource personalBlogSource) {
		this.personalBlogSource = personalBlogSource;
	}
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public TimeFrame getUpdatePeriod() {
		return updatePeriod;
	}

	public void setUpdatePeriod(TimeFrame updatePeriod) {
		this.updatePeriod = updatePeriod;
	}
 
}
