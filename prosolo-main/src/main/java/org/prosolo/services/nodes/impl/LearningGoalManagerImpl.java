package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CourseCompetence;
import org.prosolo.common.domainmodel.course.CourseEnrollment;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.activityWall.SocialStreamObserver;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.services.nodes.PortfolioManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.util.nodes.AnnotationUtil;
import org.prosolo.util.nodes.NodeUtil;
import org.prosolo.web.activitywall.data.AttachmentPreview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.nodes.LearningGoalManager")
public class LearningGoalManagerImpl extends AbstractManagerImpl implements LearningGoalManager {
	
	private static final long serialVersionUID = -2098317178138585509L;

	private static Logger logger = Logger.getLogger(LearningGoalManagerImpl.class);
	
	@Autowired private ResourceFactory resourceFactory;
	@Autowired private TagManager tagManager;
	@Autowired private EventFactory eventFactory;
	@Autowired private ActivityManager activityManager;
	@Autowired private CompetenceManager compManager;
	@Autowired private PortfolioManager portfolioManager;
	
	@Override
	@Transactional
	public List<TargetLearningGoal> getUserTargetGoals(User user) {
		return getUserTargetGoals(user, persistence.currentManager());
	}
	
	@Override
	@Transactional
	public List<TargetLearningGoal> getUserTargetGoals(User user, Session session) {
		if (user == null) {
			return null;
		}
		
		String query = 
			"SELECT DISTINCT tGoal " +
			"FROM User user " +
			"LEFT JOIN user.learningGoals tGoal " +
			"LEFT JOIN FETCH tGoal.learningGoal goal " +
			"WHERE user = :user " +
				"AND tGoal.deleted = :deleted " +
			"ORDER BY tGoal.dateCreated ASC";
		
		@SuppressWarnings("unchecked")
		List<TargetLearningGoal> result = session.createQuery(query).
		setEntity("user",user).
		setBoolean("deleted", false).
		list();
		
		return result;
	}
	
	@Override
	@Transactional
	public List<Long> getUserGoalsIds(User user) {
		if (user == null) {
			return null;
		}
		
		String query = 
			"SELECT DISTINCT goal.id " +
			"FROM User user " +
			"LEFT JOIN user.learningGoals tGoal " +
			"LEFT JOIN tGoal.learningGoal goal " +
			"WHERE user = :user " +
				"AND tGoal.deleted = :deleted " +
			"ORDER BY tGoal.dateCreated ASC";
		
		@SuppressWarnings("unchecked")
		List<Long> result = persistence.currentManager().createQuery(query).
		setEntity("user",user).
		setBoolean("deleted", false).
		list();
		return result;
	}

	@Override
	@Transactional (readOnly = false)
	public TargetLearningGoal createNewLearningGoal(User user, String title, String description,
			Date deadline, Collection<Tag> tags, Collection<Tag> hashtags,
			boolean progressActivityDependent) throws EventException, ResourceCouldNotBeLoadedException {
		
		LearningGoal newGoal = resourceFactory.createNewLearningGoal(
				user, 
				title,
				description,
				deadline,
				tags,
				hashtags);
		
		TargetLearningGoal newTargetGoal = createNewTargetLearningGoal(user, newGoal);
		
		// TODO: goal - check whether this should be called for Goal or TargetGoal
		//twitterStreamsManager.addNewHashTagsForLearningGoalAndRestartStream(hashtags, newGoal.getId());
		
		return newTargetGoal;
	}
	
	@Override
	@Transactional (readOnly = false)
	public TargetLearningGoal createNewTargetLearningGoal(User user, long goalId) throws ResourceCouldNotBeLoadedException, EventException {
		LearningGoal goal = loadResource(LearningGoal.class, goalId);
		return createNewTargetLearningGoal(user, goal);
	}
	
	@Override
	@Transactional (readOnly = false)
	public TargetLearningGoal createNewTargetLearningGoal(User user,
		LearningGoal goal) throws EventException, ResourceCouldNotBeLoadedException {
		user = loadResource(User.class, user.getId());
	
		TargetLearningGoal newTargetGoal = resourceFactory.createNewTargetLearningGoal(goal, user, true);
	
		user.addLearningGoal(newTargetGoal);
		user = saveEntity(user);
		flush();
		return newTargetGoal;
	}
	
	@Override
	@Transactional (readOnly = false)
	public TargetLearningGoal createNewCourseBasedLearningGoal(User user, long courseId, LearningGoal courseGoal,
			String context) throws EventException, ResourceCouldNotBeLoadedException {
		
		Course course = loadResource(Course.class, courseId);

		return createNewCourseBasedLearningGoal(user, course, courseGoal, context);
	}
	
	@Override
	@Transactional (readOnly = false)
	public TargetLearningGoal createNewCourseBasedLearningGoal(User user, Course course, LearningGoal courseGoal,
			String context) throws EventException, ResourceCouldNotBeLoadedException {
		if (courseGoal == null) {
			courseGoal = resourceFactory.createNewLearningGoal(
					user, 
					course.getTitle(),
					course.getDescription(),
					null,
					course.getTags(),
					course.getHashtags());
			
			courseGoal = merge(courseGoal);
		}
		
		TargetLearningGoal targetGoal = resourceFactory.createNewTargetLearningGoal(courseGoal, user, true);
		targetGoal = merge(targetGoal);
		targetGoal = saveEntity(targetGoal);
		
		if (course.getCompetences() != null) {
			for (CourseCompetence courseComp : course.getCompetences()) {
				addCompetenceToGoal(user, targetGoal, courseComp.getCompetence(), true, context);
			}
		}
		
		user = merge(user);
		user.addLearningGoal(targetGoal);
		user = saveEntity(user);
		
		// not generating CREATE event as ENROLL_COURSE has already been created
		
		return targetGoal;
	}
	
	@Override
	@Transactional (readOnly = false)
	public LearningGoal updateLearningGoal(LearningGoal learningGoal,
			String title, String description, String tagsString,
			String hashtagsString, Date deadline, boolean freeToJoin) {
		boolean updated = false;
		
		if (!learningGoal.getTitle().equals(title)) {
			learningGoal.setTitle(title);
			updated = true;
		}
		
		if (learningGoal.getDescription()==null || !learningGoal.getDescription().equals(description)) {
			learningGoal.setDescription(description);
			updated = true;
		}
		
		String oldTagsString = AnnotationUtil.getAnnotationsAsSortedCSV(learningGoal.getTags());
		
		if (!oldTagsString.equals(tagsString)) {
			Set<Tag> newTagList = tagManager.parseCSVTagsAndSave(tagsString);
			
			learningGoal.setTags(newTagList);
			updated = true;
		}
		
		String oldHashagsString = AnnotationUtil.getAnnotationsAsSortedCSV(learningGoal.getHashtags());
		
		if (!oldHashagsString.equals(hashtagsString)) {
			Set<Tag> newHashtagList = tagManager.parseCSVTagsAndSave(hashtagsString);
			
			learningGoal.setHashtags(newHashtagList);
			updated = true;
		}
		
		if (deadline != null) {
			if (learningGoal.getDeadline() != null) {
				if (!learningGoal.getDeadline().equals(deadline)) {
					learningGoal.setDeadline(deadline);
					updated = true;
				}
			} else {
				learningGoal.setDeadline(deadline);
				updated = true;
			}
		} else {
			if (learningGoal.getDeadline() != null) {
				learningGoal.setDeadline(deadline);
				updated = true;
			}
		}
		
		
		if (learningGoal.isFreeToJoin() != freeToJoin) {
			learningGoal.setFreeToJoin(freeToJoin);
			updated = true;
		}
		
		if (updated) {
			learningGoal = saveEntity(learningGoal);
		}

		return learningGoal;
	}
	
	@Override
	@Transactional (readOnly = false)
	public TargetLearningGoal updateTargetLearningGoal(
			TargetLearningGoal targetGoal, boolean progressActivityDependent,
			int progress) {
		
		boolean updated = false;
		
		if (targetGoal.getProgress() != progress) {
			targetGoal.setProgress(progress);
			updated = true;
		}		
		
		if (targetGoal.isProgressActivityDependent() != progressActivityDependent) {
			targetGoal.setProgressActivityDependent(progressActivityDependent);
			updated = true;
		}		

		if (updated) {
			recalculateGoalProgress(targetGoal);
			targetGoal = saveEntity(targetGoal);
		}

		return targetGoal;
	}
	
	@Override
	@Transactional (readOnly = false)
	public boolean deleteGoal(long targetGoalId) throws ResourceCouldNotBeLoadedException {
		TargetLearningGoal targetGoal = loadResource(TargetLearningGoal.class, targetGoalId);
		return deleteGoal(targetGoal);
	}
	
	@Override
	@Transactional (readOnly = false)
	public boolean deleteGoal(TargetLearningGoal targetGoal) {
		if (targetGoal != null) {
			targetGoal = markAsDeleted(merge(targetGoal));
			return true;
		}
		return false;
	}
	
	@Override
	@Transactional (readOnly = false)
	public boolean removeLearningGoal(User user, LearningGoal goalToRemove) throws EventException {
		user = merge(user);
		
		if (goalToRemove != null) {
			Iterator<TargetLearningGoal> iterator = user.getLearningGoals().iterator();
			
			while (iterator.hasNext()) {
				TargetLearningGoal targetLearningGoal = (TargetLearningGoal) iterator.next();
				
				if (targetLearningGoal.getLearningGoal().getId() == goalToRemove.getId()) {
					iterator.remove();
					break;
				}
			}
			user = saveEntity(user);
			
			return true;
		}
		return false;
	}
	
	@Override
	@Transactional (readOnly = false)
	public TargetLearningGoal markAsCompleted(User user, TargetLearningGoal targetGoal, String context) throws EventException {
		logger.debug("Marking as complete Target Learning Goal with id " + targetGoal.getId());
		targetGoal = merge(targetGoal);
		
		if (targetGoal.getProgress() != 100) {
			targetGoal.setCompletedDate(new Date());
			targetGoal.setProgress(100);
			targetGoal = saveEntity(targetGoal);
		}
		
		Map<String, String> parameters = new HashMap<String, String>();
		
		if (context != null) {
			parameters.put("context", context);
		}
		
		eventFactory.generateEvent(EventType.Completion, user, targetGoal, parameters);
		return targetGoal;
	}
	
	@Override
	@Transactional (readOnly = false)
	public NodeEvent addCompetenceToGoal(User user, TargetLearningGoal targetGoal, 
			Competence competence, boolean propagateEventAutomatically, String context) throws EventException {
	
		targetGoal = merge(targetGoal);
		// this was causing two TargetCompetences to be created
		// user = merge(user);
		TargetCompetence tc = resourceFactory.createNewTargetCompetence(user, competence, targetGoal.getVisibility());
		
		return addTargetCompetenceToGoal(user, targetGoal, tc, propagateEventAutomatically, context);
	}
		
	@Override
	@Transactional (readOnly = false)
	public NodeEvent addCompetenceToGoal(User user, long targetGoalId, 
			Competence competence, boolean propagateEventAutomatically, String context) throws EventException, ResourceCouldNotBeLoadedException {
		TargetLearningGoal targetGoal = loadResource(TargetLearningGoal.class, targetGoalId);

		return addCompetenceToGoal(user, targetGoal, competence, propagateEventAutomatically, context);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional (readOnly = false)
	public NodeEvent addTargetCompetenceToGoal(User user, TargetLearningGoal targetGoal, 
			TargetCompetence tComp, boolean propagateManuallyToSocialStream, String context) throws EventException {
		
		if (targetGoal != null && tComp != null) {
		 	targetGoal.addTargetCompetence(tComp);
		 	targetGoal = saveEntity(targetGoal);
			
			
			tComp.setParentGoal(targetGoal);
			tComp = saveEntity(tComp);
			
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("context", context);
			Event event = null;
			if (propagateManuallyToSocialStream) {
				event = eventFactory.generateEvent(
						EventType.Attach, 
						user, tComp, 
						targetGoal, 
						new Class[]{SocialStreamObserver.class}, 
						parameters);
			} else {
				event = eventFactory.generateEvent(
						EventType.Attach, 
						user, 
						tComp, 
						targetGoal, 
						parameters);
			}
			return new NodeEvent(tComp, event);
		}
		return null;
	}
	
	@Override
	@Transactional (readOnly = false)
	public TargetActivity cloneAndAttachActivityToTargetCompetence(User user,
			long targetCompetenceId, Activity activity, boolean sync) throws EventException, ResourceCouldNotBeLoadedException {
		TargetCompetence targetCompetence = loadResource(TargetCompetence.class, targetCompetenceId);
		
		if (targetCompetence != null && activity != null) {
			TargetActivity newTargetActivity = resourceFactory.createNewTargetActivity(activity, user);
			
			// set parent TargetCompetence for the TargetActivity
			newTargetActivity.setParentCompetence(targetCompetence);
			newTargetActivity = saveEntity(newTargetActivity);
			
			targetCompetence.addTargetActivity(newTargetActivity);
			saveEntity(targetCompetence);
			
			return newTargetActivity;
		}
		return null;
	}
	
	@Override
	@Transactional (readOnly = false)
	public List<TargetActivity> cloneActivitiesAndAddToTargetCompetence(User user,
			long targetCompId, List<Activity> activities,
			boolean sync, String context) throws EventException, ResourceCouldNotBeLoadedException {
		
		TargetCompetence targetCompetence = loadResource(TargetCompetence.class, targetCompId);
		return cloneActivitiesAndAddToTargetCompetence(user, targetCompetence, activities, sync, context);
	}
	
	@Override
	@Transactional (readOnly = false)
	public List<TargetActivity> cloneActivitiesAndAddToTargetCompetence(User user,
			TargetCompetence targetComp, List<Activity> originalActivities, boolean sync,
			String context) throws EventException {
		
		if (targetComp != null && originalActivities != null) {
			targetComp = merge(targetComp);
			List<TargetActivity> newActivities = new LinkedList<TargetActivity>();
			
			for (Activity activity : originalActivities) {
				activity = merge(activity);
				TargetActivity newTargetActivity = resourceFactory.createNewTargetActivity(activity, user);
				
				// set parent TargetCompetence for the TargetActivity
				newTargetActivity.setParentCompetence(targetComp);
				newTargetActivity = saveEntity(newTargetActivity);
				
				newActivities.add(newTargetActivity);
			}
			
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("context", context);
			parameters.put("activities", NodeUtil.getCSVStringOfIds(originalActivities));
			
			eventFactory.generateEvent(EventType.AttachAll, user, null, targetComp, parameters);
			
			targetComp.getTargetActivities().addAll(newActivities);
			
			saveEntity(targetComp);
			
			return newActivities;
		}
		return null;
	}
	
	@Override
	@Transactional (readOnly = false)
	public TargetActivity createActivityAndAddToTargetCompetence(User user,
			String title, String description, AttachmentPreview attachmentPreview,
			VisibilityType visType, long targetCompetenceId, boolean connectWithStatus,
			String context, String page, String learningContext, String service) 
					throws EventException, ResourceCouldNotBeLoadedException {
		
		Activity newActivity = activityManager.createNewActivity(
				user, 
				title,
				description,
				attachmentPreview, 
				visType, 
				!connectWithStatus,
				context,
				page,
				learningContext,
				service);
		
		return addActivityToTargetCompetence(user, targetCompetenceId, newActivity, context,
				page, learningContext, service);
	}
	
	@Override
	@Transactional (readOnly = false)
	public TargetActivity addActivityToTargetCompetence(User user,
			long targetCompetenceId, Activity activity, String context, String page,
			String learningContext, String service)
			throws EventException, ResourceCouldNotBeLoadedException {
		
		if (activity != null) {
			TargetCompetence targetCompetence = loadResource(TargetCompetence.class, targetCompetenceId);
			TargetActivity newTargetActivity = resourceFactory.createNewTargetActivity(activity, user);
			
			// set parent TargetCompetence for the TargetActivity
			newTargetActivity.setParentCompetence(targetCompetence);
			newTargetActivity = saveEntity(newTargetActivity);
			
			targetCompetence.addTargetActivity(newTargetActivity);
			targetCompetence = saveEntity(targetCompetence);
			
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("context", context);
			
			//migration to new context approach
			eventFactory.generateEvent(EventType.Attach, user, newTargetActivity, targetCompetence, page, learningContext,
					service, parameters);
			return newTargetActivity;
		}
		return null;
	}
	
	@Override
	@Transactional (readOnly = false)
	public TargetActivity addActivityToTargetCompetence(User user, long targetCompetenceId, long activityId, String context) 
			throws EventException, ResourceCouldNotBeLoadedException {
		
		Activity activity = loadResource(Activity.class, activityId);
		return addActivityToTargetCompetence(user, targetCompetenceId, activity, context, null, null, null);
	}
	
	@Override
	@Transactional (readOnly = false)
	public TargetCompetence addActivityToTargetCompetence(User user, 
			TargetCompetence targetCompetence,
			Activity activity, boolean sync) throws EventException {
		
		if (targetCompetence != null && activity != null) {
			TargetActivity newTargetActivity = resourceFactory.createNewTargetActivity(activity, user);
			
			// set parent TargetCompetence for the TargetActivity
			newTargetActivity.setParentCompetence(targetCompetence);
			newTargetActivity = saveEntity(newTargetActivity);
			
			targetCompetence.addTargetActivity(newTargetActivity);
			targetCompetence = saveEntity(targetCompetence);
			
			Map<String, String> parameters = new HashMap<String, String>();
			eventFactory.generateEvent(EventType.Attach, user, newTargetActivity, targetCompetence, parameters);
			return targetCompetence;
		}
		return null;
	}

	@Override
	@Transactional (readOnly = false)
	public GoalTargetCompetenceAnon deleteTargetCompetenceFromGoal(User user, long targetGoalId,
			long targetCompId)
			throws EventException, ResourceCouldNotBeLoadedException {
		
		TargetLearningGoal targetGoal = loadResource(TargetLearningGoal.class, targetGoalId);
		TargetCompetence targetCompetence = loadResource(TargetCompetence.class, targetCompId);
			
		targetGoal.removeCompetence(targetCompetence);
		
		targetGoal = saveEntity(targetGoal);
		
		return new GoalTargetCompetenceAnon(targetGoal, targetCompetence);
	}
	
	@Override
	@Transactional (readOnly = false)
	public boolean detachActivityFromTargetCompetence(User user, TargetActivity targetActivity, 
			String context) throws EventException {
		
		if (targetActivity != null) {
			TargetCompetence tComp = (TargetCompetence) targetActivity.getParentCompetence();
			
			tComp.removeActivity(targetActivity);
			tComp = saveEntity(tComp);
			
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("context", context);
			
			eventFactory.generateEvent(EventType.Detach, user, targetActivity, tComp, parameters);
			
			return true;
		}
		return false;
	}
	
	@Override
	@Transactional (readOnly = false)
	public TargetLearningGoal recalculateGoalProgress(TargetLearningGoal targetGoal) {
		
		if (targetGoal.isProgressActivityDependent()) {
			targetGoal = merge(targetGoal);
			double totalNo = 0;
			double completedNo = 0;
			
			Collection<TargetCompetence> competences = targetGoal.getTargetCompetences();
			
			for (TargetCompetence targetCompetence : competences) {
				Collection<TargetActivity> targetActivities = targetCompetence.getTargetActivities();
				
				for (TargetActivity targetActivity : targetActivities) {
					totalNo++;
					
					if (targetActivity.isCompleted())
						completedNo++;
				}
			}
			
			int newProgress = (int) (completedNo / (double) totalNo * 100);
			
			if (newProgress != targetGoal.getProgress()) {
				targetGoal.setProgress(newProgress);
				return saveEntity(targetGoal);
			}
		}
		return targetGoal;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<User> retrieveCollaborators(long goalId, User user){
		return retrieveCollaborators(goalId, user, persistence.currentManager());
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<User> retrieveCollaborators(long goalId, User user, Session session) {
		String query =
			"SELECT DISTINCT user "+
			"FROM User user "+
			"LEFT JOIN user.learningGoals tGoal "+
			"LEFT JOIN tGoal.learningGoal goal "+
			"WHERE goal.id = :goalId " + 
				"AND tGoal.deleted = false ";
		
		if (user != null) {
			query += "AND user != :user";
		}
		
		Query q = session.createQuery(query)
			.setLong("goalId", goalId);
		
		if (user != null) {
			q.setEntity("user", user);
		}
		
		@SuppressWarnings("unchecked")
		List<User> result = q.list();
		
		if (result != null && !result.isEmpty()) {
			return result;
		}		
		return new ArrayList<User>();
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<User> getCollaboratorsByTargetGoalId(long taretGoalId) {
		String query =
			"SELECT DISTINCT user "+
			"FROM User user "+
			"LEFT JOIN user.learningGoals tGoal "+
			"LEFT JOIN tGoal.learningGoal goal "+
			"WHERE goal IN (" +
				"SELECT DISTINCT goal1 "+
				"FROM TargetLearningGoal tGoal1 "+
				"LEFT JOIN tGoal1.learningGoal goal1 "+
				"WHERE tGoal1.id = :taretGoalId" +
			") AND tGoal.deleted = false";		
		@SuppressWarnings("unchecked")
		List<User> result = persistence.currentManager().createQuery(query)
			.setLong("taretGoalId", taretGoalId)
			.list();		
		if (result != null && !result.isEmpty()) {
			return result;
		}		
		return new ArrayList<User>();
	}
	@Override
	@Transactional (readOnly = true)
	public Set<Long> getCollaboratorsIdsByTargetGoalId(long taretGoalId) {
		Set<Long> myLearningGoalCollaborators = new TreeSet<Long>();
		String query =
			"SELECT DISTINCT user.id "+
			"FROM User user "+
			"LEFT JOIN user.learningGoals tGoal "+
			"LEFT JOIN tGoal.learningGoal goal "+
			"WHERE goal IN (" +
				"SELECT DISTINCT goal1 "+
				"FROM TargetLearningGoal tGoal1 "+
				"LEFT JOIN tGoal1.learningGoal goal1 "+
				"WHERE tGoal1.id = :taretGoalId" +
			") AND tGoal.deleted = false";		
		@SuppressWarnings("unchecked")
		List<Long> users = persistence.currentManager().createQuery(query)
				.setLong("taretGoalId", taretGoalId)
		.list();
	
	if (users != null && !users.isEmpty()) {
		myLearningGoalCollaborators.addAll(users);		
	}		
		return myLearningGoalCollaborators;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<User> retrieveLearningGoalMembers(long goalId, Session session){
		return retrieveCollaborators(goalId, null, session);
	}
	
	@Override
	@Transactional (readOnly = true)
	public Map<User, Map<LearningGoal, List<Tag>>> getMembersOfLearningGoalsHavingHashtags(
			Collection<String> hashtagsStr, Session session) {
		
		Map<User, Map<LearningGoal, List<Tag>>> userHashtags = new HashMap<User, Map<LearningGoal, List<Tag>>>();
		
		if (hashtagsStr != null && !hashtagsStr.isEmpty()) {
			String query = 
				"SELECT DISTINCT user, goal, hashtag " + 
				"FROM User user " + 
				"LEFT JOIN user.learningGoals tGoal " +
				"LEFT JOIN tGoal.learningGoal goal " +
				"LEFT JOIN goal.hashtags hashtag " +
				"WHERE hashtag.title IN (:hashtags)";

			@SuppressWarnings("unchecked")
			List<Object> result = session.createQuery(query)
					.setParameterList("hashtags", hashtagsStr)
					.list();
			
			if (result != null) {
				Iterator<?> iterator = result.iterator();
				
				while (iterator.hasNext()) {
					Object[] objects = (Object[]) iterator.next();
					User user = (User) objects[0];
					LearningGoal goal = (LearningGoal) objects[1];
					Tag hash = (Tag) objects[2];
					
					if (user != null && goal != null && hash != null) {
						Map<LearningGoal, List<Tag>> goalAnn = null;
						
						List<Tag> hashtags = null;
						
						if (userHashtags.containsKey(user)) {
							goalAnn = userHashtags.get(user);
	
							if (goalAnn.containsKey(goal)) {
								hashtags = goalAnn.get(goal);
							} else {
								hashtags = new ArrayList<Tag>();
							}
							hashtags.add(hash);
							
							goalAnn.put(goal, hashtags);
						} else {
							hashtags = new ArrayList<Tag>();
							hashtags.add(hash);
	
							goalAnn = new HashMap<LearningGoal, List<Tag>>();
							goalAnn.put(goal, hashtags);
						}
	
						userHashtags.put(user, goalAnn);
					}
				}
			}
		} 
		return userHashtags;
	}
	
	@Override
	@Transactional (readOnly = true)
	public boolean canUserJoinGoal(long goalId, User user) throws ResourceCouldNotBeLoadedException {
		LearningGoal goal = loadResource(LearningGoal.class, goalId);
		
		if (goal.getMaker().equals(user)) {
			return false;
		}
		if (isUserMemberOfLearningGoal(goal.getId(), user)) {
			return false;
		}
		if (portfolioManager.hasUserCompletedGoal(user, goal.getId())) {
			return false;
		}
		return true;
	}
	
	@Override
	@Transactional (readOnly = true)
	public boolean isUserMemberOfLearningGoal(long goalId, User user) {
		if (user == null) {
			logger.warn("User passed can not be null");
			return false;
		} else {
			String query = 
				"SELECT cast(COUNT(DISTINCT user) as int) " +
				"FROM User user " +
				"LEFT JOIN user.learningGoals tGoal " +
				"LEFT JOIN tGoal.learningGoal goal " +
				"WHERE goal.id = :goalId " +
					"AND user = :user " + 
					"AND tGoal.deleted = false";
			
			Integer result = (Integer) persistence.currentManager().createQuery(query)
					.setLong("goalId", goalId)
					.setEntity("user",user).uniqueResult();
			
			return result > 0;
		}
	}
	
	@Override
	@Transactional (readOnly = false)
	public NodeEvent createCompetenceAndAddToGoal(User user,
			String title, String description, 
			int validity, int duration, VisibilityType visibilityType, Collection<Tag> tags,
			long targetGoalId, boolean propagateManuallyToSocialStream,
			String context) throws EventException, ResourceCouldNotBeLoadedException {
		
		TargetCompetence newCompetence = compManager.createNewTargetCompetence(user, title, 
				description, validity, duration, tags, visibilityType);
		
		TargetLearningGoal goal = loadResource(TargetLearningGoal.class, targetGoalId);
		
		NodeEvent nodeEvent = addTargetCompetenceToGoal(
				merge(user), 
				merge(goal), 
				newCompetence, 
				propagateManuallyToSocialStream,
				context);
		
		return nodeEvent;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<TargetLearningGoal> getUserGoalsContainingCompetence(User user, Competence comp) {
		if (comp == null) 
			return new ArrayList<TargetLearningGoal>();
			
		String query = 
			"SELECT DISTINCT tGoal " +
			"FROM User user " +
			"LEFT JOIN user.learningGoals tGoal " +
			"LEFT JOIN FETCH tGoal.learningGoal goal " +
			"LEFT JOIN tGoal.targetCompetences tComp " +
			"LEFT JOIN tComp.competence comp " +
			"WHERE comp = :comp " +
				"AND user = :user";
		
		@SuppressWarnings("unchecked")
		List<TargetLearningGoal> result = persistence.currentManager().createQuery(query)
				.setEntity("user", user)
				.setEntity("comp",comp)
				.list();
		
		if (result != null) {
			return result;
		}
		return new ArrayList<TargetLearningGoal>();
	}

	@Override 
	@Transactional (readOnly = true)
	public int retrieveCollaboratorsNumber(long goalId, User user) {
		String query=
			"SELECT cast(COUNT(DISTINCT user) as int) " +
			"FROM User user "+
			"LEFT JOIN user.learningGoals tGoal " +
			"LEFT JOIN tGoal.learningGoal goal " +
			"WHERE goal.id = :goalId " +
				"AND user != :user";
		
		Integer collNumber = (Integer) persistence.currentManager().createQuery(query)
				.setLong("goalId", goalId)
				.setEntity("user", user)
				.uniqueResult();
		
		return collNumber;
	}
	
	@Override
	@Transactional (readOnly = true)
	public Map<Long, List<Long>> getCollaboratorsOfLearningGoals(List<LearningGoal> goals) {
		String query = 
			"SELECT DISTINCT goal.id, user.id " +
			"FROM User user "+
			"LEFT JOIN user.learningGoals tGoal "+
			"LEFT JOIN tGoal.learningGoal goal " +
			"WHERE goal IN (:goals)";

		@SuppressWarnings("unchecked")
		List<Object[]> result = persistence.currentManager().createQuery(query).
				setParameterList("goals", goals).
				list();
		
		Map<Long, List<Long>> counts = new HashMap<Long, List<Long>>();
		
		if (result != null && !result.isEmpty()) {
			
			for (Object[] res : result) {
				Long goalId = (Long) res[0];
				Long userId = (Long) res[1];
				
				List<Long> goalMemberIds = counts.get(goalId);
				
				if (goalMemberIds == null) {
					goalMemberIds = new ArrayList<Long>();
				}
				goalMemberIds.add(userId);
				
				counts.put(goalId, goalMemberIds);
			}
			
		}
		return counts;
	}

	@Override
	@Transactional (readOnly = false)
	public LearningGoal createNewLearningGoal(User user, String title,
			String description, Date deadline, Collection<Tag> keywords)
			throws EventException {
		
		LearningGoal newGoal = resourceFactory.createNewLearningGoal(
				user, 
				title,
				description,
				deadline,
				keywords,
				null);
		
		flush();
		return newGoal;
	}
	
	@Override
	@Transactional (readOnly = true)
	public String getTokensForLearningGoalsAndCompetenceForUser(User user) {
		
		StringBuffer userTokensBuffer = new StringBuffer();
		
		String goalQuery = 
				"SELECT DISTINCT goal.title, goal.description " +
				"FROM User user " +
				"LEFT JOIN user.learningGoals tGoal " +
				"LEFT JOIN tGoal.learningGoal goal " +
				"WHERE user = :user";	
	 
			@SuppressWarnings("unchecked")
		List<Object[]> goalsresult = persistence.currentManager().createQuery(goalQuery)
			.setEntity("user", user)
			.list();
		for (Object[] res : goalsresult) {
			userTokensBuffer.append(res[0]);
			userTokensBuffer.append(" ");
			userTokensBuffer.append(res[1]);
			userTokensBuffer.append(" ");
		}
		String tagsQuery = 
				"SELECT DISTINCT tag.title " +
				"FROM User user " +
				"LEFT JOIN user.learningGoals tGoal " +
				"LEFT JOIN tGoal.learningGoal goal " +
				 "LEFT JOIN goal.tags tag "+
				"WHERE user = :user";	
	 
			@SuppressWarnings("unchecked")
		List<String> tagsresult = persistence.currentManager().createQuery(tagsQuery)
			.setEntity("user", user)
			.list();
		for (String res : tagsresult) {
			userTokensBuffer.append(res);
			userTokensBuffer.append(" ");
		}
		String compQuery = 
				"SELECT DISTINCT comp.title, " +
						"comp.description " +
				"FROM User user " +
				"LEFT JOIN user.learningGoals tGoal " +
				"LEFT JOIN tGoal.learningGoal goal " +
				"LEFT JOIN tGoal.targetCompetences tComp " +
				"LEFT JOIN tComp.competence comp " +
				"WHERE user = :user";
		
		@SuppressWarnings("unchecked")
		List<Object[]> compsresult = persistence.currentManager().createQuery(compQuery)
				.setEntity("user", user)
				.list();
			for (Object[] res : compsresult) {
				userTokensBuffer.append(res[0]);
				userTokensBuffer.append(" ");
				userTokensBuffer.append(res[1]);
				userTokensBuffer.append(" ");	
			}
			
			String actquery = 
					"SELECT DISTINCT activity.title " +
					"FROM User user " +
					"LEFT JOIN user.learningGoals tGoal " +
					"LEFT JOIN tGoal.learningGoal goal " +
					"LEFT JOIN tGoal.targetCompetences tComp " +
					"LEFT JOIN tComp.competence comp " +
					"LEFT JOIN tComp.targetActivities tActivity "+
					"LEFT JOIN tActivity.activity activity "+
					"WHERE user = :user";
			try{
			@SuppressWarnings("unchecked")
			List<String> actsresult = persistence.currentManager().createQuery(actquery)
					.setEntity("user", user)
					.list();
				for (String res : actsresult) {
					userTokensBuffer.append(res);
					userTokensBuffer.append(" ");
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		return userTokensBuffer.toString();
	}
	
	@Override
	@Transactional (readOnly = false)
	public TargetLearningGoal updateGoalProgress(long targetGoalId, int newProgress) throws ResourceCouldNotBeLoadedException {
		TargetLearningGoal goal = loadResource(TargetLearningGoal.class, targetGoalId);
		
		goal.setProgress(newProgress);
		return saveEntity(goal);
	}
	
	@Override
	@Transactional (readOnly = true)
	public TargetLearningGoal getTargetGoal(long goalId, long userId) {
		String query=
			"SELECT tGoal " +
			"FROM User user "+
			"LEFT JOIN user.learningGoals tGoal " +
			"LEFT JOIN tGoal.learningGoal goal " +
			"WHERE goal.id = :goalId " +
				"AND user.id = :userId";
		
		@SuppressWarnings("unchecked")
		List<TargetLearningGoal> result = persistence.currentManager().createQuery(query)
				.setLong("goalId", goalId)
				.setLong("userId", userId)
				.setMaxResults(1)
				.list();
		
		if (result != null && !result.isEmpty()) {
			return result.iterator().next();
		}
		return null;
	}
	
	@Override
	@Transactional (readOnly = true)
	public Long getGoalIdForTargetGoal(long targetGoalId) {
		String query=
			"SELECT lGoal.id " +
			"FROM TargetLearningGoal tGoal "+
			"LEFT JOIN tGoal.learningGoal lGoal " +
			"WHERE tGoal.id = :targetGoalId ";
		
		Long goalId = (Long) persistence.currentManager().createQuery(query)
				.setLong("targetGoalId", targetGoalId).uniqueResult();
		
		return goalId;
	}
	
	@Override
	@Transactional (readOnly = false)
	public TargetLearningGoal cloneTargetCompetencesFromOneGoalToAnother(
			TargetLearningGoal sourceGoal,
			TargetLearningGoal targetGoal) throws ResourceCouldNotBeLoadedException {
		
//		targetGoal = merge(targetGoal);
//		
//		sourceGoal = loadResource(TargetLearningGoal.class, sourceGoal.getId(), true);
//		
//		Collection<TargetCompetence> targetCompetences = sourceGoal.getTargetCompetences();
//		
//		Iterator<TargetCompetence> iterator = targetCompetences.iterator();
//		
//		while (iterator.hasNext()) {
//			TargetCompetence targetComp = iterator.next();
//			
//			targetComp = merge(targetComp);
//			
//			targetComp.setParentGoal(targetGoal);
//			targetComp = saveEntity(targetComp);
//			
//			sourceGoal.removeCompetence(targetComp);
//			targetGoal.addTargetCompetence(targetComp);
//		}
//			
//		sourceGoal = saveEntity(sourceGoal);
//		targetGoal = saveEntity(targetGoal);
//		
//		return targetGoal;
		
		if (sourceGoal.getTargetCompetences() != null && !sourceGoal.getTargetCompetences().isEmpty()) {
			for (TargetCompetence targetComp : sourceGoal.getTargetCompetences()) {
				TargetCompetence cloneTargetCompetence = cloneTargetCompetence(targetComp);
				cloneTargetCompetence.setParentGoal(targetGoal);
				cloneTargetCompetence = saveEntity(cloneTargetCompetence);
				
				targetGoal.addTargetCompetence(cloneTargetCompetence);
			}
		}
		return saveEntity(targetGoal);
	}
	
	@Transactional (readOnly = false)
	private TargetCompetence cloneTargetCompetence(TargetCompetence targetComp) {
		TargetCompetence clone = new TargetCompetence();
		clone.setDateCreated(targetComp.getDateCreated());
		clone.setDateStarted(targetComp.getDateStarted());
		clone.setCompletedDay(targetComp.getCompletedDay());
		clone.setDateFinished(targetComp.getDateFinished());
		clone.setCompetence(targetComp.getCompetence());
		clone.setMaker(targetComp.getMaker());
		clone.setVisibility(targetComp.getVisibility());
		clone.setTitle(targetComp.getTitle());
		clone.setDescription(targetComp.getDescription());
		clone.setCompleted(targetComp.isCompleted());
		clone.setVisibility(targetComp.getVisibility());
		clone.setTags(new HashSet<Tag>(targetComp.getTags()));
		clone.setHashtags(new HashSet<Tag>(targetComp.getHashtags()));
		clone.setBasedOn(targetComp);
		
		for (TargetActivity targetAct : targetComp.getTargetActivities()) {
			clone.addTargetActivity(cloneTargetActivity(targetAct));
		}
		return saveEntity(clone);
	}

	private TargetActivity cloneTargetActivity(TargetActivity targetAct) {
		TargetActivity clone = new TargetActivity();
		clone.setMaker(targetAct.getMaker());
		clone.setActivity(targetAct.getActivity());
		clone.setDateCreated(targetAct.getDateCreated());
		clone.setDateCompleted(targetAct.getDateCompleted());
		clone.setTitle(targetAct.getTitle());
		clone.setVisibility(targetAct.getVisibility());
		clone.setTags(new HashSet<Tag>(targetAct.getTags()));
		clone.setHashtags(new HashSet<Tag>(targetAct.getHashtags()));
		clone.setCompleted(targetAct.isCompleted());
		clone.setTaPosition(targetAct.getTaPosition());
		clone.setParentCompetence(targetAct.getParentCompetence());
		clone.setAssignmentLink(targetAct.getAssignmentLink());
		clone.setAssignmentTitle(targetAct.getAssignmentTitle());
		clone.setBasedOn(targetAct);
		return saveEntity(clone);
	}
	
	@Override
	@Transactional (readOnly = true)
	public long getNumberOfUsersLearningGoal(LearningGoal goal) {
		String query1 =
			"SELECT tGoal " +
			"FROM TargetLearningGoal tGoal " +
			"LEFT JOIN tGoal.learningGoal goal " +
			"WHERE goal = :goal ";
		
		@SuppressWarnings("unchecked")
		List<TargetLearningGoal> tGoals = persistence.currentManager().createQuery(query1)
			.setEntity("goal", goal)
			.setMaxResults(1)
			.list();

		TargetLearningGoal tGoal = null;
		
		if (tGoals != null && !tGoals.isEmpty()) {
			tGoal = tGoals.iterator().next();
		} else {
			return 0;
		}
		
		// if this is regular, user created goal
		
		CourseEnrollment courseEnrollment = tGoal.getCourseEnrollment();
		
		if (courseEnrollment == null) {
			String query=
				"SELECT COUNT(tGoal) " +
				"FROM TargetLearningGoal tGoal " +
				"LEFT JOIN tGoal.learningGoal goal " +
				"WHERE goal = :goal " +
					"AND goal.deleted = false " +
					"AND tGoal.deleted = false ";
			
			Long result = (Long) persistence.currentManager().createQuery(query)
					.setEntity("goal", goal)
					.uniqueResult();
			
			return result;
		} else {
			String query=
				"SELECT COUNT(tGoal1) " +
				"FROM TargetLearningGoal tGoal1 " +
				"LEFT JOIN tGoal1.courseEnrollment enrollment1 " +
				"LEFT JOIN enrollment1.course course1 " +
				"WHERE tGoal1.deleted = false " +
					"AND course1 IN (" +
							"SELECT course " +
							"FROM TargetLearningGoal tGoal " +
							"LEFT JOIN tGoal.courseEnrollment enrollment " +
							"LEFT JOIN enrollment.course course " +
							"WHERE tGoal = :tGoal " +
						")";
			
			Long result = (Long) persistence.currentManager().createQuery(query)
				.setEntity("tGoal", tGoal)
				.uniqueResult();
			
			return result;
		}
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<User> getUsersLearningGoal(long goalId) {
		String query1 =
			"SELECT tGoal " +
			"FROM TargetLearningGoal tGoal " +
			"LEFT JOIN tGoal.learningGoal goal " +
			"WHERE goal.id = :goalId ";
		
		@SuppressWarnings("unchecked")
		List<TargetLearningGoal> tGoals = persistence.currentManager().createQuery(query1)
			.setLong("goalId", goalId)
			.setMaxResults(1)
			.list();

		TargetLearningGoal tGoal = null;
		
		if (tGoals != null && !tGoals.isEmpty()) {
			tGoal = tGoals.iterator().next();
		} else {
			return new ArrayList<User>();
		}
		
		// if this is regular, user created goal
		
		CourseEnrollment courseEnrollment = tGoal.getCourseEnrollment();
		
		if (courseEnrollment == null) {
			String query=
				"SELECT user " +
				"FROM User user "+
				"LEFT JOIN user.learningGoals tGoal " +
				"LEFT JOIN tGoal.learningGoal goal " +
				"WHERE goal.id = :goalId " +
					"AND goal.deleted = false " +
					"AND tGoal.deleted = false ";
			
			@SuppressWarnings("unchecked")
			List<User> result =  persistence.currentManager().createQuery(query)
					.setLong("goalId", goalId)
					.list();
			
			if (result != null)
				return result;
			
			return new ArrayList<User>();
		} else {
			String query=
				"SELECT user " +
				"FROM User user "+
				"LEFT JOIN user.learningGoals tGoal1 " +
				"LEFT JOIN tGoal1.courseEnrollment enrollment1 " +
				"LEFT JOIN enrollment1.course course1 " +
				"WHERE tGoal1.deleted = false " +
					"AND course1 IN (" +
							"SELECT course " +
							"FROM TargetLearningGoal tGoal " +
							"LEFT JOIN tGoal.courseEnrollment enrollment " +
							"LEFT JOIN enrollment.course course " +
							"WHERE tGoal = :tGoal " +
					")";
			
			@SuppressWarnings("unchecked")
			List<User> result =  persistence.currentManager().createQuery(query)
				.setEntity("tGoal", tGoal)
				.list();
			
			if (result != null)
				return result;
			
			return new ArrayList<User>();
		}
	}
	
	public class NodeEvent {
		public Node node;
		public Event event;
		
		public NodeEvent(Node node, Event event){
			this.node = node;
			this.event = event;
		}

		public Node getNode() {
			return node;
		}

		public void setNode(Node node) {
			this.node = node;
		}

		public Event getEvent() {
			return event;
		}

		public void setEvent(Event event) {
			this.event = event;
		}
	}
	
	public class GoalTargetCompetenceAnon {
		public TargetLearningGoal targetGoal;
		public TargetCompetence targetCompetence;
		
		public GoalTargetCompetenceAnon(TargetLearningGoal targetGoal, TargetCompetence targetCompetence) {
			this.targetGoal = targetGoal;
			this.targetCompetence = targetCompetence;
		}

		public TargetLearningGoal getTargetGoal() {
			return targetGoal;
		}

		public void setTargetGoal(TargetLearningGoal targetGoal) {
			this.targetGoal = targetGoal;
		}

		public TargetCompetence getTargetCompetence() {
			return targetCompetence;
		}

		public void setTargetCompetence(TargetCompetence targetCompetence) {
			this.targetCompetence = targetCompetence;
		}
		
	}
	@Override
	@Transactional (readOnly = true)
	public Set<Long> getTargetCompetencesForTargetLearningGoal(Long targetLearningGoalId) {
		Set<Long> targetCompetences=new TreeSet<Long>();
		String query = 
				"SELECT tComp.id " +
						"FROM TargetLearningGoal tGoal " +
						"LEFT JOIN tGoal.targetCompetences tComp "+
						"WHERE tGoal.id = :targetLearningGoalId and tComp.id>0 ";

		
		@SuppressWarnings("unchecked")
		List<Long> tComps =  persistence.currentManager().createQuery(query).
				setLong("targetLearningGoalId", targetLearningGoalId).
				list();
		System.out.println("FOUND TG COMPETENCES SIZE:"+tComps.size());
		if (tComps != null) {
			 targetCompetences.addAll(tComps);
		}

		return targetCompetences;
	}
	@Override
	@Transactional (readOnly = true)
	public Set<Long> getTargetActivitiesForTargetLearningGoal(Long targetLearningGoalId) {
		Set<Long> targetActivities=new TreeSet<Long>();
		String query = 
				"SELECT tActivity.id " +
						"FROM TargetLearningGoal tGoal " +
						"LEFT JOIN tGoal.targetCompetences tComp "+
						"LEFT JOIN tComp.targetActivities tActivity "+
						"WHERE tGoal.id = :targetLearningGoalId and tActivity.id>0 ";

		
		@SuppressWarnings("unchecked")
		List<Long> tActivities =  persistence.currentManager().createQuery(query).
				setLong("targetLearningGoalId", targetLearningGoalId).
				list();
		System.out.println("FOUND TG Activities SIZE:"+tActivities.size());
		if (tActivities != null) {
			 targetActivities.addAll(tActivities);
		}

		return targetActivities;
	}
}
