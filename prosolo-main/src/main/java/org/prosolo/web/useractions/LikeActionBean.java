/**
 * 
 */
package org.prosolo.web.useractions;

import java.util.Collection;

import javax.faces.bean.ManagedBean;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.annotation.Annotation;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.activityWall.impl.data.SocialActivityData;
import org.prosolo.services.annotation.LikeManager;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.activitywall.data.SocialActivityCommentData;
import org.prosolo.web.goals.LearnBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author "Nikola Milikic"
 *
 */
@ManagedBean(name = "likeaction")
@Component("likeaction")
@Scope("request")
public class LikeActionBean {
	
	private static Logger logger = Logger.getLogger(LikeActionBean.class);

	@Autowired private LoggedUserBean loggedUser;
	@Autowired private LikeManager likeManager;
	@Autowired private DefaultManager defaultManager;
	@Autowired private ApplicationBean applicationBean;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	public boolean isLikedByUser(Node resource) {
		if (resource != null) {
			return likeManager.isLikedByUser(resource, loggedUser.getUserId());
		}
		return false;
	}
	
	public boolean like(long resourceId, Class<? extends Node> clazz, Session session, String context) throws ResourceCouldNotBeLoadedException {
		Node resource = defaultManager.loadResource(clazz, resourceId, session);
		return like(resource, session, context);
	}
	
	public boolean like(Node resource, Session session, String context) {
		try {
			Annotation ann = likeManager.like(loggedUser.getUserId(), resource, session, context, null, null, null);
			
			if (ann != null) {
				logger.debug("User "+loggedUser.getUserId()+" liked resource ("+resource.getId()+")");
				return true;
			} else {
				logger.error("Could not like resource "+resource.getTitle()+" ("+resource.getId()+") " +
						"by the user "+loggedUser.getUserId());
			}
		} catch (EventException | ResourceCouldNotBeLoadedException e) {
			logger.error("There was an error in liking resource "+resource.getTitle()+" ("+resource.getId()+") " +
					"when user "+loggedUser.getUserId()+" tried to like it. " + e.getMessage());
		}
		return false;
	}
	
	public boolean removeLike(long resourceId, Class<? extends Node> clazz, Session session, String context) throws ResourceCouldNotBeLoadedException {
		Node resource = defaultManager.loadResource(clazz, resourceId, session);
		return removeLike(resource, session, context);
	}
	
	public boolean removeLike(Node resource, Session session, String context) {
		boolean successful = false;
		try {
			successful = likeManager.removeLike(loggedUser.getUserId(), resource, session, context, 
					null, null, null);
			
			if (successful) {
				logger.debug("User "+loggedUser.getUserId()+" unliked resource ("+resource+")");
			} else {
				logger.error("Could not unlike resource "+resource.getTitle()+" ("+resource+") " +
						"by the user "+loggedUser.getUserId());
			}
		} catch (EventException e) {
			logger.error("Error when trying to unlike resource "+resource.getTitle()+" ("+resource+") " +
					"by the user "+loggedUser.getUserId()+". "+e);
		}
		
		return successful;
	}
	
	public void likeActivity(final ActivityWallData actData, final String context) {
		actData.setLiked(true);
		actData.setLikeCount(actData.getLikeCount()+1);
		
		String page = PageUtil.getPostParameter("page");
		String lContext = PageUtil.getPostParameter("learningContext");
		String service = PageUtil.getPostParameter("service");
		
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				long targetActivityId = actData.getId();
				Session session = (Session) defaultManager.getPersistence().openSession();
				
				try {
					likeManager.likeNode(loggedUser.getUserId(), targetActivityId, session, context,
							page, lContext, service);
					
					logger.debug("User "+loggedUser.getUserId()+" liked target activity ("+targetActivityId+")");
					session.flush();
				} catch (EventException e) {
					logger.error("There was an error in liking target activity "+ targetActivityId +
							"when user "+loggedUser.getUserId()+" tried to like it. " + e.getMessage());
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error("There was an error in liking target activity "+ targetActivityId +
							"when user "+loggedUser.getUserId()+" tried to like it. " + e.getMessage());
				} finally {
					HibernateUtil.close(session);
				}
			}
		});
		
		asyncUpdateOtherUsersActivityCaches(actData);
	}

	public void asyncUpdateOtherUsersActivityCaches(final ActivityWallData actData) {
		taskExecutor.execute(new Runnable() {
		    @Override
		    public void run() {
	    		Collection<HttpSession> userSessions = applicationBean.getAllHttpSessions().values();
	    		
	    		for (HttpSession userSession : userSessions) {
	    			LearnBean userLearningGoalBean = (LearnBean) userSession.getAttribute("learninggoals");
					
					if (userLearningGoalBean != null) {
						userLearningGoalBean.getData().updateAllActivityData(actData);
					}
	    		}
			}
		});
	}
	
	public void removeLikeActivity(final ActivityWallData actData, final String context) {
		actData.setLiked(false);
		actData.setLikeCount(actData.getLikeCount()-1);
		
		String page = PageUtil.getPostParameter("page");
		String lContext = PageUtil.getPostParameter("learningContext");
		String service = PageUtil.getPostParameter("service");
		
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				long targetActivityId = actData.getId();
				Session session = (Session) defaultManager.getPersistence().openSession();
				
				try {
					likeManager.removeLikeFromNode(loggedUser.getUserId(), targetActivityId, session, context,
							page, lContext, service);
					
					logger.debug("User "+loggedUser.getUserId()+" unliked target activity ("+targetActivityId+")");
					session.flush();
				} catch (EventException e) {
					logger.error("There was an error in liking target activity "+ targetActivityId +
							"when user "+loggedUser.getUserId()+" tried to unlike it. " + e.getMessage());
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error("There was an error in liking target activity "+ targetActivityId +
							"when user "+loggedUser.getUserId()+" tried to unlike it. " + e.getMessage());
				}
 
				 finally{
		 				HibernateUtil.close(session);
		 			} 
			}
		});
		
		asyncUpdateOtherUsersActivityCaches(actData);
	}
	
	public void likeSocialActivity(final SocialActivityData wallData, final String context) {
		final long socialActivityId = wallData.getSocialActivity().getId();
		
		int currentLinkCount = likeManager.getLikeCountForSocialActivity(socialActivityId);
		final int newLikeCount = currentLinkCount + 1;
		wallData.setLikeCount(newLikeCount);
		wallData.setLiked(true);
		
		String page = PageUtil.getPostParameter("page");
		String learningContext = PageUtil.getPostParameter("learningContext");
		String service = PageUtil.getPostParameter("service");
		taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
            	Session session = (Session) defaultManager.getPersistence().openSession();
            	
            	try {
            		likeManager.likeSocialActivity(loggedUser.getUserId(), wallData.getConfigId(), 
            				wallData.getSocialActivity().getId(), newLikeCount, session, context,
            				page, learningContext, service);
            		
        			logger.debug("User "+loggedUser.getUserId()+" liked Social Activity ("+socialActivityId+")");

    				session.flush();
            	} catch (EventException e) {
            		logger.error("There was an error in liking social activity ("+socialActivityId+") " +
            				"when user "+loggedUser.getUserId()+" tried to like it. " + e.getMessage());
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
	
	public void removeLikeFromSocialActivity(final SocialActivityData wallData, final String context) {
		final long socialActivityId = wallData.getSocialActivity().getId();
		
		int currentLinkCount = likeManager.getLikeCountForSocialActivity(socialActivityId);
		final int newLikeCount = currentLinkCount - 1;
		wallData.setLikeCount(newLikeCount);
		wallData.setLiked(false);
			
		String page = PageUtil.getPostParameter("page");
		String learningContext = PageUtil.getPostParameter("learningContext");
		String service = PageUtil.getPostParameter("service");
		taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
            	Session session = (Session) defaultManager.getPersistence().openSession();
            	
				try {
					likeManager.removeLikeFromSocialActivity(loggedUser.getUserId(), wallData.getConfigId(), 
							wallData.getSocialActivity().getId(), newLikeCount, session, context,
							page, learningContext, service);

					logger.debug("User "+loggedUser.getUserId()+" unliked Social Activity ("+socialActivityId+")");

					session.flush();
				} catch (EventException e) {
					logger.error("Error when user "+loggedUser.getUserId()+ " tried to unlike Social Activity "+socialActivityId+". "+e);
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
	
	public void likeComment(final SocialActivityCommentData commentData, String context) {
		updateLikeForComment(commentData, true, context);
	}
	
	public void removeLikeFromComment(final SocialActivityCommentData commentData, String context) {
		updateLikeForComment(commentData, false, context);
	}
	
	public boolean updateLikeForComment(final SocialActivityCommentData commentData, 
			final boolean liked, final String context) {
		
		commentData.setLiked(liked);
		
		final int likeCount = liked ? commentData.getLikeCount()+1 : commentData.getLikeCount()-1;
		commentData.setLikeCount(likeCount);
		
		String page = PageUtil.getPostParameter("page");
		String lContext = PageUtil.getPostParameter("learningContext");
		String service = PageUtil.getPostParameter("service");
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				long commentId = commentData.getId();
				Session session = (Session) defaultManager.getPersistence().openSession();
			
				try {
					if (liked) {
						likeManager.likeComment(loggedUser.getUserId(), commentId, session, context,
								page, lContext, service);
						logger.debug("User "+loggedUser.getUserId()+" liked comment "+commentId);
					} else {
						likeManager.removeLikeFromComment(loggedUser.getUserId(), commentId, session, context,
								page, lContext, service);
						logger.debug("User "+loggedUser.getUserId()+" unliked comment "+commentId);
					}
					session.flush();
				} catch (EventException e) {
					logger.error("Error when user "+loggedUser.getUserId()+" tried to update like count of comment "+commentId+". "+e);
				} catch (ResourceCouldNotBeLoadedException e1) {
					logger.error(e1);
				}

			
				 finally{
		 				HibernateUtil.close(session);
		 			} 
			}
		});
		return true;
	}
	
}
