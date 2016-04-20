package org.prosolo.services.interaction.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.annotation.AnnotatedResource;
import org.prosolo.common.domainmodel.annotation.AnnotationType;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.annotation.Annotation1Manager;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.event.context.data.LearningContextData;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interaction.CommentManager;
import org.prosolo.services.interaction.data.CommentData;
import org.prosolo.services.interaction.data.CommentSortField;
import org.prosolo.services.interaction.data.factory.CommentDataFactory;
import org.prosolo.services.lti.exceptions.DbConnectionException;
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
				CommentData commentData = getCommentData(comment, userId, 0);
				List<CommentData> childComments = new ArrayList<>();
				if(comment.getChildComments() != null) {
					for(Comment1 childComment : comment.getChildComments()) {
						CommentData child = getCommentData(childComment, userId, comment.getId());
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

	private CommentData getCommentData(Comment1 comment, long userId, long parentId) {
		boolean liked = hasUserLikedComment(userId, comment.getId());
		return commentFactory.getCommentData(comment, liked, parentId);
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
			
			eventFactory.generateEvent(EventType.Like, user, comment, null, 
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
			
			eventFactory.generateEvent(EventType.RemoveLike, user, comment, null, context.getPage(), 
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
			Comment1 comment = new Comment1();
			comment.setDescription(data.getComment());
			comment.setCommentedResourceId(data.getCommentedResourceId());
			comment.setResourceType(resource);
			comment.setInstructor(data.isInstructor());
			//comment.setDateCreated(data.getDateCreated());
			comment.setPostDate(data.getDateCreated());
			User user = (User) persistence.currentManager().load(User.class, userId);
			comment.setUser(user);
			if(data.getParentCommentId() > 0) {
				Comment1 parent = (Comment1) persistence.currentManager().load(Comment1.class, 
						data.getParentCommentId());
				comment.setParentComment(parent);
			}
			
			saveEntity(comment);
			
			//avoid queries to db
			User actor = new User();
			actor.setId(userId);
			
			//TODO check with Nikola if target (Competence, Activity) is needed
			eventFactory.generateEvent(EventType.Comment, actor, comment, null, 
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
			
			eventFactory.generateEvent(EventType.Edit, actor, comment, null, 
					context.getPage(), context.getLearningContext(), context.getService(), null);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating comment");
		}
	}
	
}
