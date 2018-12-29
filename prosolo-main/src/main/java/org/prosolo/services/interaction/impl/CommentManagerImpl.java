package org.prosolo.services.interaction.impl;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.annotation.AnnotatedResource;
import org.prosolo.common.domainmodel.annotation.AnnotationType;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.annotation.Annotation1Manager;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interaction.CommentManager;
import org.prosolo.services.interaction.data.CommentData;
import org.prosolo.services.interaction.data.CommentReplyFetchMode;
import org.prosolo.services.interaction.data.CommentSortData;
import org.prosolo.services.interaction.data.CommentSortField;
import org.prosolo.services.interaction.data.factory.CommentDataFactory;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.Role;
import org.prosolo.services.user.data.UserData;
import org.prosolo.services.common.data.SortingOption;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


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
	private CommentManager self;
	@Inject
	private CredentialManager credManager;
	@Inject
	private Activity1Manager actManager;

	@Override
	@Transactional(readOnly = true)
	public List<CommentData> getAllComments(CommentedResourceType resourceType, long resourceId, 
			CommentSortData commentSortData, long userId, boolean loadOnlyCommentsFromUsersLearningSameDeliveries) throws DbConnectionException {
		return getAllComments(resourceType, resourceId, commentSortData, 
				CommentReplyFetchMode.FetchReplies, userId, loadOnlyCommentsFromUsersLearningSameDeliveries);
	}
	
	private List<CommentData> getAllComments(CommentedResourceType resourceType, long resourceId, 
			CommentSortData commentSortData, CommentReplyFetchMode replyFetchMode, long userId,
		    boolean loadOnlyCommentsFromUsersLearningSameDeliveries) {
		return getComments(resourceType, resourceId, false, 0, commentSortData, 
				replyFetchMode, userId, loadOnlyCommentsFromUsersLearningSameDeliveries);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CommentData> getComments(CommentedResourceType resourceType, long resourceId, 
			boolean paginate, int maxResults, CommentSortData commentSortData, 
			CommentReplyFetchMode replyFetchMode, long userId, boolean loadOnlyCommentsFromUsersLearningSameDeliveries)
			throws DbConnectionException {
	
		if (replyFetchMode == CommentReplyFetchMode.FetchNumberOfReplies) {
			return getCommentsWithNumberOfReplies(resourceType, resourceId, paginate, maxResults, commentSortData, userId, loadOnlyCommentsFromUsersLearningSameDeliveries);
		} else if (replyFetchMode == CommentReplyFetchMode.FetchReplies) {
			return getCommentsWithReplies(resourceType, resourceId, paginate, maxResults, commentSortData, userId, loadOnlyCommentsFromUsersLearningSameDeliveries);
		} else {
			logger.warn("Comment loading with mode " + replyFetchMode + " is not supported");
			return new LinkedList<>();
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CommentData> getCommentsWithNumberOfReplies(CommentedResourceType resourceType, long resourceId, 
			boolean paginate, int maxResults, CommentSortData commentSortData, 
			long userId, boolean loadOnlyCommentsFromUsersLearningSameDeliveries) throws DbConnectionException {
		try {
			List<Long> deliveries = null;
			if (loadOnlyCommentsFromUsersLearningSameDeliveries) {
				long compId = resourceType == CommentedResourceType.Competence
						? resourceId
						: actManager.getCompetenceIdForActivity(resourceId);
				deliveries = credManager.getIdsOfDeliveriesUserIsLearningContainingCompetence(userId, compId);
			}
			CommentSortField sortField = commentSortData.getSortField();
			SortingOption sortOption = commentSortData.getSortOption();
			String order = sortOption == SortingOption.DESC ? "DESC" : "ASC";
			
			StringBuilder query = new StringBuilder(
					"SELECT DISTINCT comment.id, comment.description,  " +
					"comment.instructor, comment.likeCount, comment.commentedResourceId, comment.postDate, " +
					"user.id, user.name, user.lastname, user.avatarUrl, user.position, count(child.id) AS numberOfReplies " +
					"FROM Comment1 comment " +
					"INNER JOIN comment.user user " +
					"LEFT JOIN comment.childComments child " +
					"WHERE comment.resourceType = :resType " +
				    "AND comment.commentedResourceId = :resourceId " +
					"AND comment.parentComment is NULL ");

			if (loadOnlyCommentsFromUsersLearningSameDeliveries) {
				if (!deliveries.isEmpty()) {
					query.append("AND (user.id = :userId OR EXISTS " +
							"(from TargetCredential1 cred WHERE cred.user.id = user.id AND cred.credential.id IN (:credentials))) ");
				} else {
					query.append("AND user.id = :userId ");
				}
			}
			
			if (paginate && commentSortData.getPreviousId() > 0) {
				String sign = sortOption == SortingOption.ASC ? ">=" : "<=";
				query.append("AND comment." + sortField.getSortField() + sign + " :previousFieldValue " +
						 "AND NOT (comment." + sortField.getSortField() + " = :previousFieldValue AND comment.id >= :previousId) ");
			}
			
			query.append("GROUP BY comment.id, comment.description,  " +
				"comment.instructor, comment.likeCount, comment.commentedResourceId, comment.postDate, " +
				"user.id, user.name, user.lastname, user.avatarUrl, user.position " +
				"ORDER BY comment." + sortField.getSortField() + " " + order +
					   ", comment.id DESC");

			Query q = persistence.currentManager()
					.createQuery(query.toString())
					.setParameter("resType", resourceType)
					.setLong("resourceId", resourceId);

			if (loadOnlyCommentsFromUsersLearningSameDeliveries) {
				q.setLong("userId", userId);
				if (!deliveries.isEmpty()) {
					q.setParameterList("credentials", deliveries);
				}
			}
			
			if (paginate) {
				if (commentSortData.getPreviousId() > 0) {
					q.setLong("previousId", commentSortData.getPreviousId());
					
					if (sortField == CommentSortField.DATE_CREATED) {
						q.setTimestamp("previousFieldValue", commentSortData.getPreviousDate());
					} else if (sortField == CommentSortField.LIKE_COUNT) {
						q.setInteger("previousFieldValue", commentSortData.getPreviousLikeCount());
					}
				}
				q.setMaxResults(maxResults + 1);
			}
			
			List<CommentData> comments = new ArrayList<>();
			
			@SuppressWarnings("unchecked")
			List<Object[]> res = q.list();
			
			if (res == null) {
				return new ArrayList<>();
			}
			
			for (Object[] row : res) {
				if (row != null) {
					CommentData commentData = commentFactory.getCommentData(
							(Long) row[0],			// id, 
							(String) row[1],		// description, 
							new UserData((Long) row[6], (String) row[7], (String) row[8], (String) row[9], (String) row[10], null, false),
							(Boolean) row[2],		// isInstructor, 
							(Integer) row[3],		// likeCount,
							hasUserLikedComment(userId, (Long) row[0]), 
							(Long) row[4],			// commentedResourceId, 
							(Date) row[5],			// dateCreated, 
							null,					// parent
							((Long) row[11]).intValue());		// numberOfReplies
					comments.add(commentData);
				}
			}
				
			return comments;
			
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error loading comments");
		}
	}
	@Override
	@Transactional(readOnly = true)
	public List<CommentData> getCommentsWithReplies(CommentedResourceType resourceType, long resourceId, 
			boolean paginate, int maxResults, CommentSortData commentSortData, long userId, boolean loadOnlyCommentsFromUsersLearningSameDeliveries) throws DbConnectionException {
		try {
			List<Long> deliveries = null;
			if (loadOnlyCommentsFromUsersLearningSameDeliveries) {
				long compId = resourceType == CommentedResourceType.Competence
						? resourceId
						: actManager.getCompetenceIdForActivity(resourceId);
				deliveries = credManager.getIdsOfDeliveriesUserIsLearningContainingCompetence(userId, compId);
			}

			CommentSortField sortField = commentSortData.getSortField();
			SortingOption sortOption = commentSortData.getSortOption();
			String order = sortOption == SortingOption.DESC ? "DESC" : "ASC";
			
			StringBuilder query = new StringBuilder(
					"SELECT DISTINCT comment " +
					"FROM Comment1 comment " +
					"INNER JOIN comment.user user " +
					"LEFT JOIN fetch comment.childComments child " +
					"WHERE comment.resourceType = :resType " +
					"AND comment.commentedResourceId = :resourceId " +
					"AND comment.parentComment is NULL ");

			if (loadOnlyCommentsFromUsersLearningSameDeliveries) {
				if (!deliveries.isEmpty()) {
					query.append("AND (user.id = :userId OR EXISTS " +
					"(from TargetCredential1 cred WHERE cred.user.id = user.id AND cred.credential.id IN (:credentials))) ");
				} else {
					query.append("AND user.id = :userId ");
				}
			}
			
			if (paginate && commentSortData.getPreviousId() > 0) {
				String sign = sortOption == SortingOption.ASC ? ">=" : "<=";
				
				query.append("AND comment." + sortField.getSortField() + sign + " :previousFieldValue " +
						"AND NOT (comment." + sortField.getSortField() + " = :previousFieldValue AND comment.id >= :previousId) ");
			}
			
			query.append("ORDER BY comment." + sortField.getSortField() + " " + order +
					", comment.id DESC, child.postDate ASC");
			
			Query q = persistence.currentManager()
					.createQuery(query.toString())
					.setParameter("resType", resourceType)
					.setLong("resourceId", resourceId);

			if (loadOnlyCommentsFromUsersLearningSameDeliveries) {
				q.setLong("userId", userId);
				if (!deliveries.isEmpty()) {
					q.setParameterList("credentials", deliveries);
				}
			}
			
			if (paginate) {
				if (commentSortData.getPreviousId() > 0) {
					q.setLong("previousId", commentSortData.getPreviousId());
					
					if (sortField == CommentSortField.DATE_CREATED) {
						q.setTimestamp("previousFieldValue", commentSortData.getPreviousDate());
					} else if (sortField == CommentSortField.LIKE_COUNT) {
						q.setInteger("previousFieldValue", commentSortData.getPreviousLikeCount());
					}
				}
				q.setMaxResults(maxResults + 1);
			}
			
			List<CommentData> comments = new ArrayList<>();
			
			@SuppressWarnings("unchecked")
			List<Comment1> res = q.list();
			
			if(res == null) {
				return new ArrayList<>();
			}
			
			for (Comment1 comment : res) {
				CommentData commentData = getCommentData(comment, userId, null, 0);
				
				List<CommentData> childComments = new ArrayList<>();
				
				if (comment.getChildComments() != null) {
					for (Comment1 childComment : comment.getChildComments()) {
						CommentData child = getCommentData(childComment, userId, commentData, 0);
						childComments.add(child);
					}
					commentData.setChildComments(childComments);
					commentData.setNumberOfReplies(childComments.size());
				}
				comments.add(commentData);
			}
			return comments;
			
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error loading comments");
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
			throw new DbConnectionException("Error loading comments");
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
	//nt
	public void likeComment(long commentId, UserContextData context)
			throws DbConnectionException {
		try {
			Result<Void> res = self.likeCommentAndGetEvents(commentId, context);

			eventFactory.generateEvents(res.getEventQueue());
		} catch (DbConnectionException dbe) {
			logger.error(dbe);
			dbe.printStackTrace();
			throw dbe;
		}
	}
	
	@Override
	@Transactional (readOnly = false)
	public Result<Void> likeCommentAndGetEvents(long commentId, UserContextData context)
			throws DbConnectionException {
		try {
			Result<Void> result = new Result<>();
			annotationManager.createAnnotation(context.getActorId(), commentId, AnnotatedResource.Comment,
					AnnotationType.Like);

			String query = "UPDATE Comment1 comment " +
						   "SET comment.likeCount = comment.likeCount + 1 " +
						   "WHERE comment.id = :commentId";

			persistence.currentManager()
				.createQuery(query)
				.setLong("commentId", commentId)
				.executeUpdate();
			
			Comment1 comment = new Comment1();
			comment.setId(commentId);

			result.appendEvent(eventFactory.generateEventData(EventType.Like, context, comment, null, null, null));
			return result;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error  saving comment like");
		}
	}
	
	@Override
	@Transactional (readOnly = false)
	public void unlikeComment(long commentId, UserContextData context)
			throws DbConnectionException {
		try {
			annotationManager.deleteAnnotation(context.getActorId(), commentId, AnnotatedResource.Comment,
					AnnotationType.Like);
			String query = "UPDATE Comment1 comment " +
						   "SET comment.likeCount = comment.likeCount - 1 " +
						   "WHERE comment.id = :commentId";
			persistence.currentManager()
				.createQuery(query)
				.setLong("commentId", commentId)
				.executeUpdate();
			
			Comment1 comment = new Comment1();
			comment.setId(commentId);
			
			eventFactory.generateEvent(EventType.RemoveLike, context, comment, null, null, null);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error saving comment like");
		}
	}
	
//	@Override
//	@Transactional (readOnly = false)
//	public Comment1 saveNewCompetenceComment(CommentData data, long userId, 
//			PageContextData context) throws DbConnectionException {
//		try {
//			return saveNewComment(data, userId, CommentedResourceType.Competence, context);
//		} catch(Exception e) {
//			logger.error(e);
//			e.printStackTrace();
//			throw new DbConnectionException("Error saving comment");
//		}
//	}

	@Override
	//nt
	public Comment1 saveNewComment(CommentData data, CommentedResourceType resource,
			UserContextData context) throws DbConnectionException {
		try {
			Result<Comment1> res = self.saveNewCommentAndGetEvents(data, resource, context);

			eventFactory.generateEvents(res.getEventQueue());

			return res.getResult();
		} catch (DbConnectionException dbe) {
			logger.error(dbe);
			dbe.printStackTrace();
			throw dbe;
		}
	}

	@Override
	@Transactional(readOnly = false)
	public Result<Comment1> saveNewCommentAndGetEvents(CommentData data, CommentedResourceType resource,
													   UserContextData context) throws DbConnectionException {
		Result<Comment1> result = new Result<>();
		try {
			Comment1 comment = new Comment1();
			comment.setDescription(data.getComment());
			comment.setCommentedResourceId(data.getCommentedResourceId());
			comment.setResourceType(resource);
			comment.setInstructor(data.isInstructor());
			comment.setManagerComment(data.isManagerComment());
			//comment.setDateCreated(data.getDateCreated());
			comment.setPostDate(data.getDateCreated());
			User user = (User) persistence.currentManager().load(User.class, context.getActorId());
			comment.setUser(user);
			if(data.getParent() != null) {
				Comment1 parent = (Comment1) persistence.currentManager().load(Comment1.class,
						data.getParent().getCommentId());
				comment.setParentComment(parent);
			}

			saveEntity(comment);

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
				case ActivityResult:
					target = new TargetActivity1();
					break;
			}
			target.setId(data.getCommentedResourceId());

			EventType eventType = data.getParent() != null ? EventType.Comment_Reply : EventType.Comment;
			result.appendEvent(eventFactory.generateEventData(eventType, context, comment, target, null, null));

			result.setResult(comment);
			return result;
		} catch(DbConnectionException dce) {
			throw dce;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error saving activity");
		}
	}
	
	@Override
	@Transactional (readOnly = false)
	public void updateComment(CommentData data, UserContextData context)
			throws DbConnectionException {
		try {
			Comment1 comment = (Comment1) persistence.currentManager().load(Comment1.class, 
					data.getCommentId());
			comment.setDescription(data.getComment());
			
			eventFactory.generateEvent(EventType.Edit, context, comment, null,null, null);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error updating comment");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Long> getIdsOfUsersThatCommentedResource(CommentedResourceType resourceType, 
			long resourceId, Role role, List<Long> usersToExclude) 
					throws NullPointerException, DbConnectionException {
		try {
			if(role == null) {
				throw new NullPointerException("Role can not be null");
			}
			StringBuilder query = new StringBuilder(
					"SELECT distinct comment.user.id FROM Comment1 comment " +
					"WHERE comment.resourceType = :resType " +
					"AND comment.commentedResourceId = :resourceId " +
					"AND comment.managerComment = :managerComment ");
			if(usersToExclude != null && !usersToExclude.isEmpty()) {
					query.append("AND comment.user.id NOT IN (:usersToExclude)");
			}
			
			Query q = persistence.currentManager()
					.createQuery(query.toString())
					.setParameter("resType", resourceType)
					.setLong("resourceId", resourceId)
					.setBoolean("managerComment", role == Role.Manager ? true : false);
			if(usersToExclude != null && !usersToExclude.isEmpty()) {
				q.setParameterList("usersToExclude", usersToExclude);
			}		
					
			@SuppressWarnings("unchecked")
			List<Long> res =q.list();
			
			if(res == null) {
				return new ArrayList<>();
			}
	
			return res;
		
		} catch(NullPointerException npe) {
			throw npe;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error loading user ids");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public Long getCommentedResourceCreatorId(CommentedResourceType resourceType, long resourceId) 
			throws DbConnectionException {
		try {
			switch (resourceType) {
			case ActivityResult:
				String query = 
				"SELECT targetCompetence.user.id " + 
				"FROM TargetActivity1 targetActivity " +
				"INNER JOIN targetActivity.targetCompetence targetCompetence " +
				"WHERE targetActivity.id = :targetActivityId";
		
				Long id = (Long) persistence.currentManager()
						.createQuery(query)
						.setLong("targetActivityId", resourceId)
						.uniqueResult();
				return id;
			default:
				String creatorFieldName = getCreatorFieldNameForResourceType(resourceType);
				String query1 = 
						"SELECT res." + creatorFieldName + ".id " + 
						"FROM " + resourceType.getDbTableName() + " res " +
						"WHERE res.id = :resId";
				
				Long id1 = (Long) persistence.currentManager()
						.createQuery(query1)
						.setLong("resId", resourceId)
						.uniqueResult();
				return id1;
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error loading user id");
		}
	}

	private String getCreatorFieldNameForResourceType(CommentedResourceType resourceType) {
		switch(resourceType) {
			case Activity:
			case Competence:
				return "createdBy";
			case SocialActivity:
				return "actor";
			case ActivityResult:
				return "createdBy";
			default:
				return null;
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public Role getCommentedResourceCreatorRole(CommentedResourceType resourceType, long resourceId) 
			throws DbConnectionException {
		try {
			switch (resourceType) {
			case Competence:
				String query = 
					"SELECT comp.type " + 
					"FROM Competence1 comp " +
					"WHERE comp.id = :compId";
		
				LearningResourceType type = (LearningResourceType) persistence.currentManager()
						.createQuery(query)
						.setLong("compId", resourceId)
						.uniqueResult();
				return type == LearningResourceType.UNIVERSITY_CREATED ? Role.Manager : Role.User;
			case Activity:
				String query2 = 
					"SELECT act.type " + 
					"FROM Activity1 act " +
					"WHERE act.id = :actId";
		
				LearningResourceType actType = (LearningResourceType) persistence.currentManager()
						.createQuery(query2)
						.setLong("actId", resourceId)
						.uniqueResult();
				return actType == LearningResourceType.UNIVERSITY_CREATED ? Role.Manager : Role.User;
			default:
				return Role.User;
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error loading user role");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public long getCommentsNumber(CommentedResourceType resourceType, long resourceId) 
			throws DbConnectionException {
		try {
			String query = "SELECT COUNT(comment.id) FROM Comment1 comment " +
					   	   "WHERE comment.resourceType = :resType " +
					   	   "AND comment.commentedResourceId = :resourceId " +
					   	   "AND comment.parentComment is NULL ";
			
			Long res = (Long) persistence.currentManager()
					.createQuery(query)
					.setParameter("resType", resourceType)
					.setLong("resourceId", resourceId)
					.uniqueResult();
			
			if(res == null) {
				return 0;
			}
	
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error retrieving number of comments");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CommentData> getAllFirstLevelCommentsAndSiblingsOfSpecifiedComment(
			CommentedResourceType resourceType, long resourceId, CommentSortData commentSortData, 
			long commentId, long userId, boolean loadOnlyCommentsFromUsersLearningSameDeliveries) throws DbConnectionException {
		try {
			List<CommentData> comments = getAllComments(resourceType, resourceId, commentSortData, 
					CommentReplyFetchMode.FetchNumberOfReplies, userId, loadOnlyCommentsFromUsersLearningSameDeliveries);
			long parentCommentId = getParentCommentId(commentId, resourceType, resourceId);
	
			for(CommentData comment : comments) {
				if(comment.getCommentId() == parentCommentId) {
					comment.setChildComments(getAllCommentReplies(comment, commentSortData, userId));
					break;
				}
			}
			return comments;
			
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error loading comments");
		}
	}
	
	/**
	 * returns id of a parent comment and 0 if it is first level comment
	 * @param commentId
	 */
	private long getParentCommentId(long commentId, CommentedResourceType resourceType, long resourceId) {
		String query = "SELECT parent.id " +
					   "FROM Comment1 com " +
					   "LEFT JOIN com.parentComment parent " +
					   "WHERE com.id = :id " +
					   "AND com.resourceType = :resType " +
					   "AND com.commentedResourceId = :resourceId ";
		
		Long res = (Long) persistence.currentManager()
				.createQuery(query)
				.setLong("id", commentId)
				.setParameter("resType", resourceType)
				.setLong("resourceId", resourceId)
				.uniqueResult();
		
		if(res == null) {
			return 0;
		}
		return res.longValue();
	}
	
}
