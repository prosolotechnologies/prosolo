package org.prosolo.web.useractions;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.event.context.data.LearningContextData;
import org.prosolo.services.interaction.CommentManager;
import org.prosolo.services.interaction.data.CommentData;
import org.prosolo.services.interaction.data.CommentSortOption;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "commentBean")
@Component("commentBean")
@Scope("view")
public class CommentBean implements Serializable {

	private static final long serialVersionUID = -2948282414910224988L;

	private static Logger logger = Logger.getLogger(CommentBean.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private CommentManager commentManager;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;

	private String topLevelComment;
	
	private boolean isInstructor;
	private List<CommentData> comments;
	private CommentedResourceType resourceType;
	private long resourceId;
	private CommentSortOption sortOption = CommentSortOption.MOST_RECENT;
	
	private CommentSortOption[] sortOptions;
	
	private long newestCommentId;
	
	public void init(CommentedResourceType resourceType, long resourceId, boolean isInstructor) {
		try {
			sortOptions = CommentSortOption.values();
			this.resourceType = resourceType;
			this.resourceId = resourceId;
			this.isInstructor = isInstructor;
			loadComments();
			logger.info("Comments for resource " + resourceType.toString() + 
					" with id " + resourceId + " is loaded");
		} catch(DbConnectionException e) {
			logger.error(e);
		}
	}

	private void loadComments() {
		this.comments = commentManager.getAllComments(resourceType, resourceId, 
				sortOption.getSortField(), sortOption.getSortOption(), loggedUser.getUser().getId());
	}
	
	public void sortChanged(CommentSortOption sortOption) {
		if(sortOption != this.sortOption) {
			this.sortOption = sortOption;
			loadComments();
		}
	}
	
	public void saveTopLevelComment() {
		saveNewComment(null);
	}
	
	public void saveNewComment(CommentData parent) {
		CommentData newComment = new CommentData();
		CommentData realParent = null;
		if(parent == null) {
			newComment.setComment(topLevelComment);
			topLevelComment = null;
		} else {
			/*
			 * if parent comment has parent, then that comment will be the
			 * parent of a new comment because we have only one level of replies
			 */
			realParent = parent.getParent() != null ? parent.getParent() : 
				parent;
			newComment.setParent(realParent);
			newComment.setComment(parent.getReplyToComment());
		}
		newComment.setCommentedResourceId(resourceId);
		newComment.setDateCreated(new Date());
		UserData creator = new UserData(loggedUser.getUser());
		newComment.setCreator(creator);
		newComment.setInstructor(isInstructor);
		
		String page = PageUtil.getPostParameter("page");
		String lContext = PageUtil.getPostParameter("learningContext");
		String service = PageUtil.getPostParameter("service");
			
		try {
    		LearningContextData context = new LearningContextData(page, lContext, service);
    		Comment1 comment = commentManager.saveNewComment(newComment, loggedUser.getUser().getId(), 
    				resourceType, context);
        	
        	newComment.setCommentId(comment.getId());
        	newestCommentId = newComment.getCommentId();
        	
			if (parent != null) {
				realParent.getChildComments().add(newComment);
			} else {
	        	/* 
	        	 * if selected sort option is newest first, add element to the
	        	 * end of a list, otherwise add it to the beginning
	        	 */
	        	if(sortOption == CommentSortOption.MOST_RECENT) {
	        		comments.add(newComment);
	        	} else {
	        		comments.add(0, newComment);
	        	}
        	}
        	PageUtil.fireSuccessfulInfoMessage("Comment posted");
    	} catch (DbConnectionException e) {
    		logger.error(e);
    		PageUtil.fireErrorMessage("Error while adding new comment");
    	}
		
//		taskExecutor.execute(new Runnable() {
//            @Override
//            public void run() {	
//            	try {
//            		LearningContextData context = new LearningContextData(page, lContext, service);
//            		Comment1 comment = commentManager.saveNewComment(editComment, loggedUser.getUser().getId(), 
//            				resourceType, context);
//	            	
//	            	editComment.setCommentId(comment.getId());
//	            	
//            	} catch (DbConnectionException e) {
//            		logger.error(e);
//            	}
//            }
//        });
	}
	
	public void editComment(CommentData comment) {
		String page = PageUtil.getPostParameter("page");
		String lContext = PageUtil.getPostParameter("learningContext");
		String service = PageUtil.getPostParameter("service");
		taskExecutor.execute(new Runnable() {
            @Override
            public void run() {	
            	try {
            		LearningContextData context = new LearningContextData(page, lContext, service);
            		commentManager.updateComment(comment, loggedUser.getUser().getId(), context);
            	} catch (DbConnectionException e) {
            		logger.error(e);
            	}
            }
        });
	}
	
	public void likeAction(CommentData data) {
		String page = PageUtil.getPostParameter("page");
		String lContext = PageUtil.getPostParameter("learningContext");
		String service = PageUtil.getPostParameter("service");
		
		//we can trade off accuracy for performance here
		boolean liked = !data.isLikedByCurrentUser();
		data.setLikedByCurrentUser(liked);
		if(liked) {
			data.setLikeCount(data.getLikeCount() + 1);
		} else {
			data.setLikeCount(data.getLikeCount() - 1);
		}
		
		taskExecutor.execute(new Runnable() {
            @Override
            public void run() {	
            	try {
            		LearningContextData context = new LearningContextData(page, lContext, service);
            		if(liked) {
	            		commentManager.likeComment(loggedUser.getUser().getId(), data.getCommentId(), 
	            				context);
            		} else {
            			commentManager.unlikeComment(loggedUser.getUser().getId(), data.getCommentId(), 
	            				context);
            		}
	            	
            	} catch (DbConnectionException e) {
            		logger.error(e);
            	}
            }
        });
	}
	
	public boolean isCurrentUserCommentCreator(CommentData comment) {
		return loggedUser.getUser().getId() == comment.getCreator().getId();
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

	public CommentSortOption[] getSortOptions() {
		return sortOptions;
	}

	public void setSortOptions(CommentSortOption[] sortOptions) {
		this.sortOptions = sortOptions;
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
	
}
