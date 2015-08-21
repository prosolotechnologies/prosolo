package org.prosolo.bigdata.dal.persistence;

 
import java.util.Date;
import java.util.List;

import org.prosolo.common.domainmodel.feeds.FeedEntry;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.preferences.FeedsPreferences;

public interface DiggestGeneratorDAO extends DAO{

	List<Long> getAllUsersIds();

	FeedsPreferences getFeedsPreferences(User user);

	List<FeedEntry> getFeedEntriesFromSources(
			List<FeedSource> subscribedRssSources, User user, Date dateFrom);

}
