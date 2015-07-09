package org.prosolo.bigdata.dal.persistence.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.prosolo.bigdata.dal.persistence.TwitterStreamingDAO;
import org.prosolo.bigdata.twitter.StreamListData;
import org.prosolo.common.domainmodel.user.User;

import com.datastax.driver.core.Session;
 


/**
@author Zoran Jeremic Jun 21, 2015
 *
 */

public class TwitterStreamingDAOImpl implements TwitterStreamingDAO{

	private static Logger logger = Logger.getLogger(TwitterStreamingDAOImpl.class);
	
	public void test(){
		EntityManager em = org.prosolo.bigdata.dal.persistence.EntityManagerUtil.getEntityManagerFactory()
				.createEntityManager();
		
		String query="SELECT DISTINCT user FROM User user WHERE user.id >0";
	 	List<User> users=em.createQuery(query).getResultList();
		System.out.println("FOUND USERS:"+users.size());
		for(User u:users){
			System.out.println("FOUND USER:"+u.getLastname());
		}
		em.close();
	}
	public void test2(){
		SessionFactory sessionFactory = org.prosolo.bigdata.dal.persistence.HibernateUtil.getSessionFactory();
		org.hibernate.Session session=sessionFactory.openSession();
		String query="SELECT DISTINCT user FROM User user WHERE user.id >0";
	 	List<User> users=session.createQuery(query).list();
		System.out.println("FOUND USERS:"+users.size());
		for(User u:users){
			System.out.println("FOUND USER:"+u.getLastname());
		}
		session.close();
	}
	@Override
	public Map<String, StreamListData> readAllHashtagsAndLearningGoalsIds() {
		System.out.println("read all hashtags and learning goals ids...");
		EntityManager em = org.prosolo.bigdata.dal.persistence.EntityManagerUtil.getEntityManagerFactory()
				.createEntityManager();
		String query = 
			"SELECT DISTINCT hashtag.title, lGoal.id " +
			"FROM LearningGoal lGoal " +
			"LEFT JOIN lGoal.hashtags hashtag WHERE hashtag.id > 0";
		
		logger.info("hb query:" + query);
		@SuppressWarnings("unchecked")
		List<Object> result =  em.createQuery(query).getResultList();
		
		Map<String, StreamListData> hashtagsLearningGoalIds = new HashMap<String, StreamListData>();
			 
		if (result != null) {
			Iterator<Object> resultIt = result.iterator();
			
			while (resultIt.hasNext()) {
				System.out.println("result");
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
		em.close();
		return hashtagsLearningGoalIds;
	}
}

