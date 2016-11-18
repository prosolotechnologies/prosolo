package org.prosolo.services.interaction.data;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.web.useractions.CommentBean;

public class CommentsData implements Serializable {

	private static final long serialVersionUID = -4537657862393549529L;

	private static Logger logger = Logger.getLogger(CommentBean.class);
	
	private String topLevelComment;
	
	private boolean initialized;
	private boolean isInstructor;
	private List<CommentData> comments;
	private CommentedResourceType resourceType;
	private long resourceId;
	private CommentSortOption sortOption = CommentSortOption.MOST_RECENT;
	private boolean moreToLoad;
	
	private long newestCommentId;
	
	private int numberOfComments;
	private long commentId;
	
	public CommentsData(CommentedResourceType resourceType, long resourceId) {
		this.resourceType = resourceType;
		this.resourceId = resourceId;
		logger.info("Comment options for resource " + resourceType.toString() + 
				" with id " + resourceId + " is created");
	}
	
	public CommentsData(CommentedResourceType resourceType, long resourceId, boolean isInstructor) {
		this.resourceType = resourceType;
		this.resourceId = resourceId;
		this.isInstructor = isInstructor;
		logger.info("Comment options for resource " + resourceType.toString() + 
				" with id " + resourceId + " is created");
	}
	
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
	
	public String getTopLevelComment() {
		return topLevelComment;
	}

	public void setTopLevelComment(String topLevelComment) {
		this.topLevelComment = topLevelComment;
	}

	public List<CommentData> getComments() {
		return comments;
	}

	public void setComments(List<CommentData> comments) {
		this.comments = comments;
	}

	public CommentSortOption getSortOption() {
		return sortOption;
	}

	public void setSortOption(CommentSortOption sortOption) {
		this.sortOption = sortOption;
	}

	public long getNewestCommentId() {
		return newestCommentId;
	}

	public void setNewestCommentId(long newestCommentId) {
		this.newestCommentId = newestCommentId;
	}

	public boolean isInstructor() {
		return isInstructor;
	}

	public void setInstructor(boolean isInstructor) {
		this.isInstructor = isInstructor;
	}

	public CommentedResourceType getResourceType() {
		return resourceType;
	}

	public void setResourceType(CommentedResourceType resourceType) {
		this.resourceType = resourceType;
	}

	public long getResourceId() {
		return resourceId;
	}

	public void setResourceId(long resourceId) {
		this.resourceId = resourceId;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public boolean isMoreToLoad() {
		return moreToLoad;
	}

	public void setMoreToLoad(boolean moreToLoad) {
		this.moreToLoad = moreToLoad;
	}

	public int getNumberOfComments() {
		return numberOfComments;
	}

	public void setNumberOfComments(int numberOfComments) {
		this.numberOfComments = numberOfComments;
	}

	public long getCommentId() {
		return commentId;
	}

	public void setCommentId(long commentId) {
		this.commentId = commentId;
	}
	
}
