package org.prosolo.web.useractions;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.activityWall.SocialActivityManager;
import org.prosolo.services.interaction.CommentManager;
import org.prosolo.services.interaction.data.*;
import org.prosolo.services.interaction.data.factory.CommentDataFactory;
import org.prosolo.services.user.data.UserData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.useractions.util.ICommentBean;
import org.prosolo.web.util.HTMLUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@ManagedBean(name = "commentBean")
@Component("commentBean")
@Scope("view")
public class CommentBean implements Serializable, ICommentBean {

	private static final long serialVersionUID = -2948282414910224988L;

	private static Logger logger = Logger.getLogger(CommentBean.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private CommentManager commentManager;
	@Inject private SocialActivityManager socialActivityManager;
	@Inject @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	@Inject private CommentDataFactory commentDataFactory;

	private CommentSortOption[] sortOptions;
	private int limit = 2;
	
	@PostConstruct
	public void init() {
		sortOptions = CommentSortOption.values();
	}
	
	public void loadComments(CommentsData commentsData) {
		try {
			List<CommentData> comments = retrieveComments(commentsData);
			commentsData.setComments(comments);
			commentsData.setInitialized(true);
		} catch(DbConnectionException e) {
			logger.error(e);
		}
	}
	
	private List<CommentData> retrieveComments(CommentsData commentsData) {
		try {
			CommentSortData csd = getCommentSortData(commentsData);
			List<CommentData> comments = null;

			if(commentsData.getCommentId() > 0) {
				comments = commentManager.getAllFirstLevelCommentsAndSiblingsOfSpecifiedComment(
						commentsData.getResourceType(), commentsData.getResourceId(), csd, 
						commentsData.getCommentId(), loggedUser.getUserId(), commentsData.getCredentialId());
				commentsData.setNumberOfComments(comments.size());
			} else {
				comments = commentManager.getComments(commentsData.getResourceType(), 
						commentsData.getResourceId(), true, limit, csd, 
						CommentReplyFetchMode.FetchNumberOfReplies, loggedUser.getUserId(), commentsData.getCredentialId());
				
				int commentsNumber = comments.size();
				if(commentsNumber == limit + 1) {
					commentsData.setMoreToLoad(true);
					comments.remove(commentsNumber - 1);
				} else {
					commentsData.setMoreToLoad(false);
				}
			}
			Collections.reverse(comments);
			return comments;
		} catch(Exception e) {
			logger.error("error", e);
			return null;
		}
	}
	
	public CommentSortData getCommentSortData(CommentsData commentsData) {
		return commentDataFactory.getCommentSortData(commentsData);
	}

	public void loadMoreComments(CommentsData commentsData) {
		if(commentsData.isMoreToLoad()) {
			List<CommentData> comments = retrieveComments(commentsData);
			if(comments != null) {
				comments.addAll(commentsData.getComments());
			}
			commentsData.setComments(comments);
		}
	}
	
	public void loadRepliesIfNotLoaded(CommentsData commentsData, CommentData comment) {
		if(comment.getNumberOfReplies() > 0 && (comment.getChildComments() == null
				|| comment.getChildComments().isEmpty())) {
			loadReplies(commentsData, comment);
		}
	}
	
	private void loadReplies(CommentsData commentsData, CommentData comment) {
		try {
			List<CommentData> replies = commentManager.getAllCommentReplies(comment, 
					getCommentSortData(commentsData), loggedUser.getUserId());
			Collections.reverse(replies);
			comment.setChildComments(replies);
		} catch(Exception e) {
			logger.error(e);
		}
	}
	
	public void sortChanged(CommentSortOption sortOption, CommentsData commentsData) {
		if(sortOption != commentsData.getSortOption()) {
			commentsData.setSortOption(sortOption);
			commentsData.setComments(null);
			loadComments(commentsData);
		}
	}

	@Override
	public void saveTopLevelComment(CommentsData commentsData) {
		saveNewComment(null, commentsData);
	}
	
	@Override
	public void saveNewComment(CommentData parent, CommentsData commentsData) {
		try {
			CommentData newComment = new CommentData();
			CommentData realParent = null;

			if (parent == null) {
				newComment.setComment(commentsData.getTopLevelComment());
				commentsData.setTopLevelComment(null);
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

			newComment.setCommentedResourceId(commentsData.getResourceId());
			newComment.setDateCreated(new Date());

			UserData creator = new UserData(
					loggedUser.getUserId(),
					loggedUser.getName(),
					loggedUser.getLastName(),
					loggedUser.getAvatar(),
					loggedUser.getPosition(),
					loggedUser.getEmail(),
					true);

			newComment.setCreator(creator);
			newComment.setInstructor(commentsData.isInstructor());
			newComment.setManagerComment(commentsData.isManagerComment());
			newComment.setCredentialId(commentsData.getCredentialId());
			
    		Comment1 comment = null;
			if (commentsData.getResourceType() == CommentedResourceType.SocialActivity) {
				comment = socialActivityManager.saveSocialActivityComment(
						commentsData.getResourceId(), newComment,
						commentsData.getResourceType(), loggedUser.getUserContext());
			} else {
				comment = commentManager.saveNewComment(newComment, commentsData.getResourceType(),
						loggedUser.getUserContext());
			}
    		
        	newComment.setCommentId(comment.getId());
        	commentsData.setNewestCommentId(newComment.getCommentId());
        	
			if (parent != null) {
				loadReplies(commentsData, realParent);
				//realParent.getChildComments().add(newComment);
				//realParent.incrementNumberOfReplies();
			} else {
	        	/* 
	        	 * if selected sort option is newest first, add element to the
	        	 * end of a list, otherwise add it to the beginning
	        	 */
//	        	if(commentsData.getSortOption() == CommentSortOption.MOST_RECENT) {
//	        		commentsData.addComment(newComment);
//	        	} else {
//	        		commentsData.addComment(0, newComment);
//	        	}
				commentsData.addComment(newComment);
				commentsData.incrementNumberOfComments();
        	}

			if (parent.getParent() != null) {
				parent.getParent().setReplyToComment(null);
			} else {
				parent.setReplyToComment(null);
			}
        	PageUtil.fireSuccessfulInfoMessage("Your comment is posted");
    	} catch (Exception e) {
    		logger.error(e);
    		PageUtil.fireErrorMessage("Error posting a comment");
    	}
		
//		taskExecutor.execute(new Runnable() {
//            @Override
//            public void run() {	
//            	try {
//            		PageContextData context = new PageContextData(page, lContext, service);
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
	
	public void editComment(CommentData comment, CommentsData allCommentsData) {
		try {
			if (allCommentsData.getResourceType() == CommentedResourceType.SocialActivity) {
				socialActivityManager.updateSocialActivityComment(
						allCommentsData.getResourceId(), comment, loggedUser.getUserContext());
			} else {
				commentManager.updateComment(comment, loggedUser.getUserContext());
			}
		} catch (DbConnectionException e) {
			logger.error("Error", e);
		}
	}
	
	public void likeAction(CommentData data) {
		UserContextData context = loggedUser.getUserContext();
		
		//we can trade off accuracy for performance here
		boolean liked = !data.isLikedByCurrentUser();
		data.setLikedByCurrentUser(liked);
		if(liked) {
			data.setLikeCount(data.getLikeCount() + 1);
		} else {
			data.setLikeCount(data.getLikeCount() - 1);
		}
		
		taskExecutor.execute(() -> {
			try {
				if(liked) {
					commentManager.likeComment(data.getCommentId(), context);
				} else {
					commentManager.unlikeComment(data.getCommentId(), context);
				}

			} catch (DbConnectionException e) {
				logger.error(e);
			}
        });
	}
	
	public boolean isCurrentUserCommentCreator(CommentData comment) {
		return loggedUser.getUserId() == comment.getCreator().getId();
	}

	public CommentSortOption[] getSortOptions() {
		return sortOptions;
	}

	public void setSortOptions(CommentSortOption[] sortOptions) {
		this.sortOptions = sortOptions;
	}

}
