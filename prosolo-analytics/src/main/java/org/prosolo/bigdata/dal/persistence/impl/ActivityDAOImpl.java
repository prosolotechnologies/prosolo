package org.prosolo.bigdata.dal.persistence.impl;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;
import org.prosolo.bigdata.dal.persistence.ActivityDAO;
import org.prosolo.bigdata.dal.persistence.HibernateUtil;
import org.prosolo.common.domainmodel.activities.TargetActivity;

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
		setSession(HibernateUtil.getSessionFactory().openSession());
	}
	
	@Override
	public boolean updateTimeSpentOnActivities(Map<Long, Long> activitiesWithTimeSpent) {
		Transaction t = null;
		try{
			t = session.beginTransaction();
			for(Entry<Long, Long> entry : activitiesWithTimeSpent.entrySet()) {
				updateTimeSpentOnActivity(entry.getKey(), entry.getValue());
			}
			t.commit();
			return true;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			t.rollback();
			return false;
		}
	}

	private void updateTimeSpentOnActivity(long id, long timeSpent) throws Exception {
		try {
			TargetActivity targetActivity = (TargetActivity) session.load(TargetActivity.class, id);
			long currentTimeSpent = targetActivity.getTimeSpent();
			targetActivity.setTimeSpent(currentTimeSpent + timeSpent);
		
			session.saveOrUpdate(targetActivity);
		} catch(Exception e) {
			throw new Exception("Error while updating time spent on activity");
		}
	}
}
