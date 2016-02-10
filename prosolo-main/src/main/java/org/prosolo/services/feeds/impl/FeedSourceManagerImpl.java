package org.prosolo.services.feeds.impl;

import java.util.List;

import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.services.feeds.FeedSourceManager;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
@Service("org.prosolo.bigdata.feeds.FeedSourceManager")
public class FeedSourceManagerImpl extends AbstractManagerImpl implements FeedSourceManager {
	
	private static final long serialVersionUID = -8374488016509958122L;

	@Override
	@Transactional
	public FeedSource getOrCreateFeedSource(String title, String link) {
		FeedSource feedSource = getFeedSource(link);
		
		if (feedSource != null) {
			return feedSource;
		} else {
			return createFeedSource(title, link);
		}
	}

	@Override
	@Transactional (readOnly = false)
	public FeedSource createFeedSource(String title, String link) {
		int indexOfSlash = link.lastIndexOf("/");
		
		if (indexOfSlash >= 0 && indexOfSlash == link.length()-1) {
			link = link.substring(0, indexOfSlash);
		}
		
		FeedSource feedSource = new FeedSource(title, link);
		return saveEntity(feedSource);
	}
	
	@Override
	@Transactional (readOnly = true)
	public FeedSource getFeedSource(String link) {
		int indexOfSlash = link.lastIndexOf("/");
		
		if (indexOfSlash >= 0 && indexOfSlash == link.length()-1) {
			link = link.substring(0, indexOfSlash);
		}
		
		String query = 
			"SELECT feedSource " +
			"FROM FeedSource feedSource " +
			"WHERE feedSource.link = :link";
		
		@SuppressWarnings("unchecked")
		List<FeedSource> result = persistence.currentManager().createQuery(query).
			setString("link", link).
			list();
		
		if(result == null || result.isEmpty()) {
			return null;
		}
		
		return result.get(0);
	}
	
}
