package org.prosolo.bigdata.dal.persistence.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.dal.persistence.ActivityDAO;
import org.prosolo.bigdata.dal.persistence.HibernateUtil;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;

public class ActivityDAOImpl extends GenericDAOImpl implements ActivityDAO {

	private static Logger logger = Logger
			.getLogger(ActivityDAOImpl.class);
	
	public static class ActivityDAOImplHolder {
		public static final ActivityDAOImpl INSTANCE = new ActivityDAOImpl();
	}
	
	public static ActivityDAOImpl getInstance() {
		return ActivityDAOImplHolder.INSTANCE;
	}
	
	public ActivityDAOImpl(){
		
	}
	
	@Override
	public boolean updateTimeSpentOnActivities(Map<Long, Long> activitiesWithTimeSpent) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction t = null;
		try{
			t = session.beginTransaction();
			for(Entry<Long, Long> entry : activitiesWithTimeSpent.entrySet()) {
				updateTimeSpentOnActivity(entry.getKey(), entry.getValue(), session);
			}
			t.commit();
			return true;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			if(t != null) {
				t.rollback();
			}
			return false;
		} finally {
			session.close();
		}
	}

	private void updateTimeSpentOnActivity(long id, long timeSpent, Session session) throws Exception {
		try {
			TargetActivity1 targetActivity = (TargetActivity1) session.load(TargetActivity1.class, id);
			long currentTimeSpent = targetActivity.getTimeSpent();
			targetActivity.setTimeSpent(currentTimeSpent + timeSpent);
			logger.info("Time spent on target activity with id " + id + " increased for " + timeSpent);
			session.saveOrUpdate(targetActivity);
		} catch(Exception e) {
			throw new Exception("Error while updating time spent on activity");
		}
	}

	//TODO cred-redesign-07
	@Override
	public List<TargetActivity1> getTargetActivities(long targetCompId, boolean loadActivity) 
			throws Exception {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction t = null;
		try {
			t = session.beginTransaction();
			TargetCompetence1 targetComp = (TargetCompetence1) session.load(
					TargetCompetence1.class, targetCompId);
			
			StringBuilder query = new StringBuilder("SELECT targetAct " +
					       "FROM TargetActivity1 targetAct ");
			
			if(loadActivity) {
				query.append("INNER JOIN FETCH targetAct.activity ");
			}
			
			query.append("WHERE targetAct.targetCompetence = :targetComp " +
					     "ORDER BY targetAct.order");

			@SuppressWarnings("unchecked")
			List<TargetActivity1> res = session
				.createQuery(query.toString())
				.setEntity("targetComp", targetComp)
				.list();
			t.commit();
			if(res == null) {
				return new ArrayList<>();
			}
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			if(t != null) {
				t.rollback();
			}
			throw new Exception("Error while loading activities");
		} finally {
			session.close();
		}
	}
	
	@Override
	public List<Long> getTimeSpentOnActivityForAllUsersSorted(long activityId) throws Exception {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction t = null;
		try {
			t = session.beginTransaction();
			String query =
				"SELECT tActivity.timeSpent " +
				"FROM TargetActivity1 tActivity "+
				"INNER JOIN tActivity.activity activity "+
				"WHERE activity.id = :activityId " +
				"AND (tActivity.timeSpent != :timeSpent " +
					"OR tActivity.completed = :completed) " +
				"ORDER BY tActivity.timeSpent ASC"; 
			
			@SuppressWarnings("unchecked")
			List<Long> result = session.createQuery(query)
				.setLong("activityId", activityId)
				.setLong("timeSpent", 0)
				.setBoolean("completed", true)
				.list();
			t.commit();
			if (result != null && !result.isEmpty()) {
				return result;
			}
			return new ArrayList<Long>();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			if(t != null) {
				t.rollback();
			}
			throw new Exception("Error while loading time spent on activity");
		} finally {
			session.close();
		}
	}
	
	@Override
	public int getPercentileGroup(List<Long> times, long timeSpentForObservedActivity) {
		final int numberOfGroups = 5;
		
		int size = times.size();
		if (size < numberOfGroups) {
			return calculateGroupSpecific(times, timeSpentForObservedActivity);
		}
		
		double percentile = (double) 1 / numberOfGroups;
		BigDecimal bd = BigDecimal.valueOf(percentile);
		for(int i = 1; i <= numberOfGroups; i++) {
			//double currentPercentile = percentile * i;
			BigDecimal currentPercentile = bd.multiply(BigDecimal.valueOf(i));
			long upperBound = getKthPercentile(times, currentPercentile);
			if(upperBound >= timeSpentForObservedActivity) {
				return i;
			}
		}
		
		return 1;
	}
	
	//algorithm is working only when number of groups is 5
	private int calculateGroupSpecific(List<Long> times, long timeSpentForObservedActivity) {
		int size = times.size();
		switch (size) {
		case 1:
			return 3;
		case 2:
			if (times.get(0) == timeSpentForObservedActivity) {
				return 1;
			} else {
				return 5;
			}
		case 3:
			if (times.get(0) == timeSpentForObservedActivity) {
				return 1;
			} else if(times.get(1) == timeSpentForObservedActivity) {
				return 3;
			} else {
				return 5;
			}
		case 4:
			if (times.get(0) == timeSpentForObservedActivity) {
				return 1;
			} else if (times.get(1) == timeSpentForObservedActivity) {
				return 2;
			} else if (times.get(2) == timeSpentForObservedActivity) {
				return 3;
			} else {
				return 5;
			}
		default:
			return 1;
		}
	}

	private long getKthPercentile(List<Long> times, BigDecimal currentPercentile) {
		BigDecimal k = currentPercentile.multiply(BigDecimal.valueOf(times.size()));
		//check if k is whole number
		if(!isIntegerValue(k)) {
			k = k.setScale(0, RoundingMode.CEILING);
		}
		
		int index = k.intValueExact() - 1;
		return times.get(index);
	}
	
	private boolean isIntegerValue(BigDecimal bd) {
		  return bd.signum() == 0 || bd.scale() <= 0 || bd.stripTrailingZeros().scale() <= 0;
	}
	
	@Override
	public void publishActivitiesForCompetences(List<Long> compIds) 
			throws DbConnectionException {
		//TODO cred-redesign-07
//		Session session = HibernateUtil.getSessionFactory().openSession();
//		Transaction t = null;
//		try {
//			t = session.beginTransaction();
//			//get all draft activities
//			List<Activity1> acts = getDraftActivitiesFromCompetences(compIds, session);
//			for(Activity1 a : acts) {
//				a.setPublished(true);
//			}
//			t.commit();
//		} catch(Exception e) {
//			logger.error(e);
//			e.printStackTrace();
//			if(t != null) {
//				t.rollback();
//			}
//			throw new DbConnectionException("Error while publishing activities");
//		} finally {
//			session.close();
//		}
	}
	
	private List<Activity1> getDraftActivitiesFromCompetences(List<Long> compIds, Session session) {
		String query = "SELECT act FROM CompetenceActivity1 cAct " +
					   "INNER JOIN cAct.activity act " +
					   "WHERE cAct.competence.id IN (:compIds) " +
					   "AND act.published = :published";
		
		Query q = session
				.createQuery(query)
				.setParameterList("compIds", compIds)
				.setBoolean("published", false);
		
		@SuppressWarnings("unchecked")
		List<Activity1> activities = q.list();
		return activities != null ? activities : new ArrayList<>();
	}
}
