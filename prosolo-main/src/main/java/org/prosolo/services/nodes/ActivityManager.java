package org.prosolo.services.nodes;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.outcomes.Outcome;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.context.data.LearningContextData;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.web.activitywall.data.AttachmentPreview;
import org.prosolo.web.competences.data.ActivityType;

public interface ActivityManager extends AbstractManager {

	// TargetActivity toggleCompleted(TargetActivity activity) throws
	// EventException;
	Activity createNewActivity(User user, String title,
			String description, AttachmentPreview attachmentPreview, VisibilityType visType, boolean sync,
			String context, String page, String learningContext, String service) throws EventException;

	Activity createNewActivity(User user, String title, String description,
			AttachmentPreview attachmentPreview, VisibilityType visType,
			Collection<Tag> tags) throws EventException;

	Activity createNewResourceActivity(User user, String title,
			String description, AttachmentPreview attachmentPreview, VisibilityType visType, 
			Collection<Tag> tags, boolean propagateToSocialStreamManualy,
			String context, String page, String learningContext, String service) throws EventException;

	boolean checkIfCompletedByUser(User user, Activity activity);

	boolean checkIfUserCompletedActivity(User user, Activity activity);

	boolean checkIfActivityInPortfolio(User user, Activity activity);

	List<User> getUsersHavingTargetActivityInLearningGoal(TargetActivity activity);

	List<User> getUsersHavingTargetActivityInLearningGoal(TargetActivity activity, Session session);

	List<TargetActivity> getTargetActivities(Activity activity);

	Activity getActivity(long targetActivityId);

	TargetActivity updateTargetActivityWithAssignement(long targetActivityId,
			String assignmentLink, String assignmentTitle, Session session)
			throws ResourceCouldNotBeLoadedException;

	/**
	 * @param id
	 * @param title
	 * @param description
	 * @param activityType
	 * @param mandatory
	 * @param uploadPreview 
	 * @param maxNumberOfFiles
	 * @param visibleToEveryone
	 * @param duration
	 * @param user
	 * @return
	 * @throws ResourceCouldNotBeLoadedException 
	 */
	Activity updateActivity(long id, String title, String description,
			ActivityType type, boolean mandatory,
			AttachmentPreview attachmentPreview, int maxNumberOfFiles,
			boolean visibleToEveryone, int duration, User user) throws ResourceCouldNotBeLoadedException;

	TargetActivity replaceTargetActivityOutcome(long targetActivityId, Outcome outcome,
			Session session);

	List<TargetActivity> getAllTargetActivities();

	List<Activity> getMockActivities(int limit);
	
	//List<TargetActivity> getCompetenceCompletedTargetActivities(long userId, long compId) throws DbConnectionException;

	boolean updateActivityStartDate(long id, Date date, Session session);
	
	List<Long> getTimeSpentOnActivityForAllUsersSorted(long activityId) throws DbConnectionException;
	
	boolean updateTimeSpentOnActivity(long activityId, long timeSpent, Session session);
	
	boolean checkIfActivityIsReferenced(long activityId) throws DbConnectionException;
	
	void deleteActivity(long activityId, Class<? extends Activity> activityClass, User user, 
			LearningContextData data);
}
