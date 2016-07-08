package org.prosolo.web.activitywall;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activitywall.old.SocialActivity;
import org.prosolo.common.domainmodel.interfacesettings.FilterType;
import org.prosolo.common.exceptions.KeyNotFoundInBundleException;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.activityWall.impl.data.SocialActivityData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.interaction.PostManager;
import org.prosolo.services.interfaceSettings.InterfaceSettingsManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.displayers.StatusWallSocialActivitiesDisplayer;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.util.PageUtil;
import org.prosolo.web.util.ResourceBundleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author "Nikola Milikic"
 *
 */

@ManagedBean(name = "activitywall")
@Component("activitywall")
@Scope("session")
public class ActivityWallBean1 {
	
	private static Logger logger = Logger.getLogger(ActivityWallBean1.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private InterfaceSettingsManager interfaceSettingsManager;
	@Autowired private PostManager postManager;
	@Autowired private EventFactory eventFactory;
	@Autowired private LoggingNavigationBean actionLogger;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	private StatusWallSocialActivitiesDisplayer activityWallDisplayer;

	// stores new activities temporarily
//	private List<SocialActivityData1> newActivities = new LinkedList<SocialActivityData1>();
	
	@PostConstruct
	public void init() {
		activityWallDisplayer = ServiceLocator.getInstance().getService(StatusWallSocialActivitiesDisplayer.class);
		activityWallDisplayer.init(loggedUser.getUserId(), loggedUser.getLocale(), loggedUser.getSelectedStatusWallFilter());
	}
	
	public void initializeActivities() {
		logger.debug("Initializing main activity wall");
		
		activityWallDisplayer.initializeActivities();
		logger.debug("Initialized main activity wall");
	}
	
	public void loadMoreActivities() {
		activityWallDisplayer.loadMoreActivities("statusWall");
	}
	
	public void refresh() {
		activityWallDisplayer.refresh();
	}
	
	public void updatePost(SocialActivityData socialActivityData, final String context) {
		final String updatedText = socialActivityData.getText();
		
		try {
			final SocialActivity updatedSocialActivity = postManager.updatePost(
					loggedUser.getUserId(),
					socialActivityData.getSocialActivity().getId(),
					updatedText, 
					context);
			
			// update cache of logged in user
			activityWallDisplayer.updateSocialActivity(updatedSocialActivity);
			
			taskExecutor.execute(new Runnable() {
				@Override
				public void run() {
					Map<String, String> parameters = new HashMap<String, String>();
					parameters.put("context", context);
					parameters.put("newText", updatedText);
					
					try {
						eventFactory.generateEvent(EventType.PostUpdate, loggedUser.getUserId(), updatedSocialActivity, parameters);
					} catch (EventException e) {
						logger.error(e);
					}
				}
			});
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
		
		PageUtil.fireSuccessfulInfoMessage("Post updated");
	}
	
	public void changeFilter() {
		final FilterType filterType = FilterType.valueOf(PageUtil.getPostParameter("filter"));
		long courseId = 0;
		
		String courseIdString = PageUtil.getPostParameter("courseId");
		if (courseIdString != null && !courseIdString.isEmpty()) {
			courseId = Long.parseLong(courseIdString);
		}
		
		logger.debug("User "+loggedUser.getFullName()+" is changing Activity Wall filter to '"+filterType+"'.");
		
		if (filterType != null) {
			boolean successful = interfaceSettingsManager.changeActivityWallFilter(loggedUser.getInterfaceSettings(), filterType, courseId);
			
			loggedUser.refreshUserSettings();
			loggedUser.loadStatusWallFilter(filterType, courseId);
			
			activityWallDisplayer.changeFilter(loggedUser.getSelectedStatusWallFilter());

			if (successful) {
				logger.debug("User "+loggedUser.getFullName()+" successfully changed Activity Wall filter to '"+filterType+"'.");
				PageUtil.fireSuccessfulInfoMessage("Activity Wall filter changed!");
			} else {
				logger.error("User "+loggedUser.getFullName()+" could not change Activity Wall filter to '"+filterType+"'.");
				PageUtil.fireErrorMessage("There was an error with changing Activity Wall filter!");
			}
			
			final long courseId1 = courseId;
			
			taskExecutor.execute(new Runnable() {
				@Override
				public void run() {
					Map<String, String> parameters = new HashMap<String, String>();
					parameters.put("context", "statusWall.filter");
					parameters.put("filter", filterType.name());
					
					//TODO commented
//					if (filterType.equals(FilterType.COURSE)) {
//						parameters.put("courseId", String.valueOf(courseId1));
//					}
					
					actionLogger.logEvent(EventType.FILTER_CHANGE, parameters);
				}
			});
		} else {
			logger.error("Could not find FilterType for '"+filterType+" for changing Activity Wall filter of user "+loggedUser.getFullName());
		}
	}
	
	public String getFilterPrettyName(FilterType filterType) {
		try {
			return ResourceBundleUtil.getMessage( 
					"activitywall.filter." + filterType.name().toLowerCase(), 
					FacesContext.getCurrentInstance().getViewRoot().getLocale());
		} catch (KeyNotFoundInBundleException e) {
			logger.error(e.getMessage());
		}
		return "";
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	public synchronized List<SocialActivityData> getAllActivities() {
		return activityWallDisplayer.getAllActivities();
	}
	
	public int getRefreshRate() {
		return Settings.getInstance().config.application.defaultRefreshRate;
	}

	public boolean isMoreToLoad() {
		return activityWallDisplayer.isMoreToLoad();
	}

	public String getScrollPosition() {
		return String.valueOf(activityWallDisplayer.getScrollPosition());
	}

	public void setScrollPosition(String scrollPosition) {
		try {
			activityWallDisplayer.setScrollPosition(Long.parseLong(scrollPosition));
		} catch (NumberFormatException nfe) {}
	}

	public StatusWallSocialActivitiesDisplayer getActivityWallDisplayer() {
		return activityWallDisplayer;
	}
	
}