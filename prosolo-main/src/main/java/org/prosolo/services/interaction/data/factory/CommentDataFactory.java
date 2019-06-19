package org.prosolo.services.interaction.data.factory;

import java.util.Date;
import java.util.List;

import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.services.interaction.data.CommentData;
import org.prosolo.services.interaction.data.CommentSortData;
import org.prosolo.services.interaction.data.CommentsData;
import org.prosolo.services.user.data.UserData;
import org.springframework.stereotype.Component;

@Component
public class CommentDataFactory {

	/**
	 * if comment does not have parent comment, just pass 0 for {@code parentCommentId}
	 * @param comment
	 * @param likedByCurrentUser
	 * @return
	 */
	public CommentData getCommentData(Comment1 comment, boolean likedByCurrentUser, CommentData parent,
			int numberOfReplies) {
		if(comment == null) {
			return null;
		}
		CommentData cd = new CommentData();
		cd.setCommentId(comment.getId());
		if(parent != null) {
			cd.setParent(parent);
		}
		cd.setComment(comment.getDescription());
		cd.setCreator(new UserData(comment.getUser()));
		cd.setInstructor(comment.isInstructor());
		cd.setLikeCount(comment.getLikeCount());
		cd.setLikedByCurrentUser(likedByCurrentUser);
		cd.setCommentedResourceId(comment.getCommentedResourceId());
		cd.setCommentedResourceType(comment.getResourceType());
		cd.setDateCreated(comment.getPostDate());
		cd.setNumberOfReplies(numberOfReplies);
		return cd;
	}
	
	public CommentData getCommentData(long id, String description, UserData user, boolean isInstructor, int likeCount,
									  boolean likedByCurrentUser, long commentedResourceId,
									  CommentedResourceType commentedResourceType, Date dateCreated, CommentData parent,
									  int numberOfReplies) {
		CommentData cd = new CommentData();
		cd.setCommentId(id);
		if(parent != null) {
			cd.setParent(parent);
		}
		cd.setComment(description);
		cd.setCreator(user);
		cd.setInstructor(isInstructor);
		cd.setLikeCount(likeCount);
		cd.setLikedByCurrentUser(likedByCurrentUser);
		cd.setCommentedResourceId(commentedResourceId);
		cd.setCommentedResourceType(commentedResourceType);
		cd.setDateCreated(dateCreated);
		cd.setNumberOfReplies(numberOfReplies);
		return cd;
	}
	
	public CommentSortData getCommentSortData(CommentsData commentsData) {
		List<CommentData> comms = commentsData.getComments();
		Date previousDate = null;
		int previousLikeCount = 0;
		long previousId = 0;
		if (comms != null && !comms.isEmpty()) {
			CommentData comment = comms.get(0);
			previousDate = comment.getDateCreated();
			previousLikeCount = comment.getLikeCount();
			previousId = comment.getCommentId();
		}
		return new CommentSortData(commentsData.getSortOption().getSortField(), 
				commentsData.getSortOption().getSortOption(), previousDate, previousLikeCount,
				previousId);
	}

}
