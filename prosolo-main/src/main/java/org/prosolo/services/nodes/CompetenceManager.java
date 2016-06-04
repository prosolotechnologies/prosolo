package org.prosolo.services.nodes;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.CompetenceActivity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.course.CreatorType;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.context.data.LearningContextData;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.activity.ActivityData;
import org.prosolo.services.nodes.data.activity.attachmentPreview.AttachmentPreview;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.competences.data.ActivityType;

public interface CompetenceManager extends AbstractManager {
	
	TargetCompetence createNewTargetCompetence(User user, String title, String description, int validity, int duration, Collection<Tag> tags, VisibilityType visibilityType) throws EventException;

	Competence updateCompetence(Competence competence, String title, String description,
			int duration, int validity, Collection<Tag> tags,
			List<Competence> corequisites, List<Competence> prerequisites,
			List<ActivityWallData> activities, boolean updateActivities);
	
	boolean hasUserCompletedCompetence(long competenceId, User user);

	boolean hasUserCompletedCompetence(Competence comp, User user);
	
	List<TargetCompetence> getTargetCompetences(long userId, long goalId);
	
	List<User> getMembersOfTargetCompetenceGoal(TargetCompetence tComp, Session session);

	Competence createCompetence(User user, String title,
			String description, int validity, int duration, Collection<Tag> tags, 
			List<Competence> prerequisites, List<Competence> corequisites) throws EventException;

	boolean isUserAcquiringCompetence(long competenceId, User user);

	boolean isUserAcquiringCompetence(Competence comp, User user);
	
	List<TargetActivity> getTargetActivities(long targetCompId);

	List<TargetActivity> getTargetActivities(long userId, long compId);

	Activity createNewActivityAndAddToCompetence(User user, String title,
			String description, ActivityType activityType, boolean mandatory,
			AttachmentPreview attachmentPreview, int maxNumberOfFiles,
			boolean uploadsVisibility, int duration, Competence competence) throws EventException;

	void updateTargetCompetenceProgress(long targetCompId, boolean completed, int progress) throws ResourceCouldNotBeLoadedException;

	TargetCompetence getTargetCompetence(long userId, long compId, long goalId);

	Competence createCompetence(User user, String title, String description,
			int validity, int duration, Collection<Tag> tags,
			List<Competence> prerequisites, List<Competence> corequisites,
			Date dateCreated)throws EventException ;

	Set<Long> getTargetCompetencesIds(long userId, long goalId);

	Set<Long> getTargetActivitiesIds(long targetCompId);

	Set<Long> getCompetencesHavingAttachedActivity(long activityId);

	List<Long> getActivitiesIds(long targetCompId);

	boolean disableActivityRecommendations(long targetCompId);
	
	public List<TargetCompetence> getTargetCompetencesForTargetLearningGoal(long goalId) throws DbConnectionException;
	
	public void updateCompetenceProgress(long compId, int progress) throws DbConnectionException;

	Competence updateCompetence(long id, String title, String description, int duration, int validity, boolean published,
			HashSet<Tag> tags) throws DbConnectionException;

	List<ActivityData> getCompetenceActivities(long compId) throws DbConnectionException;
	
	String getCompetenceTitle(long compId) throws DbConnectionException;
	
	CompetenceActivity saveCompetenceActivity(long compId, ActivityData activityData,
			LearningContextData context) throws DbConnectionException;

	Competence createNewUntitledCompetence(User user, CreatorType manager);
	
	void deleteCompetenceActivity(ActivityData activityData,
			List<ActivityData> changedActivities, User user, 
			LearningContextData context) throws DbConnectionException;
	
	void updateOrderOfCompetenceActivities(List<ActivityData> activities) throws DbConnectionException;
	
	/**
	 * Method for getting all completed competences (competences that has progress == 100)
	 * @return 
	 * @throws DbConnectionException
	 */
	List<TargetCompetence1> getAllCompletedCompetences(Long userId) throws DbConnectionException;
	
	/**
	 * Updated hidden_from_profile_field
	 * @param id
	 * @param duration
	 * @throws DbConnectionException
	 */
	void updateHiddenTargetCompetenceFromProfile(long id, boolean hiddenFromProfile) throws DbConnectionException;
	
}
