package org.prosolo.services.interaction;

import java.util.List;

import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.services.event.context.data.LearningContextData;
import org.prosolo.services.interaction.data.CommentData;
import org.prosolo.services.interaction.data.CommentSortField;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.util.SortingOption;

public interface CommentManager {

	/**
	 * Returns all comments for a resource
	 * @param resourceType
	 * @param resourceId
	 * @param sortField
	 * @param sortOption
	 * @param userId
	 * @return
	 * @throws DbConnectionException
	 */
	List<CommentData> getAllComments(CommentedResourceType resourceType, long resourceId, 
			CommentSortField sortField, SortingOption sortOption, long userId) 
					throws DbConnectionException;
	/**
	 * Returns number of comments specified by {@code maxResults} with all replies
	 * to those comments
	 * @param resourceType
	 * @param resourceId
	 * @param paginate
	 * @param offset
	 * @param maxResults
	 * @param sortField
	 * @param sortOption
	 * @param userId
	 * @return
	 * @throws DbConnectionException
	 */
	List<CommentData> getComments(CommentedResourceType resourceType, long resourceId, 
			boolean paginate, int offset, int maxResults, CommentSortField sortField, 
			SortingOption sortOption, long userId) throws DbConnectionException;
	
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