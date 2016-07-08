package org.prosolo.services.interaction.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.annotation.AnnotatedResource;
import org.prosolo.common.domainmodel.annotation.AnnotationType;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.annotation.Annotation1Manager;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.event.context.data.LearningContextData;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interaction.CommentManager;
import org.prosolo.services.interaction.data.CommentData;
import org.prosolo.services.interaction.data.CommentSortField;
import org.prosolo.services.interaction.data.factory.CommentDataFactory;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.util.SortingOption;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service("org.prosolo.services.interaction.CommentManager")
public class CommentManagerImpl extends AbstractManagerImpl implements CommentManager {

	private static final long serialVersionUID = -3413646255744360158L;

	private static Logger logger = Logger.getLogger(CommentManagerImpl.class);
	
	@Inject
	private EventFactory eventFactory;
	@Inject
	private CommentDataFactory commentFactory;
	@Inject
	private Annotation1Manager annotationManager;
	@Inject
	private ResourceFactory resourceFactory;

	@Override
	@Transactional(readOnly = true)
	public List<CommentData> getAllComments(CommentedResourceType resourceType, long resourceId, 
			CommentSortField sortField, SortingOption sortOption, long userId) 
					throws DbConnectionException {
		return getComments(resourceType, resourceId, false, 0, 0, sortField, sortOption, userId);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CommentData> getComments(CommentedResourceType resourceType, long resourceId, 
			boolean paginate, int offset, int maxResults, CommentSortField sortField, 
			SortingOption sortOption, long userId) throws DbConnectionException {
		try {
			String order = sortOption == SortingOption.DESC ? "DESC" : "ASC";
			String query = "SELECT distinct comment FROM Comment1 comment " +
						   "INNER JOIN fetch comment.user user " +
						   "LEFT JOIN fetch comment.childComments child " +
						   "WHERE comment.resourceType = :resType " +
						   "AND comment.commentedResourceId = :resourceId " +
						   "AND comment.parentComment is NULL " +
						   "ORDER BY comment." + sortField.getSortField() + " " + order +
						   ", child.postDate ASC";
			
			Query q = persistence.currentManager()
					.createQuery(query)
					.setParameter("resType", resourceType)
					.setLong("resourceId", resourceId);
			if(paginate) {
				q.setFirstResult(offset).setMaxResults(maxResults);
			}
			
			@SuppressWarnings("unchecked")
			List<Comment1> res = q.list();
			
			if(res == null) {
				return new ArrayList<>();
			}
	
			List<CommentData> comments = new ArrayList<>();
			for(Comment1 comment : res) {
				CommentData commentData = getCommentData(comment, userId, null);
				List<CommentData> childComments = new ArrayList<>();
				if(comment.getChildComments() != null) {
					for(Comment1 childComment : comment.getChildComments()) {
						CommentData child = getCommentData(childComment, userId, commentData);
						childComments.add(child);
					}
					commentData.setChildComments(childComments);
				}
				comments.add(commentData);
			}
			return comments;
			
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading comments");
		}
	}

	private CommentData getCommentData(Comment1 comment, long userId, CommentData parent) {
		boolean liked = hasUserLikedComment(userId, comment.getId());
		return commentFactory.getCommentData(comment, liked, parent);
	}

	private boolean hasUserLikedComment(long userId, long resourceId) {
		return annotationManager.hasUserAnnotatedResource(userId, resourceId, AnnotationType.Like, 
				AnnotatedResource.Comment);
	}
	
	@Override
	@Transactional (readOnly = false)
	public void likeComment(long userId, long commentId, LearningContextData context) 
			throws DbConnectionException {
		try {
			annotationManager.createAnnotation(userId, commentId, AnnotatedResource.Comment, 
					AnnotationType.Like);
			String query = "UPDATE Comment1 comment " +
						   "SET comment.likeCount = comment.likeCount + 1 " +
						   "WHERE comment.id = :commentId";
			persistence.currentManager()
				.createQuery(query)
				.setLong("commentId", commentId)
				.executeUpdate();
			
			//to avoid retrieving data from database
			User user = new User();
			user.setId(userId);
			Comment1 comment = new Comment1();
			comment.setId(commentId);
			
			eventFactory.generateEvent(EventType.Like, user.getId(), comment, null, 
					context.getPage(), context.getLearningContext(), context.getService(), null);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving comment like");
		}
	}
	
	@Override
	@Transactional (readOnly = false)
	public void unlikeComment(long userId, long commentId, LearningContextData context) 
			throws DbConnectionException {
		try {
			annotationManager.deleteAnnotation(userId, commentId, AnnotatedResource.Comment, 
					AnnotationType.Like);
			String query = "UPDATE Comment1 comment " +
						   "SET comment.likeCount = comment.likeCount - 1 " +
						   "WHERE comment.id = :commentId";
			persistence.currentManager()
				.createQuery(query)
				.setLong("commentId", commentId)
				.executeUpdate();
			
			//to avoid retrieving data from database
			User user = new User();
			user.setId(userId);
			Comment1 comment = new Comment1();
			comment.setId(commentId);
			
			eventFactory.generateEvent(EventType.RemoveLike, user.getId(), comment, null, context.getPage(), 
					context.getLearningContext(), context.getService(), null);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving comment like");
		}
	}
	
//	@Override
//	@Transactional (readOnly = false)
//	public Comment1 saveNewCompetenceComment(CommentData data, long userId, 
//			LearningContextData context) throws DbConnectionException {
//		try {
//			return saveNewComment(data, userId, CommentedResourceType.Competence, context);
//		} catch(Exception e) {
//			logger.error(e);
//			e.printStackTrace();
//			throw new DbConnectionException("Error while saving comment");
//		}
//	}

	@Override
	@Transactional(readOnly = false)
	public Comment1 saveNewComment(CommentData data, long userId, CommentedResourceType resource,
			LearningContextData context) throws DbConnectionException {
		try {
			Comment1 comment = resourceFactory.saveNewComment(data, userId, resource);
			
			//avoid queries to db
			User actor = new User();
			actor.setId(userId);
			BaseEntity target = null;
			switch(resource) {
				case Activity:
					target = new Activity1();
					break;
				case Competence:
					target = new Competence1();
					break;
				case SocialActivity:
					target = new SocialActivity1();
					break;
			}
			target.setId(data.getCommentedResourceId());
			
			//TODO check with Nikola if target (Competence, Activity) is needed
			EventType eventType = data.getParent() != null ? EventType.Comment_Reply : EventType.Comment;
			eventFactory.generateEvent(eventType, actor.getId(), comment, target, 
					context.getPage(), context.getLearningContext(), context.getService(), null);
			
			return comment;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving comment");
		}
		
	}
	
	@Override
	@Transactional (readOnly = false)
	public void updateComment(CommentData data, long userId, LearningContextData context) 
			throws DbConnectionException {
		try {
			Comment1 comment = (Comment1) persistence.currentManager().load(Comment1.class, 
					data.getCommentId());
			comment.setDescription(data.getComment());
			
			//avoid queries to db
			User actor = new User();
			actor.setId(userId);
			
			eventFactory.generateEvent(EventType.Edit, actor.getId(), comment, null, 
					context.getPage(), context.getLearningContext(), context.getService(), null);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating comment");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Long> getIdsOfUsersThatCommentedResource(CommentedResourceType resourceType, long resourceId, 
			List<Long> usersToExclude) throws DbConnectionException {
		try {
			String query = "SELECT distinct comment.user.id FROM Comment1 comment " +
						   "WHERE comment.resourceType = :resType " +
						   "AND comment.commentedResourceId = :resourceId " +
						   "AND comment.user.id NOT IN (:usersToExclude)";
			
			@SuppressWarnings("unchecked")
			List<Long> res = persistence.currentManager()
					.createQuery(query)
					.setParameter("resType", resourceType)
					.setLong("resourceId", resourceId)
					.setParameterList("usersToExclude", usersToExclude)
					.list();
			
			if(res == null) {
				return new ArrayList<>();
			}
	
			return res;
			
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading user ids");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public Long getCommentedResourceCreatorId(CommentedResourceType resourceType, long resourceId) 
			throws DbConnectionException {
		try {
			//TODO when comments are implemented for socialactivity this method should be changed
			//because socialactivity maybe won't have createdBy relationship
			String query = "SELECT res.createdBy.id FROM " + resourceType.getDbTableName() + " res " +
						   "WHERE res.id = :resId";
			
			Long id = (Long) persistence.currentManager()
					.createQuery(query)
					.setLong("resId", resourceId)
					.uniqueResult();

			return id;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading user id");
		}
	}
	
}
