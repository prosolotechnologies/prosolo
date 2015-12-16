package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.ResourceActivity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.UploadAssignmentActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.content.RichContent;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.outcomes.Outcome;
import org.prosolo.common.domainmodel.outcomes.SimpleOutcome;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.activityWall.SocialStreamObserver;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interaction.PostManager;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.web.activitywall.data.AttachmentPreview;
import org.prosolo.web.competences.data.ActivityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.nodes.ActivityManager")
public class ActivityManagerImpl extends AbstractManagerImpl implements	ActivityManager {
	
	private static final long serialVersionUID = -6629875178124643511L;
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ActivityManagerImpl.class);
	
	@Autowired private ResourceFactory resourceFactory;
	@Autowired private EventFactory eventFactory;
	@Autowired private PostManager postManager;

	@Override
	@Transactional
	public Activity createNewActivity(User user, String title, String description, 
			AttachmentPreview attachmentPreview, VisibilityType visType, Collection<Tag> tags) 
					throws EventException {
		return createNewResourceActivity(user, title, description, attachmentPreview, visType, null, false, null);
	}
	
	@Override
	@Transactional
	public Activity createNewActivity(User user, String title,
			String description, AttachmentPreview attachmentPreview, VisibilityType visType, boolean sync,
			String context) throws EventException {
		return createNewResourceActivity(user, title, description, attachmentPreview, visType, null, sync, context);
	}
	
	@Override
	@Transactional(readOnly = false)
	@SuppressWarnings("unchecked")
	public Activity createNewResourceActivity(User user, String title,
			String description, AttachmentPreview attachmentPreview, VisibilityType visType, 
			Collection<Tag> tags, boolean propagateToSocialStreamManualy,
			String context) throws EventException {

		Activity newActivity = resourceFactory.createNewResourceActivity(
				user, 
				title,
				description,
				attachmentPreview, 
				visType,
				tags,
				true);
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("context", context);
		
		if (propagateToSocialStreamManualy) {
			eventFactory.generateEvent(EventType.Create, user, newActivity, new Class[]{SocialStreamObserver.class}, parameters);
		} else {
			eventFactory.generateEvent(EventType.Create, user, newActivity, parameters);
		}
		
		return newActivity;
	}
	
	@Override
	@Transactional
	public boolean checkIfCompletedByUser(User user, Activity activity) {
		if (checkIfUserCompletedActivity(user, activity)) {
			return true;
		}
		if (checkIfActivityInPortfolio(user, activity)) {
			return true;
		}
		return false;
	}
	
	@Override
	@Transactional (readOnly = true)
	public boolean checkIfUserCompletedActivity(User user, Activity activity) {
		String query = 
			"SELECT count(activity) " +
			"FROM User user " +
			"LEFT JOIN user.learningGoals goal " +
			"LEFT JOIN goal.targetCompetences tComp " +
			"LEFT JOIN tComp.targetActivities tActivity " +
			"LEFT JOIN tActivity.activity activity " +
			"WHERE tActivity.completed = :completed " +
				"AND activity = :activity " +
				"AND user = :user";
		
		Long actCount = (Long) persistence.currentManager().createQuery(query).
				setBoolean("completed", true).
				setEntity("activity", activity).
				setEntity("user", user).
				uniqueResult();
		
		if (actCount != null) {
			return actCount > 0;
		}
			
		return false;
	}
	
	@Override
	@Transactional (readOnly = true)
	public boolean checkIfActivityInPortfolio(User user, Activity activity) {
		String query = 
			"SELECT count(activity) " +
			"FROM Portfolio portfolio " +
			"LEFT JOIN portfolio.user user "+
			"LEFT JOIN portfolio.competences achievedComp " +
			"LEFT JOIN achievedComp.targetCompetence tComp " +
			"LEFT JOIN tComp.targetActivities tActivity " +
			"LEFT JOIN tActivity.activity activity " +
			"WHERE activity = :activity " +
				"AND user = :user";
		
		Long actCount = (Long) persistence.currentManager().createQuery(query).
				setEntity("activity", activity).
				setEntity("user", user).
				uniqueResult();
		
		if (actCount != null) {
			return actCount > 0;
		}
		
		return false;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<User> getUsersHavingTargetActivityInLearningGoal(TargetActivity activity) {
		return getUsersHavingTargetActivityInLearningGoal(activity, persistence.currentManager());
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<User> getUsersHavingTargetActivityInLearningGoal(TargetActivity tActivity, Session session) {
		String query=
			"SELECT DISTINCT user " +
			"FROM User user " +
			"LEFT JOIN user.learningGoals tGoal " +
			"LEFT JOIN tGoal.learningGoal goal " +
			"WHERE goal IN (" +
				"SELECT goal1 " +
				"FROM TargetLearningGoal tGoal1 " +
				"LEFT JOIN tGoal1.learningGoal goal1 " +
				"LEFT JOIN tGoal1.targetCompetences tComp1 " +
				"LEFT JOIN tComp1.targetActivities tActivity1 " +
				"WHERE tActivity1 = :tActivity" +
			")";
		
		@SuppressWarnings("unchecked")
		List<User> result = session.createQuery(query)
			.setEntity("tActivity", tActivity)
			.list();
		
		if (result != null && !result.isEmpty()) {
			return result;
		}
		return new ArrayList<User>();
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<TargetActivity> getTargetActivities(Activity activity) {
		String query = 
			"SELECT DISTINCT targetActivity " +
			"FROM TargetActivity targetActivity " +
			"WHERE targetActivity.activity = :activity";
		
		@SuppressWarnings("unchecked")
		List<TargetActivity> result = persistence.currentManager().createQuery(query).
				setEntity("activity", activity).
				list();
		
		if (result != null) {
			return result;
		}
		
		return new ArrayList<TargetActivity>();
	}
	
	//used in JUnit tests only
	@Override
	@Transactional (readOnly = true)
	public List<TargetActivity> getAllTargetActivities() {
		String query = 
			"SELECT DISTINCT targetActivity " +
			"FROM TargetActivity targetActivity " ;
		
		@SuppressWarnings("unchecked")
		List<TargetActivity> result = persistence.currentManager().createQuery(query).
				
				list();
		
		if (result != null) {
			return result;
		}
		
		return new ArrayList<TargetActivity>();
	}
	
	@Override
	@Transactional (readOnly = true)
	public Activity getActivity(long targetActivityId) {
		String query = 
				"SELECT DISTINCT activity " +
				"FROM TargetActivity targetActivity " +
				"LEFT JOIN targetActivity.activity activity " +
				"WHERE targetActivity.id = :targetActivityId";
			
		return (Activity) persistence.currentManager().createQuery(query).
				setLong("targetActivityId", targetActivityId).
				uniqueResult();
	}
	
	@Override
	//@Transactional (readOnly = false)
	public TargetActivity updateTargetActivityWithAssignement(long targetActivityId, String assignmentLink, 
			String assignmentTitle, Session session) throws ResourceCouldNotBeLoadedException {
		
		TargetActivity targetActivity = (TargetActivity) session.load(TargetActivity.class, targetActivityId);
//		targetActivity = HibernateUtil.initializeAndUnproxy(targetActivity);
		
		targetActivity.setAssignmentLink(assignmentLink);
		targetActivity.setAssignmentTitle(assignmentTitle);
		session.saveOrUpdate(targetActivity);
		
		return targetActivity;
	}
	@Override
	public TargetActivity replaceTargetActivityOutcome(long targetActivityId, Outcome outcome, Session session){
		TargetActivity targetActivity = (TargetActivity) session.load(TargetActivity.class, targetActivityId);
		List<Outcome> oldOutcomes = targetActivity.getOutcomes();
		List<Outcome> newOutcomes = new ArrayList<Outcome>();
		newOutcomes.add(outcome);
		targetActivity.setOutcomes(newOutcomes);
		targetActivity.setCompleted(true);
		targetActivity.setDateCompleted(new Date());
		session.save(targetActivity);
		for (Outcome oldOutcome : oldOutcomes) {
			try {
				this.deleteById(SimpleOutcome.class, oldOutcome.getId(), session);
			} catch (ResourceCouldNotBeLoadedException e) {
				e.printStackTrace();
			}
		}
		return targetActivity;
	}
	
	@Override
	@Transactional(readOnly = false)
	public Activity updateActivity(long id, String title, String description,
			ActivityType type, boolean mandatory,
			AttachmentPreview attachmentPreview, int maxNumberOfFiles,
			boolean visibleToEveryone, int duration, User user) throws ResourceCouldNotBeLoadedException {

		Activity activity = loadResource(Activity.class, id, true);
		
		activity.setTitle(title);
		activity.setDescription(description);
		activity.setMandatory(mandatory);
		
		if (type.equals(ActivityType.RESOURCE)) {
			ResourceActivity resActivity = (ResourceActivity) activity;
			
			RichContent richContent = resActivity.getRichContent();
			
			// there was no link, and it is added now
			if (richContent == null && attachmentPreview != null) {
				richContent = postManager.createRichContent(attachmentPreview);
				
				resActivity.setRichContent(richContent);
			} else if (richContent != null && attachmentPreview == null) {
				resActivity.setRichContent(null);
			} else if (richContent != null && attachmentPreview != null) {
				richContent.setContentType(attachmentPreview.getContentType());
				richContent.setTitle(attachmentPreview.getTitle());
				richContent.setDescription(attachmentPreview.getDescription());
				richContent.setLink(attachmentPreview.getLink());
				richContent.setImageUrl(attachmentPreview.getImage());
			}
			
			saveEntity(richContent);
		} else {
			UploadAssignmentActivity assignmentActivity = (UploadAssignmentActivity) activity;
			
			assignmentActivity.setDuration(duration);
			assignmentActivity.setMaxFilesNumber(maxNumberOfFiles);
			assignmentActivity.setVisibleToEveryone(visibleToEveryone);
		}
		
		return saveEntity(activity);
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<Activity> getMockActivities(int limit) {
		String query = 
			"SELECT DISTINCT activity " +
			"FROM Activity activity ";
		
		@SuppressWarnings("unchecked")
		List<Activity> result = persistence.currentManager().createQuery(query)
			.setMaxResults(limit)
			.list();
		
		return result;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<TargetActivity> getComptenceCompletedTargetActivities(long userId, long compId) throws DbConnectionException {
		try{
			String query =
				"SELECT DISTINCT tActivity "+
				"FROM User user "+
				"LEFT JOIN user.learningGoals tGoal "+
				"LEFT JOIN tGoal.targetCompetences tComp "+
				"LEFT JOIN tComp.competence comp "+
				"LEFT JOIN tComp.targetActivities tActivity "+
				"WHERE user.id = :userId " +
					"AND tComp.id = :compId " +
					"AND tActivity.completed = :completed " +
				"ORDER BY tActivity.dateCreated DESC"; 
			
			@SuppressWarnings("unchecked")
			List<TargetActivity> result = persistence.currentManager().createQuery(query)
				.setLong("userId", userId)
				.setLong("compId", compId)
				.setBoolean("completed", true)
				.list();
			
			if (result != null && !result.isEmpty()) {
				return result;
			}
			return new ArrayList<TargetActivity>();
		} catch(Exception e) {
			logger.error(e);
			throw new DbConnectionException("Error while loading activities");
		}
	}
	

}
