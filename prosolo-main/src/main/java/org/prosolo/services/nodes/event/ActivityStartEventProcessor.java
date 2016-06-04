package org.prosolo.services.nodes.event;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.event.Event;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.goals.LearnBean;
import org.prosolo.web.goals.cache.CompetenceDataCache;
import org.prosolo.web.goals.cache.GoalDataCache;
import org.prosolo.web.goals.cache.LearningGoalPageDataCache;

public abstract class ActivityStartEventProcessor {

	private static Logger logger = Logger.getLogger(ActivityStartEventProcessor.class);
	
	protected Event event;
	protected ApplicationBean applicationBean;
	protected ActivityManager activityManager;
	
	
	public ActivityStartEventProcessor(Event event, ApplicationBean applicationBean, ActivityManager activityManager) {
		this.event = event;
		this.applicationBean = applicationBean;
		this.activityManager = activityManager;
	}
	
	public void updateActivity() {
		Session session = (Session) activityManager.getPersistence().openSession();
		try{
			Transaction transaction = session.beginTransaction();
			ActivityWallData activity = getActivityIfQualified();
			if(activity != null) {
				long id = activity.getId();
				Date now = new Date();
				boolean success = activityManager.updateActivityStartDate(id, now, session);
				if(success) {
					activity.setDateStarted(now);
				}
			}
			transaction.commit();
		}catch(DbConnectionException e) {
			logger.error(e);
		}finally {
			HibernateUtil.close(session);
		}
	}
	public ActivityWallData getActivityIfQualified() {
		boolean qualified = checkSpecificCondition();
		if(qualified) {
			Pattern pattern = Pattern.compile("learn.targetGoal.(\\d+).targetComp.(\\d+).targetActivity.(\\d+).*");
			
			Map<String, String> params = event.getParameters();
			String context = params.get("context");
			if(context == null || context.isEmpty()) {
				return null;
			}
			Matcher m = pattern.matcher(context);
	
			if (m.matches()) {
			    String tGoal = m.group(1);
			    String tComp = m.group(2);
			    String tAct = m.group(3);
			    
			    User user = event.getActor();
			    
			    HttpSession httpSession = applicationBean.getUserSession(user.getId());
				
				if (httpSession != null) {
					LearnBean learnBean = (LearnBean) httpSession.getAttribute("learninggoals");
					LearningGoalPageDataCache goalCache = learnBean.getData();
					List<GoalDataCache> goals = goalCache.getGoals();
					GoalDataCache goal = findGoalById(goals, Long.parseLong(tGoal));
					if(goal != null) {
						CompetenceDataCache comp = findCompetenceById(goal.getCompetences(),  Long.parseLong(tComp));
						if(comp == null) {
							comp = findCompetenceById(goal.getPredefinedCompetences(), Long.parseLong(tComp));
						}
						if(comp != null) {
							ActivityWallData activity = findActivityById(comp.getActivities(),  Long.parseLong(tAct));
							Date dateStarted = activity.getDateStarted();
							if(dateStarted == null) {
								return activity;
							}
						}
					}
				}
			}
		}
		
		return null;
	}
	
	abstract boolean checkSpecificCondition();

	private GoalDataCache findGoalById(List<GoalDataCache> goals, long id) {
		for(GoalDataCache g : goals) {
			if(g.getData().getTargetGoalId() == id) {
				return g;
			}
		}
		return null;
	}
	
	private CompetenceDataCache findCompetenceById(List<CompetenceDataCache> competences, long id) {
		for(CompetenceDataCache c : competences) {
			if(c.getData().getId() == id) {
				return c;
			}
		}
		return null;
	}
	
	private ActivityWallData findActivityById(List<ActivityWallData> activities, long id) {
		for(ActivityWallData a : activities) {
			if(a.getId() == id) {
				return a;
			}
		}
		return null;
	}
}
