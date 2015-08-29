package org.prosolo.web.useractions;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.comments.Comment;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.activityWall.ActivityWallManager;
import org.prosolo.services.activityWall.SocialActivityFactory;
import org.prosolo.services.activityWall.impl.data.SocialActivityData;
import org.prosolo.services.annotation.LikeManager;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.interaction.CommentingManager;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.web.ApplicationBean;
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
public class ComentActionBean implements Serializable {
	
	private static final long serialVersionUID = -4386479460010699841L;

	private static Logger logger = Logger.getLogger(ComentActionBean.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private CommentingManager commentManager;
	@Autowired private ApplicationBean applicationBean;
	@Autowired private LikeManager likeManager;
	@Autowired private DefaultManager defaultManager;
	@Autowired private ActivityWallManager activityWallManager;
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
						
						BaseEntity resource = defaultManager.loadResource(TargetActivity.class, targetActiviryId, session);

						Comment comment = commentManager.addComment(
								resource, 
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
	
	public void newCommentOnSocialActivity(final SocialActivityData wallData, final String context) {
		if (wallData != null) {
//			final String commentText = StringUtil.cleanHtml(wallData.getNewComment());
			final String commentText = wallData.getNewComment();
			
			final Date created = new Date();
			final SocialActivityCommentData commentData = new SocialActivityCommentData(commentText, loggedUser.getUser(), created, wallData);
			wallData.addComment(commentData);
			wallData.setShowHiddenComments(true);
			
			propagateCommentAsync(wallData.getSocialActivity().getId(), SocialActivity.class, context, commentText, created, commentData);
			PageUtil.fireSuccessfulInfoMessage("New comment posted!");
			wallData.setNewComment("");
			init();
		}
	}

	private void propagateCommentAsync(final long resourceId, final Class<? extends BaseEntity> resourceClazz, final String context, final String commentText, final Date created, final SocialActivityCommentData commentData) {
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
					
					Map<String, String> parameters = new HashMap<String, String>();
					parameters.put("context", context);
					session.flush();
					
					Event event = eventFactory.generateEvent(EventType.Comment, loggedUser.getUser(), comment, resource, parameters);
					
					if (event != null) {
						socialActivityFactory.createSocialActivity(event, session, null);
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
