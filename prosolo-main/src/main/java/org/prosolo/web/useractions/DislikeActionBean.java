/**
 * 
 */
package org.prosolo.web.useractions;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.activityWall.impl.data.SocialActivityData;
import org.prosolo.services.annotation.DislikeManager;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.web.LoggedUserBean;
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

@ManagedBean(name = "dislikeAction")
@Component("dislikeAction")
@Scope("request")
public class DislikeActionBean {
	
	private static Logger logger = Logger.getLogger(DislikeActionBean.class);

	@Autowired private LoggedUserBean loggedUser;
	@Autowired private DislikeManager dislikeManager;
	@Autowired private DefaultManager defaultManager;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	public boolean isDislikedByUser(Node resource) {
		if (resource != null) {
			return dislikeManager.isDislikedByUser(resource, loggedUser.getUserId());
		}
		return false;
	}
	
	public boolean removeDislike(long resourceId, Class<? extends Node> clazz, Session session, String context) throws ResourceCouldNotBeLoadedException {
		Node resource = defaultManager.loadResource(clazz, resourceId, session);
		return removeDislike(resource, session, context);
	}
	
	public boolean removeDislike(Node resource, Session session, String context) {
		boolean successful = false;
		try {
			successful = dislikeManager.removeDislike(loggedUser.getUserId(), resource, session, context,
					null, null, null);
			
			if (successful) {
				logger.debug("User "+loggedUser.getUserId()+" removed disliked resource ("+resource+")");
			} else {
				logger.error("Could not remove dislike from a resource "+resource.getTitle()+" ("+resource+") " +
						"by the user "+loggedUser.getUserId());
			}
		} catch (EventException e) {
			logger.error("Error when trying to remove dislike from resource "+resource.getTitle()+" ("+resource+") " +
					"by the user "+loggedUser.getUserId()+". "+e);
		}
		
		return successful;
	}
	
	public void dislikeSocialActivity(final SocialActivityData wallData, final String context) {
		final long socialActivityId = wallData.getSocialActivity().getId();
		
		int currentDislikeCount = dislikeManager.getDislikeCountForSocialActivity(socialActivityId);
		final int newDislikeCount = currentDislikeCount + 1;
		wallData.setDislikeCount(newDislikeCount);
		wallData.setDisliked(true);
		
		String page = PageUtil.getPostParameter("page");
		String learningContext = PageUtil.getPostParameter("learningContext");
		String service = PageUtil.getPostParameter("service");
		taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
            	Session session = (Session) defaultManager.getPersistence().openSession();
            	try {
            		dislikeManager.dislikeSocialActivity(loggedUser.getUserId(), wallData.getConfigId(), 
            				wallData.getSocialActivity().getId(), newDislikeCount, session, context,
            				page, learningContext, service);
            		
        			logger.debug("User "+loggedUser.getUserId()+" liked Social Activity ("+socialActivityId+")");
        			session.flush();
            	} catch (EventException e) {
            		logger.error("There was an error in disliking social activity ("+socialActivityId+") " +
            				"when user "+loggedUser.getUserId()+" tried to dislike it. " + e.getMessage());
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
			
		String page = PageUtil.getPostParameter("page");
		String learningContext = PageUtil.getPostParameter("learningContext");
		String service = PageUtil.getPostParameter("service");
		taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
            	Session session = (Session) defaultManager.getPersistence().openSession();
            	
				try {
					dislikeManager.removeDislikeFromSocialActivity(loggedUser.getUserId(), 
							wallData.getConfigId(), wallData.getSocialActivity().getId(), newDislikeCount, 
							session, context, page, learningContext, service);
					
					logger.debug("User "+loggedUser.getUserId()+" removed dislike from Social Activity ("+socialActivityId+")");
					
					session.flush();
				} catch (EventException e) {
					logger.error("Error when user "+loggedUser.getUserId()+ " tried to remove dislike from Social Activity "+socialActivityId+". "+e);
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
	
}
