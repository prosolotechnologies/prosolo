package org.prosolo.services.annotation;

import java.util.List;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.annotation.Annotation;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.EventException;

public interface LikeManager {
	
	void likeSocialActivity(long userId, long notificationId, long socialActivityId, 
			int newLikeCount, Session session, String context, String page,
			String lContext, String service) throws EventException, ResourceCouldNotBeLoadedException;

	Annotation like(long userId, BaseEntity resource, Session session, String context,
			String page, String lContext, String service) throws EventException, ResourceCouldNotBeLoadedException;
	
	int likeCount(BaseEntity resource);
	
	int likeCount(BaseEntity resource, Session session);
	
	int likeCount(Class<? extends BaseEntity> clazz, long resourceId);

	boolean isLikedByUser(BaseEntity resource, long userId);
	
	void removeLikeFromSocialActivity(long userId, long notificationId, 
			long socialActivityId, int newLikeCount, Session session, String context,
			String page, String lContext, String service) 
			throws EventException, ResourceCouldNotBeLoadedException;

	boolean removeLike(long userId, BaseEntity resource, Session session, String context,
			String page, String lContext, String service) throws EventException;

	 boolean removeLikeFromNode(long userId, long resourceId, Session session, String context,
				String page, String lContext, String service) throws EventException, ResourceCouldNotBeLoadedException;
	
	 boolean removeLikeFromComment(long userId, long commentId, Session session, String context,
				String page, String learningContext, String service) throws EventException, ResourceCouldNotBeLoadedException;

	int getLikeCountForSocialActivity(long id);
	
	List<User> getPeopleWhoLikedResource(long resourceId, Class<? extends BaseEntity> clazz);

	Annotation likeComment(long userId, long commentId, Session session, String context,
			String page, String learningContext, String service) throws EventException, ResourceCouldNotBeLoadedException;

	Annotation likeNode(long userId, long resourceId, Session session, String context, String page,
			String lContext, String service) throws EventException,
			ResourceCouldNotBeLoadedException;

	Annotation like(long userId, BaseEntity resource, Annotation like, Session session, String context, 
			String page, String lContext, String service)
			throws EventException, ResourceCouldNotBeLoadedException;

}