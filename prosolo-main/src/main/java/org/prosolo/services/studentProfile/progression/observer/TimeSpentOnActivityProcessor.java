package org.prosolo.services.studentProfile.progression.observer;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.nodes.ActivityManager;

import com.mongodb.DBObject;

public abstract class TimeSpentOnActivityProcessor {
	
	private static Logger logger = Logger.getLogger(TimeSpentOnActivityProcessor.class);

	protected DBObject event;
	protected long activityId;
	protected ActivityManager activityManager;
	
	public TimeSpentOnActivityProcessor(DBObject event, ActivityManager activityManager) {
		this.event = event;
		this.activityManager = activityManager;
	}
	
	public void updateTime(int eventIndex, List<DBObject> events) {
		if(isActivityStart()) {
			DBObject stopEvent = null;
			int size = events.size();
			for(int i = eventIndex + 1; i < size; i++) {
				DBObject nextEv = events.get(i);
				boolean stopped = checkIfEventStoppedLearning(nextEv);
				if(stopped) {
					stopEvent = nextEv;
					break;
				}
			}
			
			if(stopEvent == null) {
				stopEvent = events.get(size - 1);
			}
			
			long timeSpent = (long) stopEvent.get("timestamp") - (long) event.get("timestamp");
			Session session = (Session) activityManager.getPersistence().openSession();
			try{
				Transaction transaction = session.beginTransaction();
				activityManager.updateTimeSpentOnActivity(activityId, timeSpent, session);
				transaction.commit();
			} catch(Exception e) {
				logger.error(e);
				e.printStackTrace();
			} finally {
				HibernateUtil.close(session);
			}
		} 
	}

	private boolean isActivityStart() {
		if(checkAdditionalConditions()) {
			Pattern pattern = Pattern.compile("learn.targetGoal.(\\d+).targetComp.(\\d+).targetActivity.(\\d+).*");
			
			DBObject params = (DBObject) event.get("parameters");
			String context = (String) params.get("context");
			
			Matcher m = pattern.matcher(context);
	
			if (m.matches()) {
			    this.activityId = Long.parseLong(m.group(3));
			    return true;
			}
		}
		return false;
	}

	protected abstract boolean checkAdditionalConditions();

	protected abstract boolean checkIfEventStoppedLearning(DBObject nextEv);
}
