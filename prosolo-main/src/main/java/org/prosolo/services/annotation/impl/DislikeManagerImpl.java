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
import org.prosolo.services.annotation.DislikeManager;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.interaction.DisikeManager")
public class DislikeManagerImpl extends AnnotationsManagerImpl implements DislikeManager {
	
	private static final long serialVersionUID = 9139064862893881828L;

	private static Logger logger = Logger.getLogger(DislikeManagerImpl.class);
	
	@Autowired private EventFactory eventFactory;
	
	@Override
	@Transactional (readOnly = false)
	public void dislikeSocialActivity(long userId, long notificationId, long socialActivityId,
			int newDislikeCount, Session session, String context, String page, 
			String lContext, String service) 
			throws EventException, ResourceCouldNotBeLoadedException {
		
		if (notificationId > 0) {
			SocialActivityNotification notification = (SocialActivityNotification) session.get(SocialActivityNotification.class, notificationId);
			notification.setDisliked(true);
			session.save(notification);
		}
		
		SocialActivity socialActivity = (SocialActivity) session.get(SocialActivity.class, socialActivityId);
		socialActivity.setDislikeCount(newDislikeCount);
		session.save(socialActivity);
    	
    	dislike(userId, socialActivity, session, context, page, lContext, service);
	}
	
	@Override
	public Annotation dislikeComment(long userId, long commentId,
			Session session, String context) throws EventException,
			ResourceCouldNotBeLoadedException {
		Comment resource = (Comment) session.get(Comment.class, commentId);
		Annotation dislike = new CommentAnnotation(AnnotationType.Dislike);
		return dislike(userId, resource, dislike, session, context, null, null, null);
	}
	
	//changed because of new context approach
	@Override
	public Annotation dislikeNode(long userId, long resourceId, Session session,
			String context, String page, String lContext, String service) throws EventException,
			ResourceCouldNotBeLoadedException {
		Node resource = loadResource(Node.class, resourceId, session);
		Annotation dislike = new NodeAnnotation(AnnotationType.Dislike);
		return dislike(userId, resource, dislike, session, context, page, lContext, service);
	}
	
	//changed because of new context approach
	@Override
	public Annotation dislike(long userId, BaseEntity resource, Annotation dislike,
			Session session, String context, String page, String lContext, String service) throws EventException,
			ResourceCouldNotBeLoadedException {
		logger.debug("Adding dislike of a resource " + resource.getId() + " by the user " + userId);
		
		if (userId > 0) {
			User user = loadResource(User.class, userId);
			
			dislike.setMaker(user);
			dislike.setDateCreated(new Date());
			dislike.setResource(resource);
			session.save(dislike);
			
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("context", context);
			
			eventFactory.generateEvent(EventType.Dislike, userId, resource, target(resource), page, lContext,
					service, parameters);
			
			return dislike;
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
	@Transactional (readOnly = false)
	public Annotation dislike(long userId, BaseEntity resource, Session session, String context,
			String page, String lContext, String service) throws EventException, ResourceCouldNotBeLoadedException {
		logger.debug("Adding dislike of a resource " + resource.getId() + " by the user " + userId);
		
		if (resource != null && userId > 0) {
			Annotation dislike = createAnnotation(resource, AnnotationType.Dislike);
			
			dislike.setDateCreated(new Date());
			dislike.setMaker(loadResource(User.class, userId));
			dislike.setResource(resource);
			session.save(dislike);
			
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("context", context);
				
			//migration to new context approach
			eventFactory.generateEvent(EventType.Dislike, userId, resource, target(resource), page, 
					lContext, service, parameters);
			return dislike;
		} 
		return null;
	}

	@Override
	@Transactional (readOnly = true)
	public int dislikeCount(BaseEntity resource) {
		return dislikeCount(resource, getPersistence().currentManager());
	}
	
	@Override
	@Transactional
	public int dislikeCount(BaseEntity resource, Session session) {
		logger.debug("Calculating dislike count for the resource " + resource.getId());
		
		return super.annotationCount(resource.getId(), resource.getClass(), 0, AnnotationType.Dislike, session);
	}

	@Override
	@Transactional (readOnly = true)
	public int dislikeCount(Class<? extends BaseEntity> clazz, long resourceId) {
		return super.annotationCount(resourceId, clazz, 0, AnnotationType.Dislike);
	}
	
	@Override
	@Transactional (readOnly = true)
	public boolean isDislikedByUser(BaseEntity resource, long userId) {
		logger.debug("Returning boolean if the resource "+resource.getId()+" is disliked by the user "+userId);
		
		return super.isAnnotatedByUser(resource, userId, AnnotationType.Dislike);
	}
	
	@Override
	public void removeDislikeFromSocialActivity(long userId, long notificationId, long socialActivityId,
			int newDislikeCount, Session session, String context,
			String page, String lContext, String service) throws EventException,
			ResourceCouldNotBeLoadedException {
		
		if (notificationId > 0) {
			SocialActivityNotification notification = (SocialActivityNotification) session.get(SocialActivityNotification.class, notificationId);
			notification.setDisliked(false);
			session.save(notification);
		}

		SocialActivity socialActivity = (SocialActivity) session.get(SocialActivity.class, socialActivityId);
		socialActivity.setDislikeCount(newDislikeCount);
		session.save(socialActivity);
    	
    	removeDislike(userId, socialActivity, session, context, page, lContext, service);
	}

	@Override
	public boolean removeDislikeFromNode(long userId, long resourceId,
			Session session, String context, String page, String lContext, String service) throws EventException,
			ResourceCouldNotBeLoadedException {
		Node resource = (Node) session.get(Node.class, resourceId);
		return removeDislike(userId, resource, session, context, page, lContext, service);
	}
	
	@Override
	public boolean removeDislikeFromComment(long userId, long commentId,
			Session session, String context) throws EventException,
			ResourceCouldNotBeLoadedException {
		Comment comment = (Comment) session.get(Comment.class, commentId);
		return removeDislike(userId, comment, session, context, null, null, null);
	}

	@Override
	@Transactional (readOnly = false)
	public boolean removeDislike(long userId, BaseEntity resource, Session session, String context,
			String page, String lContext, String service) throws EventException {
		logger.debug("Removing dislike from a resource '"+resource.getId()+"' by the user "+userId);
		
		boolean successful = super.removeAnnotation(resource, userId, AnnotationType.Dislike, true, session);
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("context", context);
		
		//eventFactory.generateEvent(EventType.RemoveDislike, user, resource, parameters);
		//changed new context approach
		eventFactory.generateEvent(EventType.RemoveDislike, userId, resource, null, page, lContext, 
				service, parameters);
		return successful;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<User> getPeopleWhoDislikedResource(long resourceId, Class<? extends BaseEntity> clazz) {
		return super.getPeopleWhoAnnotatedResource(resourceId, clazz, AnnotationType.Dislike);
	}

	@Override
	@Transactional (readOnly = true)
	public int getDislikeCountForSocialActivity(long id) {
		String queryString = 
			"SELECT socialActivity.dislikeCount " +
			"FROM SocialActivity socialActivity " +
			"WHERE socialActivity.id = :id ";
		
		Integer result = (Integer) persistence.currentManager().createQuery(queryString)
				.setLong("id", id)
				.uniqueResult();
		
		return result.intValue();
	}
	
}
