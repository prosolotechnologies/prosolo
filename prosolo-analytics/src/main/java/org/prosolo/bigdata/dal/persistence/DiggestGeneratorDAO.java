package org.prosolo.bigdata.dal.persistence;

 
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.feeds.FeedEntry;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.feeds.SubscribedRSSFeedsDigest;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.preferences.FeedsPreferences;

public interface DiggestGeneratorDAO extends GenericDAO{

	List<Long> getAllUsersIds();

	FeedsPreferences getFeedsPreferences(User user);

	List<FeedEntry> getFeedEntriesFromSources(
			List<FeedSource> subscribedRssSources, User user, Date dateFrom);

	 FeedsPreferences getFeedsPreferences(long userId);

	//FeedsPreferences getFeedsPreferences(long userId, Session session);

 

}
