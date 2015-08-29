package org.prosolo.bigdata.dal.persistence.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.dal.persistence.DiggestGeneratorDAO;
import org.prosolo.bigdata.dal.persistence.HibernateUtil;
import org.prosolo.common.domainmodel.feeds.FeedEntry;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.preferences.FeedsPreferences;


public class DiggestGeneratorDAOImpl extends GenericDAOImpl implements
	DiggestGeneratorDAO{
	
	
	
	private static Logger logger = Logger
			.getLogger(DiggestGeneratorDAO.class);
	
	public DiggestGeneratorDAOImpl(){
		setSession(HibernateUtil.getSessionFactory().openSession());
	}
	
	@SuppressWarnings({ "unused", "unchecked" })
	@Override
	public  List<Long> getAllUsersIds() {
		//Session session=openSession();
		String query = 
			"SELECT user.id " +
			"FROM User user " +
			"WHERE user.deleted = :deleted ";
		System.out.println("Query:"+query);
		//@SuppressWarnings("unchecked")
		List<Long> result =null;
		try{
			 result = session.createQuery(query)
					 .setParameter("deleted", false).list();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		//finally{
			//session.close();
		//}
		System.out.println("RESULTS:"+result.size());

		if (result != null) {
			return result;
		}
		return new ArrayList<Long>();
	}
	@Override
	public FeedsPreferences getFeedsPreferences(User user) {
		/*String query = 
			"SELECT DISTINCT feedPreferences " + 
			"FROM FeedsPreferences feedPreferences " + 
			"LEFT JOIN feedPreferences.user user " + 
			"LEFT JOIN FETCH feedPreferences.subscribedRssSources rssSources "+
			"WHERE user.id = :userid  " +
			"AND feedPreferences.class in ('FeedsPreferences')"	;
		
		logger.debug("hb query:" + query);
		
		FeedsPreferences feedsPreferences = (FeedsPreferences) getEntityManager().createQuery(query)
				 .setParameter("userid", user.getId()).getSingleResult();
		*/
		return null;
	}
	@Override
	public FeedsPreferences getFeedsPreferences(long userId) {
		String query = 
			"SELECT DISTINCT feedPreferences " + 
			"FROM FeedsPreferences feedPreferences " + 
			"LEFT JOIN feedPreferences.user user " + 
			"WHERE user.id = :userid  " +
			"AND feedPreferences.class in ('FeedsPreferences')"	;
		System.out.println("FEED PREFERENCES QUERY:"+query+" userid:"+userId);
	
		try{
			FeedsPreferences feedsPreferences = (FeedsPreferences) (FeedsPreferences) session.createQuery(query)
					.setParameter("userid", userId).uniqueResult();
			System.out.println("RETURNING FEED PREFERENCES...");
			System.out.println("RETURNING FEED PREFERENCES..."+feedsPreferences.getId());
			return feedsPreferences;
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
		
	}
	@Override
	public List<FeedEntry> getFeedEntriesFromSources(
			List<FeedSource> feedSources, User user, Date dateFrom) {
		//Session session=openSession();
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
		
		//@SuppressWarnings("unchecked")
		try{
			@SuppressWarnings("unchecked")
			List<FeedEntry> feedEntries = session.createQuery(query)
					.setParameterList("feedSources", feedSources)
					.setDate("dateFrom", dateFrom)
					.setEntity("user", user)
					.list();
			return feedEntries;
		}catch(Exception ex){
			ex.printStackTrace();
		}
	return new ArrayList<FeedEntry>();
		
		
	}
}
