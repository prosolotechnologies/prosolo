package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.domainmodel.activities.Activity;
import org.prosolo.domainmodel.activities.CompetenceActivity;
import org.prosolo.domainmodel.activities.TargetActivity;
import org.prosolo.domainmodel.activities.events.EventType;
import org.prosolo.domainmodel.annotation.Tag;
import org.prosolo.domainmodel.competences.Competence;
import org.prosolo.domainmodel.competences.TargetCompetence;
import org.prosolo.domainmodel.organization.VisibilityType;
import org.prosolo.domainmodel.user.User;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.activitywall.data.AttachmentPreview;
import org.prosolo.web.competences.data.ActivityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.nodes.CompetenceManager")
public class CompetenceManagerImpl extends AbstractManagerImpl implements CompetenceManager {
	
	private static final long serialVersionUID = 5465009338196055834L;
	
	private static Logger logger = Logger.getLogger(CompetenceManager.class);
	
	@Autowired private EventFactory eventFactory;
	@Autowired private ResourceFactory resourceFactory;
	
	@Override
	@Transactional
	public TargetCompetence createNewTargetCompetence(User user, String title, String description, 
			int validity, int duration, Collection<Tag> tags, VisibilityType visibility) throws EventException {
		
		Competence newCompetence = resourceFactory.createCompetence(user, title, description,
				validity, duration, tags, null, null, new Date());
		TargetCompetence tc = resourceFactory.createNewTargetCompetence(user, newCompetence, visibility);
		eventFactory.generateEvent(EventType.Create, user, newCompetence);
		return tc;
	}

	@Override
	@Transactional
	public Competence createCompetence(User user, String title,
			String description, int validity, int duration, Collection<Tag> tags, 
			List<Competence> prerequisites, List<Competence> corequisites) throws EventException {
		Competence newCompetence = resourceFactory.createCompetence(user, title, description,
				validity, duration, tags, prerequisites, corequisites, new Date());

		eventFactory.generateEvent(EventType.Create, user, newCompetence);
		
		return newCompetence;
	}
	@Override
	public Competence createCompetence(User user, String title,
			String description, int validity, int duration, Collection<Tag> tags, 
			List<Competence> prerequisites, List<Competence> corequisites, Date dateCreated) throws EventException {
		Competence newCompetence = resourceFactory.createCompetence(user, title, description,
				validity, duration, tags, prerequisites, corequisites, dateCreated);

		eventFactory.generateEvent(EventType.Create, user, newCompetence);
		
		return newCompetence;
	}
	@Override
	@Transactional
	@Deprecated
	public Activity createNewActivityAndAddToCompetence(User user,
			String title, String description, ActivityType activityType,
			boolean mandatory, AttachmentPreview attachmentPreview,
			int maxNumberOfFiles, boolean uploadsVisibility, int duration,
			Competence competence) throws EventException {
		
		Activity activity = resourceFactory.createNewActivity(
					user, 
					title, 
					description, 
					activityType, 
					mandatory, 
					attachmentPreview, 
					maxNumberOfFiles, 
					uploadsVisibility, 
					duration, 
					VisibilityType.PUBLIC);

		CompetenceActivity compActivity = new CompetenceActivity(competence.getActivities().size(), activity);
		compActivity = saveEntity(compActivity);
		
		competence.addActivity(compActivity);
		saveEntity(competence);
		
		return activity;
	}
	
	@Override
	@Transactional (readOnly = false)
	public Competence updateCompetence(Competence competence, String title, String description,
			int duration, int validity, Collection<Tag> tags,
			List<Competence> corequisites, List<Competence> prerequisites,
			List<ActivityWallData> activities, boolean updateActivities) {

		competence = merge(competence);
		
		competence.setTitle(title);
		competence.setDescription(description);
		competence.setDuration(duration);
		competence.setValidityPeriod(validity);
		competence.setTags(new HashSet<Tag>(tags));
		competence.setCorequisites(corequisites);
		competence.setPrerequisites(prerequisites);
		
		if (updateActivities) {
			competence.getActivities().clear();
			
			for (ActivityWallData actData : activities) {
				try {
					long activityId = actData.getObject().getId();
					Activity activity = loadResource(Activity.class, activityId);
					
					CompetenceActivity compActivity = new CompetenceActivity(actData.getPosition(), activity);
					compActivity = saveEntity(compActivity);
					
					competence.addActivity(compActivity);
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
			}
		}
		
		return saveEntity(competence);
	}
	
	@Override
	@Transactional (readOnly = true)
	public boolean isUserAcquiringCompetence(long competenceId, User user) {
		String query = 
			"SELECT COUNT(DISTINCT user) " +
			"FROM User user " +
			"LEFT JOIN user.learningGoals goal " +
			"LEFT JOIN goal.targetCompetences tComp " +
			"LEFT JOIN tComp.competence comp "+
			"WHERE comp.id = :competenceId " +
			"AND tComp.completed = :completed " +
			"AND user = :user";
		
		Long result = (Long) persistence.currentManager().createQuery(query)
				.setLong("competenceId", competenceId)
				.setEntity("user", user)
				.setBoolean("completed", false)
				.uniqueResult();
		
		return result > 0;
	}
	
	@Override
	@Transactional (readOnly = true)
	public boolean isUserAcquiringCompetence(Competence comp, User user) {
		String query = 
			"SELECT COUNT(DISTINCT user) " +
			"FROM User user " +
			"LEFT JOIN user.learningGoals goal " +
			"LEFT JOIN goal.targetCompetences tComp " +
			"LEFT JOIN tComp.competence comp "+
			"WHERE comp = :comp " +
				"AND tComp.completed = :completed " +
				"AND user = :user";
		
		Long result = (Long) persistence.currentManager().createQuery(query)
				.setEntity("comp", comp)
				.setEntity("user", user)
				.setBoolean("completed", false)
				.uniqueResult();
		
		return result > 0;
	}
	
	@Override
	@Transactional (readOnly = true)
	public boolean hasUserCompletedCompetence(Competence comp, User user) {
		String query = 
			"SELECT COUNT(DISTINCT user) " +
			"FROM User user " +
			"LEFT JOIN user.learningGoals goal " +
			"LEFT JOIN goal.targetCompetences tComp " +
			"LEFT JOIN tComp.competence comp "+
			"WHERE comp = :comp " +
				"AND tComp.completed = :completed " +
				"AND user = :user";
		
		Long result = (Long) persistence.currentManager().createQuery(query)
				.setEntity("comp", comp)
				.setEntity("user",user)
				.setBoolean("completed", true)
				.uniqueResult();
		
		return result > 0;
	}
	
	@Override
	@Transactional (readOnly = true)
	public boolean hasUserCompletedCompetence(long competenceId, User user) {
		String query = 
			"SELECT COUNT(DISTINCT user) " +
			"FROM User user " +
			"LEFT JOIN user.learningGoals goal " +
			"LEFT JOIN goal.targetCompetences tComp " +
			"LEFT JOIN tComp.competence comp "+
			"WHERE comp.id = :competenceId " +
			"AND tComp.completed = :completed " +
			"AND user = :user";
		
		Long result = (Long) persistence.currentManager().createQuery(query)
				.setLong("competenceId", competenceId)
				.setEntity("user",user)
				.setBoolean("completed", true)
				.uniqueResult();
		
		return result > 0;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<TargetCompetence> getTargetCompetences(long userId, long goalId) {
		String query=
			"SELECT DISTINCT tComp "+
			"FROM User user "+
			"LEFT JOIN user.learningGoals tGoal "+
			"LEFT JOIN tGoal.learningGoal goal "+
			"LEFT JOIN tGoal.targetCompetences tComp "+
			"LEFT JOIN tComp.competence comp "+
			"WHERE user.id = :userId " +
				"AND goal.id = :goalId " +
			"ORDER BY comp.title";
		
		@SuppressWarnings("unchecked")
		List<TargetCompetence> result = persistence.currentManager().createQuery(query)
			.setLong("userId", userId)
			.setLong("goalId", goalId)
			.list();
		
		if (result != null && !result.isEmpty()) {
			return result;
		}
		return new ArrayList<TargetCompetence>();
	}
	@Override
	@Transactional (readOnly = true)
	public Set<Long> getTargetCompetencesIds(long userId, long goalId) {
		Set<Long> targetCompetencesIds = new TreeSet<Long>();
		String query=
			"SELECT DISTINCT tComp.id "+
			"FROM User user "+
			"LEFT JOIN user.learningGoals tGoal "+
			"LEFT JOIN tGoal.learningGoal goal "+
			"LEFT JOIN tGoal.targetCompetences tComp "+
			"LEFT JOIN tComp.competence comp "+
			"WHERE user.id = :userId " +
				"AND goal.id = :goalId " +
			"ORDER BY comp.title";
		
		@SuppressWarnings("unchecked")
		List<Long> result = persistence.currentManager().createQuery(query)
			.setLong("userId", userId)
			.setLong("goalId", goalId)
			.list();
		
		if (result != null && !result.isEmpty()) {
			targetCompetencesIds.addAll(result);
		}
		return targetCompetencesIds;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<User> getMembersOfTargetCompetenceGoal(TargetCompetence tComp, Session session) {
		String query =
			"SELECT DISTINCT user " +
			"FROM User user " +
			"LEFT JOIN user.learningGoals tGoal " +
			"LEFT JOIN tGoal.learningGoal goal " +
			"WHERE goal IN ( " +
				"SELECT DISTINCT goal1 "+
				"FROM TargetLearningGoal tGoal1 "+
				"LEFT JOIN tGoal1.learningGoal goal1 "+
				"LEFT JOIN tGoal1.targetCompetences tComp1 "+
				"WHERE tComp1 = :tComp" +
			")";
		
		@SuppressWarnings("unchecked")
		List<User> result = session.createQuery(query)
			.setEntity("tComp", tComp)
			.list();
		
		if (result != null && !result.isEmpty()) {
			return result;
		}
		return new ArrayList<User>();
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<TargetActivity> getTargetActivities(long targetCompId) {
		String query =
			"SELECT DISTINCT tActivity " +
			"FROM TargetCompetence tComp " +
			"LEFT JOIN tComp.targetActivities tActivity " +
			"WHERE tComp.id = :targetCompId " +
			"ORDER BY tActivity.dateCreated DESC";
		
		@SuppressWarnings("unchecked")
		List<TargetActivity> result = persistence.currentManager().createQuery(query)
			.setLong("targetCompId", targetCompId)
			.list();
		
		if (result != null && !result.isEmpty()) {
			return result;
		}
		return new ArrayList<TargetActivity>();
	}
	@Override
	@Transactional (readOnly = true)
	public Set<Long> getTargetActivitiesIds(long targetCompId) {
		Set<Long> tActivitiesIds=new TreeSet<Long>();
		String query =
			"SELECT DISTINCT tActivity.id " +
			"FROM TargetCompetence tComp " +
			"LEFT JOIN tComp.targetActivities tActivity " +
			"WHERE tComp.id = :targetCompId ";
		
		@SuppressWarnings("unchecked")
		List<Long> result = persistence.currentManager().createQuery(query)
			.setLong("targetCompId", targetCompId)
			.list();
		
		if (result != null && !result.isEmpty()) {
			tActivitiesIds.addAll(result);
		}
		return tActivitiesIds;
	}
	@Override
	@Transactional (readOnly = true)
	public List<Long> getActivitiesIds(long targetCompId) {
		Set<Long> activitiesIds=new TreeSet<Long>();
		String query =
			"SELECT DISTINCT activity.id " +
			"FROM TargetCompetence tComp " +
			"LEFT JOIN tComp.targetActivities tActivity " +
			"LEFT JOIN tActivity.activity activity "+
			"WHERE tComp.id = :targetCompId ";
		
		@SuppressWarnings("unchecked")
		List<Long> result = persistence.currentManager().createQuery(query)
			.setLong("targetCompId", targetCompId)
			.list();
		
		 
		return result;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<TargetActivity> getTargetActivities(long userId, long compId) {
		String query =
			"SELECT DISTINCT tActivity "+
			"FROM User user "+
			"LEFT JOIN user.learningGoals tGoal "+
			"LEFT JOIN tGoal.targetCompetences tComp "+
			"LEFT JOIN tComp.competence comp "+
			"LEFT JOIN tComp.targetActivities tActivity "+
			"WHERE user.id = :userId " +
				"AND comp.id = :compId " +
			"ORDER BY tActivity.dateCreated DESC";
		
		@SuppressWarnings("unchecked")
		List<TargetActivity> result = persistence.currentManager().createQuery(query)
			.setLong("userId", userId)
			.setLong("compId", compId)
			.list();
		
		if (result != null && !result.isEmpty()) {
			return result;
		}
		return new ArrayList<TargetActivity>();
	}
	
	@Override
	@Transactional (readOnly = false)
	public void updateTargetCompetenceProgress(long targetCompId, boolean completed) throws ResourceCouldNotBeLoadedException {
		TargetCompetence tComp = loadResource(TargetCompetence.class, targetCompId);
		
		tComp.setCompletedDay(new Date());
		tComp.setCompleted(completed);
		tComp = saveEntity(tComp);
	}

	@Override
	public TargetCompetence getTargetCompetence(long userId, long compId, long goalId) {
		String query=
			"SELECT DISTINCT tComp "+
			"FROM User user "+
			"LEFT JOIN user.learningGoals tGoal "+
			"LEFT JOIN tGoal.learningGoal goal "+
			"LEFT JOIN tGoal.targetCompetences tComp "+
			"LEFT JOIN tComp.competence comp "+
			"WHERE user.id = :userId " +
				"AND comp.id = :compId " +
				"AND goal.id = :goalId ";
		
		TargetCompetence result = (TargetCompetence) persistence.currentManager().createQuery(query)
			.setLong("userId", userId)
			.setLong("compId", compId)
			.setLong("goalId", goalId)
			.uniqueResult();
		
		return result;
	}
	@Override
	@Transactional (readOnly = true)
	public Set<Long> getCompetencesHavingAttachedActivity(long activityId) {
		Set<Long> competencesIds=new TreeSet<Long>();
		String query =
			"SELECT DISTINCT competence.id " +
			"FROM TargetCompetence tComp " +
			"LEFT JOIN tComp.targetActivities tActivity " +
			"LEFT JOIN tComp.competence competence "+
			"LEFT JOIN tActivity.activity activity "+
			"WHERE activity.id = :activityId ";
		
		@SuppressWarnings("unchecked")
		List<Long> result = persistence.currentManager().createQuery(query)
			.setLong("activityId", activityId)
			.list();
		
		if (result != null && !result.isEmpty()) {
			competencesIds.addAll(result);
		}
		return competencesIds;
	}
	
	@Override
	@Transactional(readOnly = false)
	public boolean disableActivityRecommendations(long targetCompId) {
		try {
			TargetCompetence tComp = loadResource(TargetCompetence.class, targetCompId);
			tComp.setHideActivityRecommendations(true);
			saveEntity(tComp);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
			return false;
		}
		
		return true;
	}
}
