package org.prosolo.bigdata.dal.persistence.impl;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.type.LongType;
import org.prosolo.bigdata.dal.persistence.FeedsDigestDAO;
import org.prosolo.bigdata.dal.persistence.HibernateUtil;

public class FeedsDigestDAOImpl extends GenericDAOImpl implements FeedsDigestDAO {

	private static Logger logger = Logger
			.getLogger(FeedsDigestDAOImpl.class);
	
	public static class FeedsDigestDAOImplHolder {
		public static final FeedsDigestDAOImpl INSTANCE = new FeedsDigestDAOImpl();
	}
	
	public static FeedsDigestDAOImpl getInstance() {
		return FeedsDigestDAOImplHolder.INSTANCE;
	}
	
	public FeedsDigestDAOImpl() {
		setSession(HibernateUtil.getSessionFactory().openSession());
	}
	
	@Override
	public long getFeedsDigestIdForFeedEntry(long entryId) {
		String query = 
			"SELECT entry.feeds_digest as digestId " +
			"FROM feeds_digest_entries entry " +
			"WHERE entry.entries = :entryId";
		try {
			 Long id = (Long) session.createSQLQuery(query)
					 .addScalar("digestId", LongType.INSTANCE)
					 .setLong("entryId", entryId)
					 .uniqueResult();
			 
			 return (id == null ? 0 : id);
		} catch(Exception ex) {
			logger.error(ex);
			ex.printStackTrace();
		}
		
		return 0;
	}
	
	@Override
	public long getFeedsDigestIdForTweet(long tweetId) {
		String query = 
			"SELECT entry.feeds_digest " +
			"FROM feeds_digest_tweets tweet " +
			"WHERE entry.tweets = :tweetId";
		try {
			 Long id = (Long) session.createSQLQuery(query)
					 .setLong("tweetId", tweetId)
					 .uniqueResult();
			 
			 return (id == null ? 0 : id);
		} catch(Exception ex) {
			logger.error(ex);
			ex.printStackTrace();
		}
		
		return 0;
	}
}
