package org.prosolo.web.goals;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.prosolo.app.Settings;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.domainmodel.activities.events.EventType;
import org.prosolo.domainmodel.content.GoalNote;
import org.prosolo.domainmodel.user.TargetLearningGoal;
import org.prosolo.domainmodel.user.User;
import org.prosolo.services.activityWall.ActivityWallFactory;
import org.prosolo.services.activityWall.ActivityWallManager;
import org.prosolo.services.activityWall.SocialActivityFactory;
import org.prosolo.services.activityWall.SocialActivityHandler;
import org.prosolo.services.activityWall.impl.data.SocialActivityData;
//import org.prosolo.services.activitystream.SocialActivityInboxUpdater;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.interaction.PostManager;
import org.prosolo.services.interaction.impl.PostManagerImpl.PostEvent;
import org.prosolo.services.upload.UploadManager;
import org.prosolo.util.string.StringUtil;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.ActivityWallBean;
import org.prosolo.web.activitywall.data.AttachmentPreview;
import org.prosolo.web.activitywall.data.UserData;
import org.prosolo.web.activitywall.displayers.GoalWallSocialActivitiesDisplayer;
import org.prosolo.web.activitywall.util.WallActivityConverter;
import org.prosolo.web.goals.cache.GoalDataCache;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.useractions.data.NewPostData;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "goalwall")
@Component("goalwall")
@Scope("view")
public class GoalWallBean implements Serializable {

	private static final long serialVersionUID = -1198270070356681265L;

	private static Logger logger = Logger.getLogger(GoalWallBean.class);

	@Autowired private PostManager postManager;
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private UploadManager uploadManager;
	@Autowired private EventFactory eventFactory;
	@Autowired private SocialActivityFactory socialActivityFactory;
	@Autowired private ActivityWallManager activityWallManager;
	@Autowired private ActivityWallFactory activityWallFactory;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	 
	@Autowired private WallActivityConverter wallActivityConverter;
	@Autowired private LearningGoalsBean goalsBean;
	@Autowired private ActivityWallBean activityWallBean;
	@Autowired private LoggingNavigationBean actionLogger;
	@Autowired private SocialActivityHandler socialActivityHandler;

	private int refreshRate = Settings.getInstance().config.application.defaultRefreshRate;

	private NewPostData newPostData = new NewPostData();
	private boolean connectNewPostWithStatus;
	
	private UserData userFilterData;
	
	private GoalWallSocialActivitiesDisplayer goalWallDisplayer;
	
	@PostConstruct
	public void init() {
		logger.debug("Initializing Goal Wall");
		goalWallDisplayer = ServiceLocator.getInstance().getService(GoalWallSocialActivitiesDisplayer.class);
		goalWallDisplayer.init(loggedUser.getUser(), loggedUser.getLocale(), loggedUser.getSelectedLearningGoalFilter());
	}
	
	public void initializeActivities() {
		logger.debug("Initializing goal wall");
		
		if (goalsBean != null && goalsBean.getSelectedGoalData() != null) {
			goalWallDisplayer.initializeActivities();
		}
	}
	
	public void loadMoreActivities() {
		final long targetGoalId = goalsBean.getSelectedGoalData().getData().getTargetGoalId();

		Map<String, String> parameters = new HashMap<String, String>();				
		parameters.put("context", "learn.goalWall.targetGoal."+targetGoalId);
		parameters.put("link", "loadMore");
		parameters.put("targetGoalId", String.valueOf(targetGoalId));
		
		goalWallDisplayer.loadMoreActivities(parameters);
	
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Map<String, String> parameters = new HashMap<String, String>();				
				parameters.put("context", "learn.goalWall.targetGoal."+targetGoalId);
				parameters.put("link", "loadMore");
				parameters.put("targetGoalId", String.valueOf(targetGoalId));

				
				actionLogger.logEvent(EventType.SERVICEUSE, "MOUSE_CLICK", parameters);
			}
		});
	}

	public void refresh() {
		goalWallDisplayer.refresh();
	}
	
	public void createNewPost() {
		createNewPost(goalsBean.getSelectedGoalData(), this.newPostData);
	}
	
	public void createNewPost(GoalDataCache goalData, NewPostData newPostData) {
		try {
			User user = loggedUser.getUser();
			PostEvent postEvent = postManager.createNewGoalNote(
					user, 
					goalData.getData().getGoalId(), 
					StringUtil.cleanHtml(newPostData.getText()), 
					newPostData.getAttachmentPreview(),
					goalsBean.getSelectedGoalData().getData().getVisibility(),
					connectNewPostWithStatus,
					true,
					"learn.goalWall.newPost");
			
			GoalNote goalNote = (GoalNote) postEvent.getPost();
			
			if (goalNote != null) {
				logger.debug("User \"" + user.getName()+" "+user.getLastname()+"\" ("+user.getId()+")" +
						" added a goal note \""+newPostData.getText()+"\" )"+goalNote.getId()+")");
				
				
				socialActivityHandler.addSociaActivitySyncAndPropagateToStatusAndGoalWall(postEvent.getEvent());
						
				PageUtil.fireSuccessfulInfoMessage("New goal note posted!");
				
				this.newPostData = new NewPostData();
			}
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		} catch (EventException e) {
			logger.error(e.getMessage());
		}
	}

	public void handleFileUpload(FileUploadEvent event) {
    	UploadedFile uploadedFile = event.getFile();
    	
		try {
			AttachmentPreview attachmentPreview = uploadManager.uploadFile(loggedUser.getUser(), uploadedFile, uploadedFile.getFileName());
			
			newPostData.setAttachmentPreview(attachmentPreview);

		} catch (IOException ioe) {
			ioe.printStackTrace();
			
			PageUtil.fireErrorMessage("The file was not uploaded!");
		}
    }
	
	// filter Goal Wall activities by user 
	public void setUserFilter(UserData userFilter) {
		logger.debug("Filtering Goal Wall by user " + userFilter);
		
		loggedUser.getSelectedLearningGoalFilter().setSelectedParticipant(userFilter.getId());
		
		this.userFilterData = userFilter;
		
		GoalDataCache selectedGoalData = goalsBean.getSelectedGoalData();
		
		if (goalsBean != null && selectedGoalData != null) {
			goalWallDisplayer.setFilter(loggedUser.getSelectedLearningGoalFilter());
			goalWallDisplayer.resetActivities();
			goalWallDisplayer.initializeActivities();

			long targetGoalId = selectedGoalData.getData().getTargetGoalId();
			
			// log event
	    	Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("context", "learn.targetGoal."+targetGoalId+".navigation");
			parameters.put("targetGoalId", String.valueOf(targetGoalId));
			parameters.put("user", String.valueOf(userFilter.getId()));
			
			actionLogger.logEvent(
					EventType.GOAL_WALL_FILTER, 
					TargetLearningGoal.class.getSimpleName(), 
					targetGoalId, 
					parameters);
		}
	}

	public void removeUserFilter() {
		logger.debug("User filter reseted");
		
		this.userFilterData = null;
		
		if (goalsBean != null && goalsBean.getSelectedGoalData() != null) {
			loggedUser.getSelectedLearningGoalFilter().setSelectedParticipant(0);
			
			goalWallDisplayer.setFilter(loggedUser.getSelectedLearningGoalFilter());
			goalWallDisplayer.resetActivities();
			goalWallDisplayer.initializeActivities();
		}
	}
	
    /*
     * GETTERS / SETTERS
     */
    public List<SocialActivityData> getAllActivities() {
    	if (goalsBean.getSelectedGoalData() != null) {
    		
    		return goalWallDisplayer.getAllActivities();
    	}
    	return null;
	}

	public int getRefreshRate() {
		return refreshRate;
	}
	
	public NewPostData getNewPostData() {
		return newPostData;
	}

	public void setNewPostData(NewPostData newPostData) {
		this.newPostData = newPostData;
	}
	
	public boolean isMoreToLoad() {
		return goalsBean.getSelectedGoalData() != null && goalWallDisplayer.isMoreToLoad();
	}

	public boolean isConnectNewPostWithStatus() {
		return connectNewPostWithStatus;
	}

	public void setConnectNewPostWithStatus(boolean connectNewPostWithStatus) {
		this.connectNewPostWithStatus = connectNewPostWithStatus;
	}

	public UserData getUserFilterData() {
		return userFilterData;
	}

	public GoalWallSocialActivitiesDisplayer getGoalWallDisplayer() {
		return goalWallDisplayer;
	}
	
}