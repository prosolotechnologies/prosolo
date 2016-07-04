package org.prosolo.services.annotation.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activitywall.old.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.old.SocialActivityNotification;
import org.prosolo.common.domainmodel.activitywall.old.comments.Comment;
import org.prosolo.common.domainmodel.annotation.Annotation;
import org.prosolo.common.domainmodel.annotation.AnnotationType;
import org.prosolo.common.domainmodel.annotation.CommentAnnotation;
import org.prosolo.common.domainmodel.annotation.NodeAnnotation;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.annotation.LikeManager;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional (readOnly = false)
@Service("org.prosolo.services.interaction.LikeManager")
public class LikeManagerImpl extends AnnotationsManagerImpl implements LikeManager {
	
	private static final long serialVersionUID = -1111383992946142882L;

	private static Logger logger = Logger.getLogger(LikeManagerImpl.class);
	
	@Autowired private EventFactory eventFactory;
	
	@Override
	public void likeSocialActivity(User user, long notificationId, long socialActivityId, 
			int newLikeCount, Session session, String context, String page,
			String lContext, String service) throws EventException, ResourceCouldNotBeLoadedException {
		
		// it can happen that we don't have notificationId, like in the hashtag search
		
		if (notificationId > 0) {
			SocialActivityNotification notification = (SocialActivityNotification) session.get(SocialActivityNotification.class, notificationId);
			notification.setLiked(true);
			session.save(notification);
		}

		SocialActivity socialActivity = (SocialActivity) session.get(SocialActivity.class, socialActivityId);
		socialActivity.setLikeCount(newLikeCount);
		session.save(socialActivity);
    	
    	like(user, socialActivity, session, context, page, lContext, service);
	}
	
	@Override
	@Transactional (readOnly = false)
	public Annotation likeComment(User user, long commentId, Session session, String context,
			String page, String learningContext, String service) throws EventException, ResourceCouldNotBeLoadedException {
		Comment resource = (Comment) session.get(Comment.class, commentId);
		Annotation like = new CommentAnnotation(AnnotationType.Like);
		return like(user, resource, like, session, context, page, learningContext, service);
	}
	
	//changed because new context approach
	@Override
	@Transactional (readOnly = false)
	public Annotation likeNode(User user, long resourceId, Session session, String context, String page,
			String lContext, String service) throws EventException, ResourceCouldNotBeLoadedException {
		Node resource = loadResource(Node.class, resourceId, session);
		Annotation like = new NodeAnnotation(AnnotationType.Like);
		return like(user, resource, like, session, context, page, lContext, service);
	}
	
	//changed because of new context approach
	@Override
	@Transactional (readOnly = false)
	public Annotation like(User user, BaseEntity resource, Annotation like, Session session, String context,
			String page, String lContext, String service) throws EventException, ResourceCouldNotBeLoadedException {
		logger.debug("Adding like of a resource "+resource.getId()+" by the user "+user);
		
		if (user != null) {
			user = (User) session.merge(user);
			
			like.setMaker(user);
			like.setDateCreated(new Date());
			like.setResource(resource);
			session.save(like);
			session.flush();
			
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("context", context);
			
			//changed for migration to new context approach
			eventFactory.generateEvent(EventType.Like, user, resource, target(resource), 
					page, lContext, service, parameters);
			
			return like;
		}
		return null;
	}
	
	@Override
	@Transactional (readOnly = false)
	public Annotation like(User user, BaseEntity resource, Session session, String context,
			String page, String lContext, String service) throws EventException {
		logger.debug("Adding like of a resource "+resource.getId()+" by the user "+user);
		
		if (resource != null && user != null) {
			Annotation like = createAnnotation(resource, AnnotationType.Like);
			
			like.setDateCreated(new Date());
			like.setMaker(user);
			like.setResource(resource);
			session.save(like);
			
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("context", context);
			
			//changed for migration to new context approach
			eventFactory.generateEvent(EventType.Like, user, resource, target(resource), 
					page, lContext, service, parameters);
			return like;
		}
		return null;
	}
	
	private BaseEntity target(BaseEntity resource) {
		if (resource instanceof Node) {
			return null;
		} else if (resource instanceof SocialActivity) {
			return ((SocialActivity) resource).getMaker();
		} else {
			return null;
		}
	}
	
	@Override
	@Transactional
	public int likeCount(BaseEntity resource) {
		return likeCount(resource, getPersistence().currentManager());
	}
	
	@Override
	@Transactional
	public int likeCount(BaseEntity resource, Session session) {
		logger.debug("Calculating like count for the resource "+resource.getId());
		
		return super.annotationCount(resource.getId(), resource.getClass(), null, AnnotationType.Like, session);
	}
 
	@Override
	@Transactional (readOnly = true)
	public int likeCount(Class<? extends BaseEntity> clazz, long resourceId) {
		return super.annotationCount(resourceId, clazz, null, AnnotationType.Like);
	}
	
	@Override
	@Transactional (readOnly = true)
	public boolean isLikedByUser(BaseEntity resource, User user) {
		logger.debug("Returning boolean if the resource "+resource.getId()+" is liked by the user "+user);
		
		return super.isAnnotatedByUser(resource, user, AnnotationType.Like);
	}
	
	@Override
	public void removeLikeFromSocialActivity(User user, long notificationId, 
			long socialActivityId, int newLikeCount, Session session, String context,
			String page, String lContext, String service) 
			throws EventException, ResourceCouldNotBeLoadedException {
		
		if (notificationId > 0) {
			SocialActivityNotification notification = (SocialActivityNotification) session.get(SocialActivityNotification.class, notificationId);
			notification.setLiked(false);
			session.save(notification);
		}
		
		SocialActivity socialActivity = (SocialActivity) session.get(SocialActivity.class, socialActivityId);
		socialActivity.setLikeCount(newLikeCount);
		session.save(socialActivity);
    	
    	removeLike(user, socialActivity, session, context, page, lContext, service);
	}
	
	@Override
	@Transactional (readOnly = false)
	public boolean removeLikeFromNode(User user, long resourceId, Session session, String context,
			String page, String lContext, String service) throws EventException, ResourceCouldNotBeLoadedException {
		Node resource = (Node) session.get(Node.class, resourceId);
		return removeLike(user, resource, session, context, page, lContext, service);
	}
	
	@Override
	@Transactional (readOnly = false)
	public boolean removeLikeFromComment(User user, long commentId, Session session, String context,
			String page, String learningContext, String service) throws EventException, ResourceCouldNotBeLoadedException {
		Comment comment = (Comment) session.get(Comment.class, commentId);
		return removeLike(user, comment, session, context, page, learningContext, service);
	}

	//changed because of new context approach
	@Override
	public boolean removeLike(User user, BaseEntity resource, Session session, String context,
			String page, String lContext, String service) throws EventException {
		logger.debug("Unliking resource "+resource.getId()+" by the user "+user);
		
		boolean successful = super.removeAnnotation(resource, user, AnnotationType.Like, true, session);
		
		session.flush();
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("context", context);
		
		//eventFactory.generateEvent(EventType.RemoveLike, user, resource, parameters);
		//changed because of migration to new context approach
		eventFactory.generateEvent(EventType.RemoveLike, user, resource, null, page, lContext, 
				service, parameters);
		return successful;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<User> getPeopleWhoLikedResource(long resourceId, Class<? extends BaseEntity> clazz) {
		return super.getPeopleWhoAnnotatedResource(resourceId, clazz, AnnotationType.Like);
	}
	
	@Override
	@Transactional (readOnly = true)
	public int getLikeCountForSocialActivity(long id) {
		String queryString = 
			"SELECT socialActivity.likeCount " +
			"FROM SocialActivity socialActivity " +
			"WHERE socialActivity.id = :id ";
		
		Integer result = (Integer) persistence.currentManager().createQuery(queryString)
				.setLong("id", id)
				.uniqueResult();
		
		return result.intValue();
	}
 
}
