package org.prosolo.services.interaction;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.data.Result;
import org.prosolo.services.interaction.data.CommentData;
import org.prosolo.services.interaction.data.CommentReplyFetchMode;
import org.prosolo.services.interaction.data.CommentSortData;
import org.prosolo.services.nodes.data.Role;

import java.util.List;

public interface CommentManager {

	/**
	 * Returns all comments for a resource
	 * @param resourceType
	 * @param resourceId
	 * @param commentSortData
	 * @param userId
	 * @param loadOnlyCommentsFromUsersLearningSameDeliveries
	 * @return
	 * @throws DbConnectionException
	 */
	List<CommentData> getAllComments(CommentedResourceType resourceType, long resourceId, 
			CommentSortData commentSortData, long userId, boolean loadOnlyCommentsFromUsersLearningSameDeliveries) throws DbConnectionException;
	/**
	 * Returns number of comments specified by {@code maxResults} 
	 * @param resourceType
	 * @param resourceId
	 * @param paginate
	 * @param maxResults
	 * @param commentSortData
	 * @param replyFetchMode
	 * @param userId
	 * @param loadOnlyCommentsFromUsersLearningSameDeliveries
	 * @return
	 * @throws DbConnectionException
	 */
	List<CommentData> getComments(CommentedResourceType resourceType, long resourceId, 
			boolean paginate, int maxResults, CommentSortData commentSortData, 
			CommentReplyFetchMode replyFetchMode, long userId, boolean loadOnlyCommentsFromUsersLearningSameDeliveries) throws DbConnectionException;
	
	List<CommentData> getCommentsWithNumberOfReplies(CommentedResourceType resourceType, long resourceId,
			boolean paginate, int maxResults, CommentSortData commentSortData, long userId, boolean loadOnlyCommentsFromUsersLearningSameDeliveries)
			throws DbConnectionException;
	
	List<CommentData> getCommentsWithReplies(CommentedResourceType resourceType, long resourceId, boolean paginate,
			int maxResults, CommentSortData commentSortData, long userId, boolean loadOnlyCommentsFromUsersLearningSameDeliveries) throws DbConnectionException;
	
	List<CommentData> getAllCommentReplies(CommentData parent, CommentSortData commentSortData, 
			long userId) throws DbConnectionException;
	
	void likeComment(long commentId, UserContextData context)
			throws DbConnectionException;

	Result<Void> likeCommentAndGetEvents(long commentId, UserContextData context)
			throws DbConnectionException;
	
	void unlikeComment(long commentId, UserContextData context)
			throws DbConnectionException;
	
	Comment1 saveNewComment(CommentData data, CommentedResourceType resource,
			UserContextData context) throws DbConnectionException;

	Result<Comment1> saveNewCommentAndGetEvents(CommentData data, CommentedResourceType resource,
												UserContextData context) throws DbConnectionException;
	
	void updateComment(CommentData data, UserContextData context)
			throws DbConnectionException;
	
	/**
	 * 
	 * @param resourceType
	 * @param resourceId
	 * @param role
	 * @param usersToExclude
	 * @return
	 * @throws NullPointerException if role is null
	 * @throws DbConnectionException
	 */
	List<Long> getIdsOfUsersThatCommentedResource(CommentedResourceType resourceType, long resourceId, 
			Role role, List<Long> usersToExclude) throws NullPointerException, DbConnectionException;
	
	Long getCommentedResourceCreatorId(CommentedResourceType resourceType, long resourceId) 
			throws DbConnectionException;
	
	long getCommentsNumber(CommentedResourceType resourceType, long resourceId) 
			throws DbConnectionException;
	
	List<CommentData> getAllFirstLevelCommentsAndSiblingsOfSpecifiedComment(
			CommentedResourceType resourceType, long resourceId, CommentSortData commentSortData, 
			long commentId, long userId, boolean loadOnlyCommentsFromUsersLearningSameDeliveries) throws DbConnectionException;
	
	Role getCommentedResourceCreatorRole(CommentedResourceType resourceType, long resourceId) 
			throws DbConnectionException;

}