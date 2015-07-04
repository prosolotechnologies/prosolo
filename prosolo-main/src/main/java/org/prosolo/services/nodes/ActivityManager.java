package org.prosolo.services.nodes;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.prosolo.domainmodel.activities.Activity;
import org.prosolo.domainmodel.activities.TargetActivity;
import org.prosolo.domainmodel.annotation.Tag;
import org.prosolo.domainmodel.organization.VisibilityType;
import org.prosolo.domainmodel.outcomes.Outcome;
import org.prosolo.domainmodel.user.User;
import org.prosolo.services.event.EventException;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.web.activitywall.data.AttachmentPreview;
import org.prosolo.web.competences.data.ActivityType;

public interface ActivityManager extends AbstractManager {

	// TargetActivity toggleCompleted(TargetActivity activity) throws
	// EventException;
	Activity createNewActivity(User user, String title,
			String description, AttachmentPreview attachmentPreview, VisibilityType visType, boolean sync, String context)
			throws EventException;

	Activity createNewActivity(User user, String title, String description,
			AttachmentPreview attachmentPreview, VisibilityType visType,
			Collection<Tag> tags) throws EventException;

	Activity createNewResourceActivity(User user, String title, String description,
			AttachmentPreview attachmentPreview, VisibilityType visType,
			Collection<Tag> tags, boolean sync, String context) throws EventException;

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

 

}
