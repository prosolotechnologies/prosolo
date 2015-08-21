package org.prosolo.bigdata.dal.persistence.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.dal.persistence.DiggestGeneratorDAO;
import org.prosolo.bigdata.spark.LearningGoalsMostActiveUsersAnalyzer;
import org.prosolo.common.domainmodel.feeds.FeedEntry;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.preferences.FeedsPreferences;


public class DiggestGeneratorDAOImpl extends DAOImpl implements
	DiggestGeneratorDAO{
	
	private static Logger logger = Logger
			.getLogger(DiggestGeneratorDAO.class);
	
	@SuppressWarnings({ "unused", "unchecked" })
	@Override
	public  List<Long> getAllUsersIds() {
		String query = 
			"SELECT user.id " +
			"FROM User user " +
			"WHERE user.deleted = :deleted ";
		System.out.println("Query:"+query);
		//@SuppressWarnings("unchecked")
		List<Long> result =null;
		try{
			 result = getEntityManager().createQuery(query)
					 .setParameter("deleted", false)
						.getResultList();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		System.out.println("RESULTS:"+result.size());

		if (result != null) {
			return result;
		}
		return new ArrayList<Long>();
	}
	@Override
	public FeedsPreferences getFeedsPreferences(User user) {
		String query = 
			"SELECT DISTINCT feedPreferences " + 
			"FROM FeedsPreferences feedPreferences " + 
			"LEFT JOIN feedPreferences.user user " + 
			"LEFT JOIN FETCH feedPreferences.subscribedRssSources rssSources "+
			"WHERE user.id = :userid  " +
			"AND feedPreferences.class in ('FeedsPreferences')"	;
		
		logger.debug("hb query:" + query);
		
		FeedsPreferences feedsPreferences = (FeedsPreferences) getEntityManager().createQuery(query)
				 .setParameter("userid", user.getId()).getSingleResult();
		
		return feedsPreferences;
	}
	@Override
	public List<FeedEntry> getFeedEntriesFromSources(
			List<FeedSource> feedSources, User user, Date dateFrom) {
		if (feedSources == null || feedSources.isEmpty()) {
			return new ArrayList<FeedEntry>();
		}
		
		String query = 
			"SELECT DISTINCT feedEntry " + 
			"FROM FeedEntry feedEntry " +
			"WHERE feedEntry.feedSource IN (:feedSources) " +
				"AND feedEntry.dateCreated > :dateFrom " + 
				"AND feedEntry.subscribedUser = :user " +  
			"ORDER BY feedEntry.relevance ASC, feedEntry.dateCreated DESC";
		
		@SuppressWarnings("unchecked")
		List<FeedEntry> feedEntries = getEntityManager().createQuery(query)
			.setParameter("feedSources", feedSources)
			.setParameter("dateFrom", dateFrom)
			.setParameter("user", user)
			.getResultList();
		
		return feedEntries;
	}
}
