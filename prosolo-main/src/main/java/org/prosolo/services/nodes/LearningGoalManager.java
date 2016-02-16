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
import org.prosolo.services.nodes.impl.LearningGoalManagerImpl.GoalTargetCompetenceAnon;
import org.prosolo.services.nodes.impl.LearningGoalManagerImpl.NodeEvent;
import org.prosolo.web.activitywall.data.AttachmentPreview;

public interface LearningGoalManager extends AbstractManager {
	
	List<TargetLearningGoal> getUserTargetGoals(User user);

	List<TargetLearningGoal> getUserTargetGoals(User user, Session session);
	
	LearningGoal createNewLearningGoal(User user, String title, String description, Date deadline, 
			Collection<Tag> keywords) throws EventException;
	
	TargetLearningGoal createNewTargetLearningGoal(User user, long goalId) throws ResourceCouldNotBeLoadedException, EventException;
	
	TargetLearningGoal createNewTargetLearningGoal(User user, LearningGoal goal) throws EventException, ResourceCouldNotBeLoadedException;

	TargetLearningGoal createNewLearningGoal(User user, String title, String description, Date deadline, 
			Collection<Tag> keywords, Collection<Tag> hashtags, boolean progressActivityDependent) 
					throws EventException, ResourceCouldNotBeLoadedException;
	
	TargetLearningGoal createNewCourseBasedLearningGoal(User user,
			long courseId, LearningGoal courseGoal,
			String context) throws EventException, ResourceCouldNotBeLoadedException;
	
//	TargetLearningGoal updateLearningGoal(User user, TargetLearningGoal targetGoal) throws EventException;
	
	boolean removeLearningGoal(User user, LearningGoal goal) throws EventException;
	
	boolean deleteGoal(long targetGoalId) throws ResourceCouldNotBeLoadedException;

	boolean deleteGoal(TargetLearningGoal targetGoal);
	
	TargetLearningGoal markAsCompleted(User user, TargetLearningGoal goal, String context) throws EventException;
	

	
	NodeEvent addCompetenceToGoal(User user, long targetGoalId,	Competence competence, 
			boolean propagateEventAutomatically, String context) throws EventException, 
			ResourceCouldNotBeLoadedException;
	
	NodeEvent addCompetenceToGoal(User user, TargetLearningGoal targetGoal,
			Competence competence, boolean propagateEventAutomatically,
			String context) throws EventException;
	
	NodeEvent addTargetCompetenceToGoal(User user, TargetLearningGoal targetGoal, 
			TargetCompetence tComp, boolean propagateManuallyToSocialStream,
			String context) throws EventException;

	TargetActivity cloneAndAttachActivityToTargetCompetence(User user, long targetCompetenceId, 
			Activity activity, boolean sync) throws EventException, ResourceCouldNotBeLoadedException;
	
	List<TargetActivity> cloneActivitiesAndAddToTargetCompetence(User user, TargetCompetence tComp, 
			List<Activity> targetActivities, boolean sync, String context) throws EventException;

	List<TargetActivity> cloneActivitiesAndAddToTargetCompetence(User user, long targetCompId, 
			List<Activity> targetActivities, boolean sync, String context) throws EventException, 
			ResourceCouldNotBeLoadedException;

	TargetActivity addActivityToTargetCompetence(User user,
			long targetCompetenceId, Activity activity, String context, String page,
			String learningContext, String service)
			throws EventException, ResourceCouldNotBeLoadedException;
	
	TargetActivity addActivityToTargetCompetence(User user, long targetCompetenceId, long activityId, String context)
			throws EventException, ResourceCouldNotBeLoadedException;

	TargetCompetence addActivityToTargetCompetence(User user, TargetCompetence targetCompetence, 
			Activity activity, boolean sync) throws EventException;
	
	GoalTargetCompetenceAnon deleteTargetCompetenceFromGoal(User user, long targetGoalId, 
			long targetCompId) throws EventException, ResourceCouldNotBeLoadedException;

	boolean detachActivityFromTargetCompetence(User user, TargetActivity targetActivity, String context) 
			throws EventException;

	List<User> getCollaboratorsByTargetGoalId(long taretGoalId);
	
	List<User> retrieveCollaborators(long goalId, User user);
	
	List<User> retrieveCollaborators(long goalId, User user, Session session);

	List<User> retrieveLearningGoalMembers(long goalId, Session session);
	
	Map<User, Map<LearningGoal, List<Tag>>> getMembersOfLearningGoalsHavingHashtags(
			Collection<String> hashtags, Session session);

	boolean isUserMemberOfLearningGoal(long goalId, User user);

	TargetLearningGoal recalculateGoalProgress(TargetLearningGoal targetGoal);

	TargetActivity createActivityAndAddToTargetCompetence(User user,
			String title, String description, AttachmentPreview attachmentPreview,
			VisibilityType visType, long targetCompetenceId, boolean connectWithStatus,
			String context, String page, String learningContext, String service) 
					throws EventException, ResourceCouldNotBeLoadedException;
	
	NodeEvent createCompetenceAndAddToGoal(User user, String title, String description, 
			int validity, int duration, VisibilityType visibilityType, Collection<Tag> tags, 
			long goalId, boolean propagateManuallyToSocialStream, String context) 
				throws EventException, ResourceCouldNotBeLoadedException;

	boolean canUserJoinGoal(long goalId, User user) throws ResourceCouldNotBeLoadedException;

	List<TargetLearningGoal> getUserGoalsContainingCompetence(User user, Competence comp);

	int retrieveCollaboratorsNumber(long goalId, User user);
	
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

	List<Long> getUserGoalsIds(User user);
	
	TargetLearningGoal createNewCourseBasedLearningGoal(User user, Course course, LearningGoal courseGoal,
			String context) throws EventException, ResourceCouldNotBeLoadedException;

}
