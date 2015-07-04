package org.prosolo.services.feeds.impl;

import org.prosolo.domainmodel.feeds.FeedSource;
import org.prosolo.services.feeds.FeedSourceManager;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
@Service("org.prosolo.services.feeds.FeedSourceManager")
public class FeedSourceManagerImpl extends AbstractManagerImpl implements FeedSourceManager {
	
	private static final long serialVersionUID = -8374488016509958122L;

	@Override
	@Transactional
	public FeedSource getOrCreateFeedSource(String title, String link) {
		int indexOfSlash = link.lastIndexOf("/");
		
		if (indexOfSlash >= 0 && indexOfSlash == link.length()-1) {
			link = link.substring(0, indexOfSlash);
		}
		
		FeedSource feedSource = getFeedSource(link);
		
		if (feedSource != null) {
			return feedSource;
		} else {
			feedSource = new FeedSource(title, link);
			return saveEntity(feedSource);
		}
	}
	
	@Transactional
	private FeedSource getFeedSource(String link) {
		String query = 
			"SELECT feedSource " +
			"FROM FeedSource feedSource " +
			"WHERE feedSource.link = :link";
		
		FeedSource result = (FeedSource) persistence.currentManager().createQuery(query).
			setString("link", link).
			uniqueResult();
		
		return result;
	}
	
}
