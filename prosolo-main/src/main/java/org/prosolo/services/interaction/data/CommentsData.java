package org.prosolo.services.interaction.data;

import java.io.Serializable;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.web.useractions.CommentBean;

@Builder
@Getter
@Setter
public class CommentsData implements Serializable {

	private static final long serialVersionUID = -4537657862393549529L;

	private static Logger logger = Logger.getLogger(CommentBean.class);
	
	private String topLevelComment;
	
	private boolean initialized;
	private boolean isInstructor;
	private boolean isManagerComment;
	private List<CommentData> comments;
	private CommentedResourceType resourceType;
	private long resourceId;
	@Builder.Default
	private CommentSortOption sortOption = CommentSortOption.MOST_RECENT;
	private boolean moreToLoad;
	
	private long newestCommentId;
	
	private int numberOfComments;
	private long commentId;

	private long credentialId;
	
//	public CommentsData(CommentedResourceType resourceType, long resourceId) {
//		this.resourceType = resourceType;
//		this.resourceId = resourceId;
//		logger.info("Comment options for resource " + resourceType.toString() +
//				" with id " + resourceId + " is created");
//	}
//
//	public CommentsData(CommentedResourceType resourceType, long resourceId, boolean isInstructor,
//			boolean isManagerComment) {
//		this.resourceType = resourceType;
//		this.resourceId = resourceId;
//		this.isInstructor = isInstructor;
//		this.isManagerComment = isManagerComment;
//		logger.info("Comment options for resource " + resourceType.toString() +
//				" with id " + resourceId + " is created");
//	}
	
	public void incrementNumberOfComments() {
		numberOfComments ++;
	}
	
	public void addComment(CommentData comment) {
		comments.add(comment);
	}
	
	public void addComment(int index, CommentData comment) {
		comments.add(index, comment);
	}
	
	public void addAllCommentsToTheEnd(List<CommentData> comments) {
		comments.addAll(comments);
	}
	
	public void addAllCommentsToTheBeginning(List<CommentData> comments) {
		comments.addAll(0, comments);
	}
	
}
