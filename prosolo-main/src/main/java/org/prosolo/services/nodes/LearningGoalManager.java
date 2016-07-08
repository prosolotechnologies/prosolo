package org.prosolo.services.nodes;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.activity.attachmentPreview.AttachmentPreview;
import org.prosolo.services.nodes.impl.LearningGoalManagerImpl.GoalTargetCompetenceAnon;
import org.prosolo.services.nodes.impl.LearningGoalManagerImpl.NodeEvent;

public interface LearningGoalManager extends AbstractManager {
	
	List<TargetLearningGoal> getUserTargetGoals(User user);

	List<TargetLearningGoal> getUserTargetGoals(User user, Session session);
	
	LearningGoal createNewLearningGoal(User user, String title, String description, Date deadline, 
			Collection<Tag> keywords) throws EventException;
	
	TargetLearningGoal createNewTargetLearningGoal(long userId, long goalId) throws ResourceCouldNotBeLoadedException, EventException;
	
	TargetLearningGoal createNewTargetLearningGoal(long userId, LearningGoal goal) throws EventException, ResourceCouldNotBeLoadedException;

	TargetLearningGoal createNewLearningGoal(long userId, String title, String description, Date deadline, 
			Collection<Tag> keywords, Collection<Tag> hashtags, boolean progressActivityDependent) 
					throws EventException, ResourceCouldNotBeLoadedException;
	
	TargetLearningGoal createNewCourseBasedLearningGoal(long userId,
			long courseId, LearningGoal courseGoal,
			String context) throws EventException, ResourceCouldNotBeLoadedException;
	
//	TargetLearningGoal updateLearningGoal(User user, TargetLearningGoal targetGoal) throws EventException;
	
	boolean removeLearningGoal(User user, LearningGoal goal) throws EventException;
	
	boolean deleteGoal(long targetGoalId) throws ResourceCouldNotBeLoadedException;

	boolean deleteGoal(TargetLearningGoal targetGoal);
	
	TargetLearningGoal markAsCompleted(long userId, TargetLearningGoal goal, String context) throws EventException;
	

	
	NodeEvent addCompetenceToGoal(long userId, long targetGoalId,	Competence competence, 
			boolean propagateEventAutomatically, String context) throws EventException, 
			ResourceCouldNotBeLoadedException;
	
	NodeEvent addCompetenceToGoal(long userId, TargetLearningGoal targetGoal,
			Competence competence, boolean propagateEventAutomatically,
			String context) throws EventException;
	
	NodeEvent addTargetCompetenceToGoal(User user, TargetLearningGoal targetGoal, 
			TargetCompetence tComp, boolean propagateManuallyToSocialStream,
			String context) throws EventException;

	TargetActivity cloneAndAttachActivityToTargetCompetence(User user, long targetCompetenceId, 
			Activity activity, boolean sync) throws EventException, ResourceCouldNotBeLoadedException;
	
	List<TargetActivity> cloneActivitiesAndAddToTargetCompetence(long userId, TargetCompetence tComp, 
			List<Activity> targetActivities, boolean sync, String context) throws EventException;

	List<TargetActivity> cloneActivitiesAndAddToTargetCompetence(long userId, long targetCompId, 
			List<Activity> targetActivities, boolean sync, String context) throws EventException, 
			ResourceCouldNotBeLoadedException;

	TargetActivity addActivityToTargetCompetence(long userId,
			long targetCompetenceId, Activity activity, String context, String page,
			String learningContext, String service)
			throws EventException, ResourceCouldNotBeLoadedException;
	
	TargetActivity addActivityToTargetCompetence(long userId, long targetCompetenceId, long activityId, String context)
			throws EventException, ResourceCouldNotBeLoadedException;

	TargetCompetence addActivityToTargetCompetence(User user, TargetCompetence targetCompetence, 
			Activity activity, boolean sync) throws EventException;
	
	GoalTargetCompetenceAnon deleteTargetCompetenceFromGoal(long userId, long targetGoalId, 
			long targetCompId) throws EventException, ResourceCouldNotBeLoadedException;

	boolean detachActivityFromTargetCompetence(long userId, TargetActivity targetActivity, String context) 
			throws EventException;

	List<User> getCollaboratorsByTargetGoalId(long taretGoalId);
	
	List<User> retrieveCollaborators(long goalId, long userId);
	
	List<User> retrieveCollaborators(long goalId, long userId, Session session);

	List<User> retrieveLearningGoalMembers(long goalId, Session session);
	
	Map<User, Map<LearningGoal, List<Tag>>> getMembersOfLearningGoalsHavingHashtags(
			Collection<String> hashtags, Session session);

	boolean isUserMemberOfLearningGoal(long goalId, long userId);

	TargetLearningGoal recalculateGoalProgress(TargetLearningGoal targetGoal);

	TargetActivity createActivityAndAddToTargetCompetence(long userId,
			String title, String description, AttachmentPreview attachmentPreview,
			VisibilityType visType, long targetCompetenceId, boolean connectWithStatus,
			String context, String page, String learningContext, String service) 
					throws EventException, ResourceCouldNotBeLoadedException;
	
	NodeEvent createCompetenceAndAddToGoal(long userId, String title, String description, 
			int validity, int duration, VisibilityType visibilityType, Collection<Tag> tags, 
			long goalId, boolean propagateManuallyToSocialStream, String context) 
				throws EventException, ResourceCouldNotBeLoadedException;

	boolean canUserJoinGoal(long goalId, long userId) throws ResourceCouldNotBeLoadedException;

	List<TargetLearningGoal> getUserGoalsContainingCompetence(long userId, Competence comp);

	int retrieveCollaboratorsNumber(long goalId, long userId);
	
	Map<Long, List<Long>> getCollaboratorsOfLearningGoals(List<LearningGoal> foundGoals);

	String getTokensForLearningGoalsAndCompetenceForUser(User user);

	TargetLearningGoal updateGoalProgress(long targetGoalId, int newProgress) 
			throws ResourceCouldNotBeLoadedException;

	/**
	 * @param learningGoal
	 * @param title
	 * @param description
	 * @param tagsString
	 * @param hashtagsString
	 * @param deadline 
	 * @param freeToJoin
	 * @return
	 */
	LearningGoal updateLearningGoal(LearningGoal learningGoal, String title,
			String description, String tagsString, String hashtagsString,
			Date deadline, boolean freeToJoin);

	/**
	 * @param targetGoal
	 * @param progressActivityDependent
	 * @param progress
	 * @return
	 */
	TargetLearningGoal updateTargetLearningGoal(TargetLearningGoal targetGoal,
			boolean progressActivityDependent, int progress);

	TargetLearningGoal getTargetGoal(long goalId, long userId);
	
	TargetLearningGoal cloneTargetCompetencesFromOneGoalToAnother(
			TargetLearningGoal existingTargetGoal,
			TargetLearningGoal newCourseTargetGoal) throws ResourceCouldNotBeLoadedException;

	long getNumberOfUsersLearningGoal(LearningGoal goal);

	List<User> getUsersLearningGoal(long goalId);

	Set<Long> getCollaboratorsIdsByTargetGoalId(long taretGoalId);

	Long getGoalIdForTargetGoal(long targetGoalId);

	Set<Long> getTargetCompetencesForTargetLearningGoal(
			Long targetLearningGoalId);

	Set<Long> getTargetActivitiesForTargetLearningGoal(Long targetLearningGoalId);

	List<Long> getUserGoalsIds(long userId);
	
	TargetLearningGoal createNewCourseBasedLearningGoal(long userId, Course course, LearningGoal courseGoal,
			String context) throws EventException, ResourceCouldNotBeLoadedException;

}
