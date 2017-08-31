package org.prosolo.services.activityWall;

import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.activitywall.SocialActivityConfig;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.activityWall.impl.data.SocialActivityData1;
import org.prosolo.services.event.EventException;

public interface ActivityWallActionsManager {

	SocialActivityConfig hideNotification(long socialActivityId, UserContextData context,
										  Session session) throws ResourceCouldNotBeLoadedException, EventException;
	
	boolean deleteSocialActivity(User user, long socialActivityId, UserContextData context, Session session) throws EventException, ResourceCouldNotBeLoadedException;

	void deleteSocialActivity(SocialActivityData1 socialActivity,
			UserContextData context, Session session) throws DbConnectionException;
	
	void enableComments(long socialActivityId, UserContextData context,
			Session session) throws DbConnectionException;
	
	void disableComments(long socialActivityId, UserContextData context,
			Session session) throws DbConnectionException;
	
//	SocialActivity1 decrementNumberOfReshares(long socialActivityId, Session session) 
//			throws ResourceCouldNotBeLoadedException;
	
}
