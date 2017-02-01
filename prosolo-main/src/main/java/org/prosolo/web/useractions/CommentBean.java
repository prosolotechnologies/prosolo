package org.prosolo.web.useractions;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.services.activityWall.SocialActivityManager;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.interaction.CommentManager;
import org.prosolo.services.interaction.data.CommentData;
import org.prosolo.services.interaction.data.CommentReplyFetchMode;
import org.prosolo.services.interaction.data.CommentSortData;
import org.prosolo.services.interaction.data.CommentSortOption;
import org.prosolo.services.interaction.data.CommentsData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.useractions.util.ICommentBean;
import org.prosolo.web.util.HTMLUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

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
						commentsData.getCommentId(), loggedUser.getUserId());
				commentsData.setNumberOfComments(comments.size());
			} else {
				comments = commentManager.getComments(commentsData.getResourceType(), 
						commentsData.getResourceId(), true, limit, csd, 
						CommentReplyFetchMode.FetchNumberOfReplies, loggedUser.getUserId());
				
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
			logger.error(e);
			return null;
		}
	}
	
	public static CommentSortData getCommentSortData(CommentsData commentsData) {
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
		CommentData newComment = new CommentData();
		CommentData realParent = null;
		if(parent == null) {
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
		
		// strip all tags except <br>
		newComment.setComment(HTMLUtil.cleanHTMLTagsExceptBrA(newComment.getComment()));
		
		UserData creator = new UserData(
				loggedUser.getUserId(), 
				loggedUser.getSessionData().getName(),
				loggedUser.getSessionData().getLastName(),
				loggedUser.getSessionData().getAvatar(),
				loggedUser.getSessionData().getPosition(),
				loggedUser.getSessionData().getEmail(),
				true);
		
		newComment.setCreator(creator);
		newComment.setInstructor(commentsData.isInstructor());
		
		String page = PageUtil.getPostParameter("page");
		String lContext = PageUtil.getPostParameter("learningContext");
		String service = PageUtil.getPostParameter("service");
			
		try {
    		LearningContextData context = new LearningContextData(page, lContext, service);
    		Comment1 comment = null;
    		if(commentsData.getResourceType() == CommentedResourceType.SocialActivity) {
    			comment = socialActivityManager.saveSocialActivityComment(
    					commentsData.getResourceId(), newComment, loggedUser.getUserId(), 
    					commentsData.getResourceType(), context);
    		} else {
    			comment = commentManager.saveNewComment(newComment, loggedUser.getUserId(), 
        				commentsData.getResourceType(), context);
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
	
	public void editComment(CommentData comment, CommentsData commentsData) {
		String page = PageUtil.getPostParameter("page");
		String lContext = PageUtil.getPostParameter("learningContext");
		String service = PageUtil.getPostParameter("service");
		taskExecutor.execute(new Runnable() {
            @Override
            public void run() {	
            	try {
            		LearningContextData context = new LearningContextData(page, lContext, service);
            		if(commentsData.getResourceType() == CommentedResourceType.SocialActivity) {
            			socialActivityManager.updateSocialActivityComment(commentsData.getResourceId(), 
            					comment, loggedUser.getUserId(), context);
            		} else {
            			commentManager.updateComment(comment, loggedUser.getUserId(), context);
            		}
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
	            		commentManager.likeComment(loggedUser.getUserId(), data.getCommentId(), 
	            				context);
            		} else {
            			commentManager.unlikeComment(loggedUser.getUserId(), data.getCommentId(), 
	            				context);
            		}
	            	
            	} catch (DbConnectionException e) {
            		logger.error(e);
            	}
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
