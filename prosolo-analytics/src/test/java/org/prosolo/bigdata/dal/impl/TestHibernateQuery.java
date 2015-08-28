package org.prosolo.bigdata.dal.impl;



import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.junit.Test;
import org.prosolo.bigdata.dal.persistence.DiggestGeneratorDAO;
import org.prosolo.bigdata.dal.persistence.HibernateUtil;
import org.prosolo.bigdata.dal.persistence.impl.DiggestGeneratorDAOImpl;
import org.prosolo.bigdata.feeds.ResourceTokenizer;
import org.prosolo.bigdata.feeds.impl.ResourceTokenizerImpl;
import org.prosolo.common.domainmodel.feeds.FeedEntry;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.user.User;

public class TestHibernateQuery {

	@Test
	public void testQuery() {
		Calendar cal=Calendar.getInstance();
			    cal.add(Calendar.DATE,-1);
			    Date dateFrom=cal.getTime();
		 Session session=HibernateUtil.getSessionFactory().openSession();
		 Long userid=(long) 27;
		 DiggestGeneratorDAO diggestGeneratorDAO=new DiggestGeneratorDAOImpl();
		 ResourceTokenizer resourceTokenizer=new ResourceTokenizerImpl();
			diggestGeneratorDAO.setSession(session);
			User user=(User) session.load(User.class, userid);
			String userTokenizedString = resourceTokenizer.getTokenizedStringForUser(user);
			System.out.println("TOKENIZED STRING:"+userTokenizedString);
			List<FeedSource> subscribedRssSources = diggestGeneratorDAO.getFeedsPreferences(userid).getSubscribedRssSources();
			System.out.println("HAVE SOURCES:"+subscribedRssSources.size());
				
			for (FeedSource feedSource : subscribedRssSources) {
				System.out.println("Parse feed:"+feedSource.getId());
			//	parseRSSFeed(null, user, feedSource, userTokenizedString);
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
//				List<FeedEntry> feedEntries = persistence.currentManager().createQuery(query)
//				.setParameterList("feedSources", feedSources)
//				.setDate("dateFrom", dateFrom)
//				.setEntity("user", user)
//				.list();
				List<FeedEntry> feedEntries = session.createQuery(query)
						.setParameterList("feedSources", subscribedRssSources)
						.setDate("dateFrom", dateFrom)
						.setEntity("user", user)
						.list();
				
			}catch(Exception ex){
				ex.printStackTrace();
			}
	}

}
