package org.prosolo.services.annotation;

import java.util.List;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.annotation.Annotation;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.EventException;

public interface LikeManager {
	
	void likeSocialActivity(User user, long notificationId,
			long socialActivityId, int newLikeCount, Session session, String context) throws EventException,
			ResourceCouldNotBeLoadedException;

	//Annotation like(User user, BaseEntity resource, String context) throws EventException;

	Annotation like(User user, BaseEntity resource, Session session, String context) throws EventException;
	
	//Annotation like(User user, long resourceId, String context) throws ResourceCouldNotBeLoadedException, EventException;
	
	int likeCount(BaseEntity resource);
	
	int likeCount(BaseEntity resource, Session session);
	
	int likeCount(Class<? extends BaseEntity> clazz, long resourceId);

	boolean isLikedByUser(BaseEntity resource, User user);
	
	void removeLikeFromSocialActivity(User user, long notificationId, long socialActivityId, 
			int newLikeCount, Session session, String context) throws EventException, ResourceCouldNotBeLoadedException;

	boolean removeLike(User user, BaseEntity resource, Session session, String context) throws EventException;

	boolean removeLikeFromNode(User user, long resourceId, Session session, String context) throws EventException, ResourceCouldNotBeLoadedException;
	
	boolean removeLikeFromComment(User user, long commentId, Session session, String context) throws EventException, ResourceCouldNotBeLoadedException;

	int getLikeCountForSocialActivity(long id);
	
	List<User> getPeopleWhoLikedResource(long resourceId, Class<? extends BaseEntity> clazz);

	Annotation likeComment(User user, long commentId, Session session, String context) throws EventException,
			ResourceCouldNotBeLoadedException;

	Annotation likeNode(User user, long resourceId, Session session, String context) throws EventException,
			ResourceCouldNotBeLoadedException;

	Annotation like(User user, BaseEntity resource, Annotation like, Session session, String context)
			throws EventException, ResourceCouldNotBeLoadedException;

}