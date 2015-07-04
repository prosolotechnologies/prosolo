package org.prosolo.services.annotation;

import java.util.List;

import org.hibernate.Session;
import org.prosolo.domainmodel.annotation.Annotation;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.user.User;
import org.prosolo.services.event.EventException;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;

public interface DislikeManager {
	
	void dislikeSocialActivity(User user, long notificationId, long socialActivityId,
			int newDislikeCount, Session session, String context) throws EventException,
			ResourceCouldNotBeLoadedException;

	Annotation dislikeComment(User user, long commentId, Session session, String context) throws EventException,
	ResourceCouldNotBeLoadedException;

	Annotation dislikeNode(User user, long resourceId, Session session, String context) throws EventException,
	ResourceCouldNotBeLoadedException;

	Annotation dislike(User user, BaseEntity resource, Annotation like, Session session, String context)
	throws EventException, ResourceCouldNotBeLoadedException;

	Annotation dislike(User user, BaseEntity resource, Session session, String context) throws EventException;

	int dislikeCount(BaseEntity resource);
	
	int dislikeCount(BaseEntity resource, Session session);

	int dislikeCount(Class<? extends BaseEntity> clazz, long resourceId);

	boolean isDislikedByUser(BaseEntity resource, User user);

	void removeDislikeFromSocialActivity(User user, long notificationId, long socialActivityId,
			int newDislikeCount, Session session, String context) throws EventException, ResourceCouldNotBeLoadedException;
	
	boolean removeDislikeFromNode(User user, long resourceId, Session session, String context) throws EventException, ResourceCouldNotBeLoadedException;

	boolean removeDislikeFromComment(User user, long commentId, Session session, String context) throws EventException, ResourceCouldNotBeLoadedException;

	boolean removeDislike(User user, BaseEntity resource, Session session, String context) throws EventException;
	
	List<User> getPeopleWhoDislikedResource(long resourceId, Class<? extends BaseEntity> clazz);
	
	int getDislikeCountForSocialActivity(long id);

}