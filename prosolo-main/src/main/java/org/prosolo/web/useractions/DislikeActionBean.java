/**
 * 
 */
package org.prosolo.web.useractions;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.annotation.Annotation;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.activityWall.impl.data.SocialActivityData;
import org.prosolo.services.annotation.DislikeManager;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.activitywall.data.SocialActivityCommentData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author "Nikola Milikic"
 *
 */

@ManagedBean(name = "dislikeAction")
@Component("dislikeAction")
@Scope("request")
public class DislikeActionBean {
	
	private static Logger logger = Logger.getLogger(DislikeActionBean.class);

	@Autowired private LoggedUserBean loggedUser;
	@Autowired private LikeActionBean likeActionBean;
	@Autowired private DislikeManager dislikeManager;
	@Autowired private DefaultManager defaultManager;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	public boolean isDislikedByUser(Node resource) {
		if (resource != null) {
			return dislikeManager.isDislikedByUser(resource, loggedUser.getUser());
		}
		return false;
	}
	
	public boolean dislike(long resourceId, Class<? extends Node> clazz, Session session, String context) throws ResourceCouldNotBeLoadedException {
		Node resource = defaultManager.loadResource(clazz, resourceId, session);
		return dislike(resource, session, context);
	}
	
	public boolean dislike(Node resource, Session session, String context) {
		try {
			Annotation ann = dislikeManager.dislike(loggedUser.getUser(), resource, session, context);
			
			if (ann != null) {
				logger.debug("User "+loggedUser.getUser()+" disliked resource ("+resource.getId()+")");
				return true;
			} else {
				logger.error("Could not dislike resource "+resource.getTitle()+" ("+resource.getId()+") " +
						"by the user "+loggedUser.getUser());
			}
		} catch (EventException e) {
			logger.error("There was an error in disliking resource "+resource.getTitle()+" ("+resource.getId()+") " +
					"when user "+loggedUser.getUser()+" tried to dislike it. " + e.getMessage());
		}
		return false;
	}
	
	public boolean removeDislike(long resourceId, Class<? extends Node> clazz, Session session, String context) throws ResourceCouldNotBeLoadedException {
		Node resource = defaultManager.loadResource(clazz, resourceId, session);
		return removeDislike(resource, session, context);
	}
	
	public boolean removeDislike(Node resource, Session session, String context) {
		loggedUser.refreshUser();
		
		boolean successful = false;
		try {
			successful = dislikeManager.removeDislike(loggedUser.getUser(), resource, session, context);
			
			if (successful) {
				logger.debug("User "+loggedUser.getUser()+" removed disliked resource ("+resource+")");
			} else {
				logger.error("Could not remove dislike from a resource "+resource.getTitle()+" ("+resource+") " +
						"by the user "+loggedUser.getUser());
			}
		} catch (EventException e) {
			logger.error("Error when trying to remove dislike from resource "+resource.getTitle()+" ("+resource+") " +
					"by the user "+loggedUser.getUser()+". "+e);
		}
		
		return successful;
	}
	
	public void dislikeActivity(final ActivityWallData actData, final String context) {
		actData.setDisliked(true);
		actData.setDislikeCount(actData.getDislikeCount()+1);
		
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				long targetActivityId = actData.getId();
				Session session = (Session) defaultManager.getPersistence().openSession();
				
				try {
					dislikeManager.dislikeNode(loggedUser.getUser(), targetActivityId, session, context);
					
					logger.debug("User "+loggedUser.getUser()+" disliked target activity ("+targetActivityId+")");
					session.flush();
				} catch (EventException e) {
					logger.error("There was an error in disliking target activity "+targetActivityId+
							" when user "+loggedUser.getUser()+" tried to dislike it. " + e.getMessage());
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error("There was an error in disliking target activity "+targetActivityId+
							" when user "+loggedUser.getUser()+" tried to dislike it. " + e.getMessage());
				}
				
		
			 finally{
 				HibernateUtil.close(session);
 			} 
			}
		});
		
		likeActionBean.asyncUpdateOtherUsersActivityCaches(actData);
	}
	
	public void removeDislikeActivity(final ActivityWallData actData, final String context) {
		actData.setDisliked(false);
		actData.setDislikeCount(actData.getDislikeCount()-1);
		
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				long activityId = actData.getActivity().getId();
				Session session = (Session) defaultManager.getPersistence().openSession();
				
				try {
					dislikeManager.removeDislikeFromNode(loggedUser.getUser(), activityId, session, context);
					
					logger.debug("User "+loggedUser.getUser()+" removed dislike from activity ("+activityId+")");
					session.flush();
				} catch (EventException e) {
					logger.error("There was an error in removing dislike from activity "+activityId+
							" when user "+loggedUser.getUser() +" tried to remove dislike. " + e.getMessage());
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error("There was an error in removing dislike from activity "+activityId+
							" when user "+loggedUser.getUser() +" tried to remove dislike. " + e.getMessage());
				}
				
			
				 finally{
		 				HibernateUtil.close(session);
		 			} 
			}
		});
		
		likeActionBean.asyncUpdateOtherUsersActivityCaches(actData);
	}
	
	public void dislikeSocialActivity(final SocialActivityData wallData, final String context) {
		final long socialActivityId = wallData.getSocialActivity().getId();
		
		int currentDislikeCount = dislikeManager.getDislikeCountForSocialActivity(socialActivityId);
		final int newDislikeCount = currentDislikeCount + 1;
		wallData.setDislikeCount(newDislikeCount);
		wallData.setDisliked(true);
		
		taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
            	Session session = (Session) defaultManager.getPersistence().openSession();
            	try {
            		dislikeManager.dislikeSocialActivity(loggedUser.getUser(), wallData.getConfigId(), wallData.getSocialActivity().getId(), newDislikeCount, session, context);
            		
        			logger.debug("User "+loggedUser.getUser()+" liked Social Activity ("+socialActivityId+")");
        			session.flush();
            	} catch (EventException e) {
            		logger.error("There was an error in disliking social activity ("+socialActivityId+") " +
            				"when user "+loggedUser.getUser()+" tried to dislike it. " + e.getMessage());
            	} catch (ResourceCouldNotBeLoadedException e1) {
            		logger.error(e1);
            	}

				
				 finally{
		 				HibernateUtil.close(session);
		 			} 
            }
        });
		
		// there is no need for manually updating other user's caches as InterfaceCacheUpdater observer is doing that
	}
	
	public void removeDislikeFromSocialActivity(final SocialActivityData wallData, final String context) {
		final long socialActivityId = wallData.getSocialActivity().getId();
		
		int currentDislikeCount = dislikeManager.getDislikeCountForSocialActivity(socialActivityId);
		final int newDislikeCount = currentDislikeCount - 1;
		wallData.setDislikeCount(newDislikeCount);
		wallData.setDisliked(false);
			
		taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
            	Session session = (Session) defaultManager.getPersistence().openSession();
            	
				try {
					dislikeManager.removeDislikeFromSocialActivity(loggedUser.getUser(), wallData.getConfigId(), wallData.getSocialActivity().getId(), newDislikeCount, session, context);
					
					logger.debug("User "+loggedUser.getUser()+" removed dislike from Social Activity ("+socialActivityId+")");
					
					session.flush();
				} catch (EventException e) {
					logger.error("Error when user "+loggedUser.getUser()+ " tried to remove dislike from Social Activity "+socialActivityId+". "+e);
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
			

				 finally{
		 				HibernateUtil.close(session);
		 			} 
            }
        });
		// there is no need for manually updating other user's caches as InterfaceCacheUpdater observer is doing that
	}
	
	public boolean dislikeComment(final SocialActivityCommentData commentData, String context) {
		return updateDislikeFromComment(commentData, true, context);
	}
	
	public boolean removeDislikeFromComment(final SocialActivityCommentData commentData, String context) {
		return updateDislikeFromComment(commentData, false, context);
	}
	
	public boolean updateDislikeFromComment(final SocialActivityCommentData commentData, 
			final boolean disliked, final String context) {
		
		commentData.setLiked(disliked);
		
		final int dislikeCount = disliked ? commentData.getDislikeCount()+1 : commentData.getDislikeCount()-1;
		commentData.setDislikeCount(dislikeCount);
		
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				long commentId = commentData.getId();
				Session session = (Session) defaultManager.getPersistence().openSession();
			
				try {
					if (disliked) {
						dislikeManager.dislikeComment(loggedUser.getUser(), commentId, session, context);
						logger.debug("User "+loggedUser.getUser()+" liked comment "+commentId);
					} else {
						dislikeManager.removeDislikeFromComment(loggedUser.getUser(), commentId, session, context);
						logger.debug("User "+loggedUser.getUser()+" unliked comment "+commentId);
					}	
				} catch (EventException e) {
					logger.error("Error when user "+loggedUser.getUser()+" tried to update dislike count of comment "+commentId+". "+e);
				} catch (ResourceCouldNotBeLoadedException e1) {
					logger.error(e1);
				} finally{
	 				HibernateUtil.close(session);
	 			} 
			}
		});
		return true;
	}
}
