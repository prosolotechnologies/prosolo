package org.prosolo.services.activityWall;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.activitywall.old.SocialActivityConfig;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.context.data.LearningContextData;

public interface ActivityWallActionsManager {

	SocialActivityConfig hideNotification(long socialActivityId, long userId, 
			LearningContextData context, Session session) throws ResourceCouldNotBeLoadedException, EventException;

	boolean deleteSocialActivity(User user, long socialActivityId, String context, Session session) throws EventException, ResourceCouldNotBeLoadedException;

	boolean enableComments(User user, long socialActivityId, String context, Session session) throws EventException, ResourceCouldNotBeLoadedException;
	
	boolean disableComments(User user, long socialActivityId, String context, Session session) throws EventException, ResourceCouldNotBeLoadedException;
	
	SocialActivity1 decrementNumberOfReshares(long socialActivityId, Session session) 
			throws ResourceCouldNotBeLoadedException;
	
}
