package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.CompetenceActivity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.course.CreatorType;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.data.activity.ActivityData;
import org.prosolo.services.nodes.data.activity.attachmentPreview.AttachmentPreview;
import org.prosolo.services.nodes.data.activity.mapper.ActivityMapperFactory;
import org.prosolo.services.nodes.data.activity.mapper.activityData.ActivityDataMapper;
import org.prosolo.web.activitywall.data.ActivityWallData;
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
	@Inject private ActivityManager activityManager;
	
	@Override
	@Transactional
	public TargetCompetence createNewTargetCompetence(long userId, String title, String description, 
			int validity, int duration, Collection<Tag> tags, VisibilityType visibility) throws EventException, ResourceCouldNotBeLoadedException {
		
		Competence newCompetence = resourceFactory.createCompetence(userId, title, description,
				validity, duration, tags, null, null, new Date());
		TargetCompetence tc = resourceFactory.createNewTargetCompetence(userId, newCompetence, visibility);
		eventFactory.generateEvent(EventType.Create, userId, newCompetence);
		return tc;
	}

	@Override
	@Transactional
	public Competence createCompetence(long userId, String title,
			String description, int validity, int duration, Collection<Tag> tags, 
			List<Competence> prerequisites, List<Competence> corequisites) throws EventException, ResourceCouldNotBeLoadedException {
		Competence newCompetence = resourceFactory.createCompetence(userId, title, description,
				validity, duration, tags, prerequisites, corequisites, new Date());

		eventFactory.generateEvent(EventType.Create, userId, newCompetence);
		
		return newCompetence;
	}
	@Override
	public Competence createCompetence(long userId, String title,
			String description, int validity, int duration, Collection<Tag> tags, 
			List<Competence> prerequisites, List<Competence> corequisites, Date dateCreated) throws EventException, ResourceCouldNotBeLoadedException {
		
		Competence newCompetence = resourceFactory.createCompetence(userId, title, description,
				validity, duration, tags, prerequisites, corequisites, dateCreated);

		eventFactory.generateEvent(EventType.Create, userId, newCompetence);
		
		return newCompetence;
	}
	
	@Override
	@Transactional(readOnly = false)
	public Competence createNewUntitledCompetence(long userId, CreatorType creatorType) throws DbConnectionException {
		try {
			return createCompetence(userId, "Untitled", "", 1, 1, null, null, null, new Date());
		} catch(Exception e) {
			throw new DbConnectionException("Error while creating new competence");
		}
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

		CompetenceActivity compActivity = new CompetenceActivity(competence, competence.getActivities().size(), activity);
		compActivity = saveEntity(compActivity);
		
		//competence.addActivity(compActivity);
		//saveEntity(competence);
		
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
					
					//changed relationship between competence and competence activity
					CompetenceActivity compActivity = new CompetenceActivity(competence, actData.getPosition(), activity);
					compActivity = saveEntity(compActivity);
					
					//competence.addActivity(compActivity);
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
			}
		}
		
		return saveEntity(competence);
	}
	
	@Override
	@Transactional (readOnly = true)
	public boolean isUserAcquiringCompetence(long competenceId, long userId) {
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
				.setLong("userId", userId)
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
	public boolean hasUserCompletedCompetence(long competenceId, long userId) {
		String query = 
			"SELECT COUNT(DISTINCT user) " +
			"FROM User user " +
			"LEFT JOIN user.learningGoals goal " +
			"LEFT JOIN goal.targetCompetences tComp " +
			"LEFT JOIN tComp.competence comp "+
			"WHERE comp.id = :competenceId " +
			"AND tComp.completed = :completed " +
			"AND user.id = :user";
		
		Long result = (Long) persistence.currentManager().createQuery(query)
				.setLong("competenceId", competenceId)
				.setLong("userId",userId)
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
			"INNER JOIN tComp.targetActivities tActivity " +
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
	public void updateTargetCompetenceProgress(long targetCompId, boolean completed, int progress) throws ResourceCouldNotBeLoadedException {
		TargetCompetence tComp = loadResource(TargetCompetence.class, targetCompId);
		
		tComp.setCompletedDay(new Date());
		tComp.setCompleted(completed);
		tComp.setProgress(progress);
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
	
	@Override
	@Transactional(readOnly = true)
	public List<TargetCompetence> getTargetCompetencesForTargetLearningGoal(long goalId) throws DbConnectionException{
		try {
			String query = 
				"SELECT tComp " +
				"FROM TargetLearningGoal tGoal " +
				"LEFT JOIN tGoal.targetCompetences tComp "+
				"WHERE tGoal.id = :goalId " + 
					"AND tComp.id > 0 ";
			
			@SuppressWarnings("unchecked")
			List<TargetCompetence> result = persistence.currentManager().createQuery(query)
				.setLong("goalId", goalId)
				.list();
				
			if (result != null && !result.isEmpty()) {
				return result;
			}
			return new ArrayList<TargetCompetence>();
		} catch (Exception e) {
			throw new DbConnectionException("Error while loading competences");
		}
	}
	
	@Override
	@Transactional
	public void updateCompetenceProgress(long compId, int progress) throws DbConnectionException {
		try{
			TargetCompetence tc = loadResource(TargetCompetence.class, compId);
			tc.setProgress(progress);
		    saveEntity(tc);
		}catch(Exception e){
			throw new DbConnectionException("Error while saving progress");
		}
	}

	@Override
	@Transactional(readOnly=false)
	public Competence updateCompetence(long id, String title, String description, int duration, int validity,
			boolean published, HashSet<Tag> tags) throws DbConnectionException {
		try {
			Competence competence = (Competence) persistence.currentManager().load(Competence.class, id);
			
			competence.setTitle(title);
			competence.setDescription(description);
			competence.setDuration(duration);
			competence.setValidityPeriod(validity);
			competence.setTags(tags);
		
			return competence;
		} catch (Exception e) {
			throw new DbConnectionException("Error while saving competence");
		}
	}
	
	@Override
	@Transactional(readOnly=true)
	public List<ActivityData> getCompetenceActivities(long compId) throws DbConnectionException {
		try {
			String query = 
					"SELECT cAct.id, cAct.activityPosition, act " +
					"FROM Competence comp " +
					"LEFT JOIN comp.activities cAct " +
					"INNER JOIN cAct.activity act " +
					"WHERE comp.id = :compId " + 
				    "ORDER BY cAct.activityPosition";
			
			@SuppressWarnings("unchecked")
			List<Object[]> result = persistence.currentManager()
					.createQuery(query)
					.setLong("compId", compId)
					.list();
			
			if(result == null) {
				return new ArrayList<>();
			}
			List<ActivityData> activities = new ArrayList<>();
			for(Object[] obj : result) {
				Activity activity = (Activity) obj[2];
				if(activity != null) {
					ActivityDataMapper mapper = ActivityMapperFactory.getActivityDataMapper(activity);
					if(mapper != null) {
						ActivityData act = mapper.mapToActivityData();
						act.setCompetenceActivityId((long) obj[0]);
						act.setOrder((long) obj[1]);
	
						activities.add(act);
					}
				}

			}
			return activities;
		} catch(Exception e) {
			throw new DbConnectionException("Error while loading competence activities");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public String getCompetenceTitle(long compId) throws DbConnectionException {
		try {
			String query = 
					"SELECT comp.title " +
					"FROM Competence comp " +
					"WHERE comp.id = :compId";
			
			String res = (String) persistence.currentManager().createQuery(query).
					setLong("compId", compId).
					uniqueResult();
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading competence title");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public CompetenceActivity saveCompetenceActivity(long compId, ActivityData activityData,
			LearningContextData context) throws DbConnectionException {
		try {
			CompetenceActivity compAct = new CompetenceActivity();
			Competence comp = (Competence) persistence.currentManager().load(Competence.class, compId);
			compAct.setCompetence(comp);
			compAct.setActivityPosition(activityData.getOrder());
			Activity activity = null;
			if(activityData.getActivityId() > 0) {
				activity = (Activity) persistence.currentManager().
						load(Activity.class, activityData.getActivityId());
			} else {
				activity = resourceFactory.createNewActivity(activityData);
				eventFactory.generateEvent(EventType.Create, activity.getMaker().getId(), activity, null, 
						context.getPage(), context.getLearningContext(), context.getService(), null);
			}
			compAct.setActivity(activity);
			saveEntity(compAct);
		
			return compAct;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving competence activity");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public void deleteCompetenceActivity(ActivityData activityData,
			List<ActivityData> changedActivities, long userId, 
			LearningContextData context) throws DbConnectionException {
		try {
			resourceFactory.deleteCompetenceActivityInSeparateTransaction(
					activityData.getCompetenceActivityId());
			long activityId = activityData.getActivityId();
			updateOrderOfCompetenceActivities(changedActivities);
			boolean isReferenced = activityManager.checkIfActivityIsReferenced(activityId);
			if(!isReferenced) {
				activityManager.deleteActivity(activityId, activityData.getActivityClass(), userId, context);
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while deleting competence activity");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public void updateOrderOfCompetenceActivities(List<ActivityData> activities) throws DbConnectionException {
		try {
			for(ActivityData ad : activities) {
				CompetenceActivity ca = (CompetenceActivity) persistence.currentManager().
						load(CompetenceActivity.class, ad.getCompetenceActivityId());
				ca.setActivityPosition(ad.getOrder());
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating activities");
		}
	}

	@Transactional
	@Override
	public void updateHiddenTargetCompetenceFromProfile(long compId, boolean hiddenFromProfile)
			throws DbConnectionException {
		try {
			String query = 
				"UPDATE TargetCompetence1 targetComptence1 " +
				"SET targetComptence1.hiddenFromProfile = :hiddenFromProfile " +
				"WHERE targetComptence1.id = :compId ";
	
			persistence.currentManager()
				.createQuery(query)
				.setLong("compId", compId)
				.setBoolean("hiddenFromProfile", hiddenFromProfile)
				.executeUpdate();
		} catch (Exception e) {
			logger.error(e);
			throw new DbConnectionException("Error while updating hiddenFromProfile field of a competence " + compId);
		}
	}

	@SuppressWarnings({ "unchecked" })
	@Transactional
	@Override
	public List<TargetCompetence1> getAllCompletedCompetences(long userId, boolean onlyPubliclyVisible) throws DbConnectionException {
		try {
			String query =
				"SELECT targetComptence1 " +
				"FROM TargetCompetence1 targetComptence1 " +
				"WHERE targetComptence1.targetCredential.id IN (" +
					"SELECT targetCredential1.id " +
					"FROM TargetCredential1 targetCredential1 " + 
					"WHERE targetCredential1.user.id = :userId " +
				") " + 
			    "AND targetComptence1.progress = 100 ";
			
			if (onlyPubliclyVisible) {
				query += " AND targetComptence1.hiddenFromProfile = false ";
			}
			
			query += "ORDER BY targetComptence1.title";
			
			return persistence.currentManager()
					.createQuery(query)
					.setLong("userId", userId)
					.list();
		} catch (DbConnectionException e) {
			e.printStackTrace();
			throw new DbConnectionException();
		}
	}

	@SuppressWarnings({ "unchecked" })
	@Transactional (readOnly = true)
	@Override
	public List<TargetCompetence1> getAllInProgressCompetences(long userId, boolean onlyPubliclyVisible) throws DbConnectionException {
		try {
			String query =
				"SELECT targetComptence1 " +
				"FROM TargetCompetence1 targetComptence1 " +
				"WHERE targetComptence1.targetCredential.id IN (" +
					"SELECT targetCredential1.id " +
					"FROM TargetCredential1 targetCredential1 " + 
					"WHERE targetCredential1.user.id = :userId " +
				") " + 
			    "AND targetComptence1.progress < 100 ";
			
			if (onlyPubliclyVisible) {
				query += " AND targetComptence1.hiddenFromProfile = false ";
			}
			
			query += "ORDER BY targetComptence1.title";
			
			return persistence.currentManager()
					.createQuery(query)
					.setLong("userId", userId)
					.list();
		} catch (DbConnectionException e) {
			e.printStackTrace();
			throw new DbConnectionException();
		}
	}
}
