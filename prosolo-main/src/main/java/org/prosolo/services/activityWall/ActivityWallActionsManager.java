package org.prosolo.services.activityWall;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.SocialActivityConfig;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.EventException;

public interface ActivityWallActionsManager {

	SocialActivityConfig hideNotification(long socialActivityId, User user, long configId, String contex, Session sessiont) throws ResourceCouldNotBeLoadedException, EventException;

	boolean deleteSocialActivity(User user, long socialActivityId, String context, Session session) throws EventException, ResourceCouldNotBeLoadedException;

	boolean enableComments(User user, long socialActivityId, String context, Session session) throws EventException, ResourceCouldNotBeLoadedException;
	
	boolean disableComments(User user, long socialActivityId, String context, Session session) throws EventException, ResourceCouldNotBeLoadedException;
	
	SocialActivity decrementNumberOfReshares(long resharedSocialActivityId, Session session) throws ResourceCouldNotBeLoadedException;
	
}
