package org.prosolo.services.activityWall;

import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.activitywall.SocialActivityConfig;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.activityWall.impl.data.SocialActivityData1;
import org.prosolo.services.event.EventException;

public interface ActivityWallActionsManager {

	SocialActivityConfig hideNotification(long socialActivityId, long userId, 
			LearningContextData context, Session session) throws ResourceCouldNotBeLoadedException, EventException;
	
	boolean deleteSocialActivity(User user, long socialActivityId, String context, Session session) throws EventException, ResourceCouldNotBeLoadedException;

	void deleteSocialActivity(long userId, SocialActivityData1 socialActivity, 
			LearningContextData context, Session session) throws DbConnectionException;	
	
	void enableComments(long userId, long socialActivityId, LearningContextData context, 
			Session session) throws DbConnectionException;
	
	void disableComments(long userId, long socialActivityId, LearningContextData context, 
			Session session) throws DbConnectionException;
	
//	SocialActivity1 decrementNumberOfReshares(long socialActivityId, Session session) 
//			throws ResourceCouldNotBeLoadedException;
	
}
