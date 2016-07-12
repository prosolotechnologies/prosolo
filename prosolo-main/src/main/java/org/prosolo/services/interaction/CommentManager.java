package org.prosolo.services.interaction;

import java.util.List;

import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.event.context.data.LearningContextData;
import org.prosolo.services.interaction.data.CommentData;
import org.prosolo.services.interaction.data.CommentReplyFetchMode;
import org.prosolo.services.interaction.data.CommentSortData;

public interface CommentManager {

	/**
	 * Returns all comments for a resource
	 * @param resourceType
	 * @param resourceId
	 * @param commentSortData
	 * @param userId
	 * @return
	 * @throws DbConnectionException
	 */
	List<CommentData> getAllComments(CommentedResourceType resourceType, long resourceId, 
			CommentSortData commentSortData, long userId) throws DbConnectionException;
	/**
	 * Returns number of comments specified by {@code maxResults} 
	 * @param resourceType
	 * @param resourceId
	 * @param paginate
	 * @param maxResults
	 * @param commentSortData
	 * @param replyFetchMode
	 * @param userId
	 * @return
	 * @throws DbConnectionException
	 */
	List<CommentData> getComments(CommentedResourceType resourceType, long resourceId, 
			boolean paginate, int maxResults, CommentSortData commentSortData, 
			CommentReplyFetchMode replyFetchMode, long userId) throws DbConnectionException;
	
	 List<CommentData> getAllCommentReplies(CommentData parent, CommentSortData commentSortData, 
				long userId) throws DbConnectionException;
	
	void likeComment(long userId, long commentId, LearningContextData context) 
			throws DbConnectionException;
	
	void unlikeComment(long userId, long commentId, LearningContextData context) 
			throws DbConnectionException;
	
	Comment1 saveNewComment(CommentData data, long userId, CommentedResourceType resource,
			LearningContextData context) throws DbConnectionException;
	
	void updateComment(CommentData data, long userId, LearningContextData context) 
			throws DbConnectionException;
	
	List<Long> getIdsOfUsersThatCommentedResource(CommentedResourceType resourceType, long resourceId, 
			List<Long> usersToExclude) throws DbConnectionException;
	
	Long getCommentedResourceCreatorId(CommentedResourceType resourceType, long resourceId) 
			throws DbConnectionException;

}