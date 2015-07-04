package org.prosolo.web.dialogs;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.domainmodel.general.Node;
import org.prosolo.domainmodel.portfolio.CompletedResource;
import org.prosolo.domainmodel.user.User;
import org.prosolo.domainmodel.workflow.evaluation.Badge;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.nodes.BadgeManager;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "badgelist")
@Component("badgelist")
@Scope("request")
public class BadgeListDialog implements Serializable {

	private static final long serialVersionUID = -7585213157581185003L;
	
	private static Logger logger = Logger.getLogger(BadgeListDialog.class);

	@Autowired private BadgeManager badgeManager;
	@Autowired private LoggingNavigationBean actionLogger;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;

	private Map<Badge, List<User>> badges = new HashMap<Badge, List<User>>();
	
	public void initialize (CompletedResource resource) {
		if (resource != null) {
			try {
				badges = badgeManager.getBadgesForResource(resource.getId(), CompletedResource.class);
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
		}
	}
	
	public void initializeById (final long resourceId, final String context) {
		try {
			badges = badgeManager.getBadgesForResource(resourceId, Node.class);
			
			taskExecutor.execute(new Runnable() {
	            @Override
	            public void run() {
	            	actionLogger.logServiceUse(
            			ComponentName.BADGE_LIST_DIALOG, 
            			"context", context,
            			"resourceType", Node.class.getSimpleName(),
            			"id", String.valueOf(resourceId));
	            }
			});
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public void initializeForCompletedResourceById(long compResourceId, String context) {
		try {
			badges = badgeManager.getBadgesForResource(compResourceId, CompletedResource.class);
			
			actionLogger.logServiceUse(
					ComponentName.BADGE_LIST_DIALOG, 
					"context", context,
					"resourceType", CompletedResource.class.getSimpleName(),
					"id", String.valueOf(compResourceId));
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	/*
	 * GETTERS / SETTERS
	 */

	public Map<Badge, List<User>> getBadges() {
		return badges;
	}

	public void setBadges(Map<Badge, List<User>> badges) {
		this.badges = badges;
	}
	
}
