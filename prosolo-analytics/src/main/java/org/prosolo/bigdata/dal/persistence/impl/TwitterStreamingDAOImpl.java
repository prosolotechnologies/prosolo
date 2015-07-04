package org.prosolo.bigdata.dal.persistence.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.dal.persistence.TwitterStreamingDAO;
import org.prosolo.bigdata.twitter.StreamListData;
import org.prosolo.common.domainmodel.user.LearningGoal;


/**
@author Zoran Jeremic Jun 21, 2015
 *
 */

public class TwitterStreamingDAOImpl implements TwitterStreamingDAO{

	private static Logger logger = Logger.getLogger(TwitterStreamingDAOImpl.class);
	@Override
	public Map<String, StreamListData> readAllHashtagsAndLearningGoalsIds() {
		EntityManager em = org.prosolo.bigdata.dal.persistence.EntityManagerUtil.getEntityManagerFactory()
				.createEntityManager();
		LearningGoal lg;
		String query = 
			"SELECT DISTINCT hashtag.title, lGoal.id " +
			"FROM LearningGoal lGoal " +
			"LEFT JOIN lGoal.hashtags hashtag WHERE hashtag.id > 0";
		
		logger.debug("hb query:" + query);
		List<Object> result =  em.createQuery(query).getResultList();
		
		Map<String, StreamListData> hashtagsLearningGoalIds = new HashMap<String, StreamListData>();
			 
		if (result != null) {
			Iterator<Object> resultIt = result.iterator();
			
			while (resultIt.hasNext()) {
				Object[] object = (Object[]) resultIt.next();
				String title = (String) object[0];
				Long lgId = (Long) object[1];
				
				if (hashtagsLearningGoalIds.containsKey(title)) {
					hashtagsLearningGoalIds.get(title).addGoalId(lgId);
				} else {
					StreamListData listData = new StreamListData(title);
					listData.addGoalId(lgId);
					hashtagsLearningGoalIds.put(title, listData);
				}
			}
		}
		System.out.println("FOUND DATA:"+hashtagsLearningGoalIds.size());
		return hashtagsLearningGoalIds;
	}
}

