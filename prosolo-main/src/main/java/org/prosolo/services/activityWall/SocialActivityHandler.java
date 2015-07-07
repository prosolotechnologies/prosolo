package org.prosolo.services.activityWall;

import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventException;
import org.prosolo.web.goals.GoalWallBean;

/**
@author Zoran Jeremic Jan 16, 2015
 *
 */

public interface SocialActivityHandler {

	SocialActivity addSociaActivitySyncAndPropagateToStatusAndGoalWall(Event event) throws EventException;

	void propagateSocialActivity(Event event) throws EventException;

	void updateSocialActivity(SocialActivity socialActivity,
			HttpSession userSession, Session session);

	void disableSharing(SocialActivity socialActivity, HttpSession userSession,
			Session currentManager);

	void removeSocialActivity(SocialActivity socialActivity,
			HttpSession userSession, Session session);

	void addSociaActivitySyncAndPropagateToGoalWall(Event event,
			GoalWallBean goalWallBean, User user, Locale locale)
			throws EventException;

}

