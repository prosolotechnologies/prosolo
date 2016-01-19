package org.prosolo.web.useractions;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.comments.Comment;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.activityWall.SocialActivityFactory;
import org.prosolo.services.activityWall.impl.data.SocialActivityData;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.interaction.CommentingManager;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.activitywall.data.SocialActivityCommentData;
import org.prosolo.web.useractions.data.NewCommentData;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author Zoran Jeremic
 * @date Jun 17, 2012
 */
@ManagedBean(name = "commentaction")
@Component("commentaction")
@Scope("view")
public class CommentActionBean implements Serializable {
	
	private static final long serialVersionUID = -4386479460010699841L;

	private static Logger logger = Logger.getLogger(CommentActionBean.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private CommentingManager commentManager;
	@Autowired private DefaultManager defaultManager;
	@Autowired private EventFactory eventFactory;
	@Autowired private SocialActivityFactory socialActivityFactory;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;

	private NewCommentData newCommentData = new NewCommentData();
	
	public void init() {
		newCommentData = new NewCommentData();
	}

	public NewCommentData getNewCommentData() {
		return newCommentData;
	}

	public void newComment(final ActivityWallData wallData, final String context) {
		if (wallData != null) {
//			final String commentText = StringUtil.cleanHtml(wallData.getNewComment());
			final String commentText = wallData.getNewComment();
			
			final Date created = new Date();
			final SocialActivityCommentData commentData = new SocialActivityCommentData(commentText, loggedUser.getUser(), created, wallData);
			wallData.addComment(commentData);
			wallData.setShowHiddenComments(true);
			
			taskExecutor.execute(new Runnable() {
	            @Override
	            public void run() {
	            	Session session = (Session) defaultManager.getPersistence().openSession();
	            		
	            	try {
		            	long targetActiviryId = wallData.getId();
						
		            	TargetActivity resource = defaultManager.loadResource(TargetActivity.class, targetActiviryId, session);

						Comment comment = commentManager.addComment(
								resource.getActivity(), 
								loggedUser.getUser(), 
								commentText, 
								created,
								context,
								session);
						
						session.flush();
						
						Map<String, String> parameters = new HashMap<String, String>();
						parameters.put("context", context);
						session.flush();
						
						Event event = eventFactory.generateEvent(EventType.Comment, loggedUser.getUser(), comment, resource, parameters);
						
						if (event != null) {
							socialActivityFactory.createSocialActivity(event, session, null);
						}

		            	logger.debug("User \"" + loggedUser.getUser() +
		            			" commented on an resource "+targetActiviryId+")");
		            	
		            	commentData.setId(comment.getId());
		            	
//		            	update of other registered users' walls is performed in InterfaceCacheUpdater
	            	} catch (EventException e) {
	            		logger.error(e);
	            	} catch (ResourceCouldNotBeLoadedException e) {
	            		logger.error(e);
					} finally {
						HibernateUtil.close(session);
					}
	            }
	        });
			PageUtil.fireSuccessfulInfoMessage("New comment posted!");
			wallData.setNewComment("");
			init();
		}
	}
	
	public Set<User> interactions(List<Comment> comments, User maker, User actor) {
		Set<User> result = new HashSet<User>();
		if (maker != null && !maker.equals(actor)) {
			result.add(maker);
		}
		if (comments == null || comments.size() == 0) {
			return result;
		}
		for (SocialActivity comment : comments) {
			if (comment.getMaker() != null && !comment.getMaker().equals(actor)) {
				result.add(comment.getMaker());
			}
		}
		return result;
	}

	
	public void newCommentOnSocialActivity(final SocialActivityData wallData, final String context) {
		if (wallData != null) {
//			final String commentText = StringUtil.cleanHtml(wallData.getNewComment());
			final String commentText = wallData.getNewComment();
			try {
				SocialActivity activity = defaultManager.get(SocialActivity.class, wallData.getSocialActivity().getId());
				List<Comment> comments = commentManager.getComments(activity);
				Set<User> interactions = interactions(comments, activity.getMaker(), loggedUser.getUser());
				
				final Date created = new Date();
				final SocialActivityCommentData commentData = new SocialActivityCommentData(commentText, loggedUser.getUser(), created, wallData);
				wallData.addComment(commentData);
				wallData.setShowHiddenComments(true);
				
				propagateCommentAsync(wallData.getSocialActivity().getId(), SocialActivity.class, context, commentText, created, commentData, interactions);
				PageUtil.fireSuccessfulInfoMessage("New comment posted!");
				wallData.setNewComment("");
				init();
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
			
			
		}
	}

	private void propagateCommentAsync(final long resourceId, final Class<? extends BaseEntity> resourceClazz,
			final String context, final String commentText, final Date created,
			final SocialActivityCommentData commentData, final Set<User> interactions) {
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Session session = (Session) defaultManager.getPersistence().openSession();
				
				try {
					BaseEntity resource = defaultManager.loadResource(resourceClazz, resourceId, session);
					
					Comment comment = commentManager.addComment(
							resource, 
							loggedUser.getUser(), 
							commentText, 
							created,
							context,
							session);
					
					session.flush();
					
					for(User interactedWith : interactions) {
						Map<String, String> parameters = new HashMap<String, String>();
						parameters.put("context", context);
						parameters.put("targetUserId", Long.toString(interactedWith.getId()));
						session.flush();
						
						Event event = eventFactory.generateEvent(EventType.Comment, loggedUser.getUser(), comment, resource, parameters);
						
						if (event != null) {
							socialActivityFactory.createSocialActivity(event, session, null);
						}
					}
					
					logger.debug("User \"" + loggedUser.getUser() +
							" commented on an resource "+resourceId+")");
					
					commentData.setId(comment.getId());
					
//		            	update of other registered users' walls is performed in InterfaceCacheUpdater
				} catch (EventException e) {
					logger.error(e);
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				} finally {
					HibernateUtil.close(session);
				}
			}
		});
	}
	
}