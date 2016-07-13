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
import org.prosolo.services.interaction.data.CommentReplyFetchMode;
import org.prosolo.services.interaction.data.CommentSortData;
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
			CommentSortData commentSortData, long userId) throws DbConnectionException {
		return getComments(resourceType, resourceId, false, 0, commentSortData, 
				CommentReplyFetchMode.FetchReplies, userId);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CommentData> getComments(CommentedResourceType resourceType, long resourceId, 
			boolean paginate, int maxResults, CommentSortData commentSortData, 
			CommentReplyFetchMode replyFetchMode, long userId) throws DbConnectionException {
		try {
			CommentSortField sortField = commentSortData.getSortField();
			SortingOption sortOption = commentSortData.getSortOption();
			String order = sortOption == SortingOption.DESC ? "DESC" : "ASC";
			StringBuilder query = new StringBuilder("SELECT distinct comment ");
			if(replyFetchMode == CommentReplyFetchMode.FetchNumberOfReplies) {
				query.append(", count(child.id)");
			}
			query.append("FROM Comment1 comment " +
					"INNER JOIN fetch comment.user user ");
			if(replyFetchMode == CommentReplyFetchMode.FetchNumberOfReplies) {
				query.append("LEFT JOIN comment.childComments child ");
			}
			if(replyFetchMode == CommentReplyFetchMode.FetchReplies) {
				query.append("LEFT JOIN fetch comment.childComments child ");
			}
			query.append("WHERE comment.resourceType = :resType " +
					   "AND comment.commentedResourceId = :resourceId " +
					   "AND comment.parentComment is NULL "); 
			if(paginate && commentSortData.getPreviousId() > 0) {
				String sign = sortOption == SortingOption.ASC ? ">=" : "<=";
				query.append("AND comment." + sortField.getSortField() + sign + " :previousFieldValue " +
						 "AND NOT (comment." + sortField.getSortField() + " = :previousFieldValue AND comment.id >= :previousId) ");
			}
			if(replyFetchMode == CommentReplyFetchMode.FetchNumberOfReplies) {
				query.append("GROUP BY comment.id ");
			}
			query.append("ORDER BY comment." + sortField.getSortField() + " " + order +
					   ", comment.id DESC");
			if(replyFetchMode == CommentReplyFetchMode.FetchReplies) {
				query.append(", child.postDate ASC");
			}

			Query q = persistence.currentManager()
					.createQuery(query.toString())
					.setParameter("resType", resourceType)
					.setLong("resourceId", resourceId);
			
			if(paginate) {
				if(commentSortData.getPreviousId() > 0) {
					q.setLong("previousId", commentSortData.getPreviousId());
					if(sortField == CommentSortField.DATE_CREATED) {
						q.setTimestamp("previousFieldValue", commentSortData.getPreviousDate());
					} else if(sortField == CommentSortField.LIKE_COUNT) {
						q.setInteger("previousFieldValue", commentSortData.getPreviousLikeCount());
					}
				}
				q.setMaxResults(maxResults + 1);
			}
			
			List<CommentData> comments = new ArrayList<>();
			
			if(replyFetchMode == CommentReplyFetchMode.FetchNumberOfReplies) {
				@SuppressWarnings("unchecked")
				List<Object[]> res = q.list();
				if(res == null) {
					return new ArrayList<>();
				}
				for(Object[] row : res) {
					if(row != null) {
						Comment1 comment = (Comment1) row[0];
						int numberOfReplies = ((Long) row[1]).intValue();
						CommentData commentData = getCommentData(comment, userId, null, numberOfReplies);
						comments.add(commentData);
					}
				}
				
			} else {
				@SuppressWarnings("unchecked")
				List<Comment1> res = q.list();
				
				if(res == null) {
					return new ArrayList<>();
				}
		
				for(Comment1 comment : res) {
					CommentData commentData = getCommentData(comment, userId, null, 0);
					if(replyFetchMode == CommentReplyFetchMode.FetchReplies) {
						List<CommentData> childComments = new ArrayList<>();
						if(comment.getChildComments() != null) {
							for(Comment1 childComment : comment.getChildComments()) {
								CommentData child = getCommentData(childComment, userId, commentData, 0);
								childComments.add(child);
							}
							commentData.setChildComments(childComments);
							commentData.setNumberOfReplies(childComments.size());
						}
					}
					comments.add(commentData);
				}
			}
			return comments;
			
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading comments");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CommentData> getAllCommentReplies(CommentData parent, CommentSortData commentSortData, 
			long userId) throws DbConnectionException {
		try {
			CommentSortField sortField = commentSortData.getSortField();
			SortingOption sortOption = commentSortData.getSortOption();
			String order = sortOption == SortingOption.DESC ? "DESC" : "ASC";
			String query = "SELECT distinct comment FROM Comment1 comment " +
					       "INNER JOIN fetch comment.user user " +
					       "WHERE comment.parentComment.id = :id " +
					       "ORDER BY comment." + sortField.getSortField() + " " + order;
			
			Query q = persistence.currentManager()
					.createQuery(query.toString())
					.setLong("id", parent.getCommentId());
			
			@SuppressWarnings("unchecked")
			List<Comment1> res = q.list();
			
			if(res == null) {
				return new ArrayList<>();
			}
	
			List<CommentData> comments = new ArrayList<>();
			for(Comment1 comment : res) {
				CommentData commentData = getCommentData(comment, userId, parent, 0);
				comments.add(commentData);
			}
			return comments;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading comments");
		}
	}

	private CommentData getCommentData(Comment1 comment, long userId, CommentData parent,
			int numberOfReplies) {
		boolean liked = hasUserLikedComment(userId, comment.getId());
		return commentFactory.getCommentData(comment, liked, parent, numberOfReplies);
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
			String creatorFieldName = getCreatorFieldNameForResourceType(resourceType);
			String query = "SELECT res." + creatorFieldName + ".id FROM " + resourceType.getDbTableName() + " res " +
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

	private String getCreatorFieldNameForResourceType(CommentedResourceType resourceType) {
		switch(resourceType) {
			case Activity:
			case Competence:
				return "createdBy";
			case SocialActivity:
				return "actor";
			default:
				return null;
		}
	}
	
}
