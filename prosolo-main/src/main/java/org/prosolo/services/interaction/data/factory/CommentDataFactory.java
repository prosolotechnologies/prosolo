package org.prosolo.services.interaction.data.factory;

import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.services.interaction.data.CommentData;
import org.prosolo.services.nodes.data.UserData;
import org.springframework.stereotype.Component;

@Component
public class CommentDataFactory {

	/**
	 * if comment does not have parent comment, just pass 0 for {@code parentCommentId}
	 * @param comment
	 * @param likedByCurrentUser
	 * @param parentCommentId
	 * @return
	 */
	public CommentData getCommentData(Comment1 comment, boolean likedByCurrentUser, long parentCommentId) {
		if(comment == null) {
			return null;
		}
		CommentData cd = new CommentData();
		cd.setCommentId(comment.getId());
		if(parentCommentId > 0) {
			cd.setParentCommentId(parentCommentId);
		}
		cd.setComment(comment.getDescription());
		cd.setCreator(new UserData(comment.getUser()));
		cd.setInstructor(comment.isInstructor());
		cd.setLikeCount(comment.getLikeCount());
		cd.setLikedByCurrentUser(likedByCurrentUser);
		cd.setCommentedResourceId(comment.getCommentedResourceId());
		cd.setDateCreated(comment.getPostDate());
		return cd;
	}

}
