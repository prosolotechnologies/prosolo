package org.prosolo.services.annotation;

import java.util.List;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.annotation.Annotation;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.EventException;

public interface DislikeManager {
	
	void dislikeSocialActivity(long userId, long notificationId, long socialActivityId,
			int newDislikeCount, Session session, String context, String page, 
			String lContext, String service) 
			throws EventException, ResourceCouldNotBeLoadedException;

	Annotation dislikeComment(long userId, long commentId, Session session, String context) throws EventException,
	ResourceCouldNotBeLoadedException;

	Annotation dislikeNode(long userId, long resourceId, Session session,
			String context, String page, String lContext, String service) throws EventException,
			ResourceCouldNotBeLoadedException;

	Annotation dislike(long userId, BaseEntity resource, Annotation dislike,
			Session session, String context, String page, String lContext, String service) throws EventException,
			ResourceCouldNotBeLoadedException;

	Annotation dislike(long userId, BaseEntity resource, Session session, String context,
			String page, String lContext, String service) throws EventException, ResourceCouldNotBeLoadedException;

	int dislikeCount(BaseEntity resource);
	
	int dislikeCount(BaseEntity resource, Session session);

	int dislikeCount(Class<? extends BaseEntity> clazz, long resourceId);

	boolean isDislikedByUser(BaseEntity resource, long userId);

	void removeDislikeFromSocialActivity(long userId, long notificationId, long socialActivityId,
			int newDislikeCount, Session session, String context,
			String page, String lContext, String service) throws EventException,
			ResourceCouldNotBeLoadedException;
	
	boolean removeDislikeFromNode(long userId, long resourceId,
			Session session, String context, String page, String lContext, String service) throws EventException,
			ResourceCouldNotBeLoadedException;

	boolean removeDislikeFromComment(long userId, long commentId, Session session, String context) throws EventException, ResourceCouldNotBeLoadedException;

	boolean removeDislike(long userId, BaseEntity resource, Session session, String context,
			String page, String lContext, String service) throws EventException;
	
	List<User> getPeopleWhoDislikedResource(long resourceId, Class<? extends BaseEntity> clazz);
	
	int getDislikeCountForSocialActivity(long id);

}