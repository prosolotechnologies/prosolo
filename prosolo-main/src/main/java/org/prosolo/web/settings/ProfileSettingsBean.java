/**
 * 
 */
package org.prosolo.web.settings;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.organization.OrganizationalUnit;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.web.util.AvatarUtils;
import org.prosolo.services.activityWall.impl.data.SocialActivityData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.upload.AvatarProcessor;
import org.prosolo.util.nodes.NodeTitleComparator;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.ActivityWallBean;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.activitywall.data.SocialActivityCommentData;
import org.prosolo.web.communications.data.MessagesThreadData;
import org.prosolo.web.goals.GoalWallBean;
import org.prosolo.web.goals.LearnBean;
import org.prosolo.web.goals.cache.CompetenceDataCache;
import org.prosolo.web.goals.cache.GoalDataCache;
import org.prosolo.web.home.PeopleRecommenderBean;
import org.prosolo.web.notification.TopInboxBean;
import org.prosolo.web.notification.TopNotificationsBean;
import org.prosolo.web.notification.data.NotificationData;
import org.prosolo.web.settings.data.AccountData;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author "Nikola Milikic"
 * 
 */
@ManagedBean(name = "profileSettings")
@Component("profileSettings")
@Scope("view")
public class ProfileSettingsBean implements Serializable {

	private static final long serialVersionUID = 1649841825780113183L;

	protected static Logger logger = Logger.getLogger(ProfileSettingsBean.class);

	@Autowired private LoggedUserBean loggedUser;
	@Autowired private UserManager userManager;
	@Autowired private OrganizationManager orgManager;
	@Autowired private AvatarProcessor avatarProcessor;
	@Autowired private EventFactory eventFactory;
	@Autowired private ApplicationBean applicationBean;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	@Autowired private PeopleRecommenderBean peopleRecommenderBean;
	
	private AccountData accountData;
	
	private OrganizationalUnit[] departments;
	private OrganizationalUnit tempDepartment;
	
	@PostConstruct
	public void initializeAccountData() {
//		loggedUser.refreshUser();
		
		accountData = new AccountData();
		accountData.setId(loggedUser.getUser().getId());
		accountData.setAvatarPath(loggedUser.getBigAvatar());
		accountData.setFirstName(loggedUser.getUser().getName());
		accountData.setLastName(loggedUser.getUser().getLastname());
		
		// position
		accountData.setPosition(loggedUser.getUser().getPosition());
		
		// location
		accountData.setLocationName(loggedUser.getUser().getLocationName());
 
		String lat=null;
		String lon=null;
		if(loggedUser.getUser().getLatitude()!=null){
			lat=String.valueOf(loggedUser.getUser().getLatitude());
		}
		if(loggedUser.getUser().getLongitude()!=null){
		 lon=String.valueOf(loggedUser.getUser().getLongitude());
		}
		accountData.setLatitude(lat);
		accountData.setLongitude(lon);
		//accountData.setLongitude(String.valueOf(loggedUser.getUser().getLongitude()));
		
		// department
//		Unit_User userUnit = loggedUser.getUser().getUnitUser().iterator().next();
//		accountData.setDepartment(userUnit.getUnit());
	}
	
	/*
	 * ACTIONS
	 */
	public void saveChanges() {
		boolean changed = false;
		
		User user = loggedUser.refreshUser();
		
		if (!accountData.getFirstName().equals(loggedUser.getUser().getName())) {
			user.setName(accountData.getFirstName());
			changed = true;
		}

		if (!accountData.getLastName().equals(loggedUser.getUser().getLastname())) {
			user.setLastname(accountData.getLastName());
			changed = true;
		}

		if (!accountData.getPosition().equals(loggedUser.getUser().getPosition())) {
			user.setPosition(accountData.getPosition());
			changed = true;
		}
		if ((accountData.getLocationName() != null && loggedUser.getUser().getLocationName() == null)
				||
				(!accountData.getLocationName().equals(loggedUser.getUser().getLocationName()))) {
			try {
				user.setLocationName(accountData.getLocationName());
				user.setLatitude(Double.valueOf(accountData.getLatitude()));
				user.setLongitude(Double.valueOf(accountData.getLongitude()));
				changed = true;
				peopleRecommenderBean.initLocationRecommend();
			} catch (NumberFormatException nfe) {
				logger.debug("Can not convert to double. " + nfe);
			}
		}

		if (changed) {
			loggedUser.setUser(userManager.saveEntity(user));
			try {
				eventFactory.generateEvent(EventType.Edit_Profile, loggedUser.getUser());
			} catch (EventException e) {
				logger.error(e);
			}
			PageUtil.fireSuccessfulInfoMessage(":profileForm:profileFormGrowl", "Profile information updated!");
			
			initializeAccountData();
			asyncUpdateUserDataInSocialActivities(accountData);
		}
	}
	
	public void initializeDepartments() {
		if (departments == null) {
			List<OrganizationalUnit> departmentsList = (List<OrganizationalUnit>) orgManager.getAllUnitsOfOrganization(loggedUser.getUser().getOrganization());
			Collections.sort(departmentsList, new NodeTitleComparator());
			
			departments = new OrganizationalUnit[departmentsList.size()];
			
			for (int i = 0; i < departmentsList.size(); i++) {
				departments[i] = departmentsList.get(i);
			}
		}
	}
	
	public void handleFileUpload(FileUploadEvent event) {
		try {
			UploadedFile uploadedFile = event.getFile();
			String relativePath = avatarProcessor.storeTempAvatar(loggedUser.getUser(), uploadedFile.getInputstream(), uploadedFile.getFileName(), 300, 300);
			newAvatar = Settings.getInstance().config.fileManagement.urlPrefixFolder + relativePath;
		} catch (IOException ioe) {
			logger.error(ioe.getMessage());
			
			PageUtil.fireErrorMessage("The file was not uploaded!");
		}
	}
	
	private void asyncUpdateUserDataInSocialActivities(final AccountData accountData) {
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				long userId = accountData.getId();

				Map<Long, HttpSession> userSessions = applicationBean.getAllHttpSessions();
				
				for (Entry<Long, HttpSession> userSessionMap : userSessions.entrySet()) {
					
					if (userSessionMap != null) {
						HttpSession userSession = userSessionMap.getValue();
						
						// updating Status Wall data
						ActivityWallBean activityWallBean = (ActivityWallBean) userSession.getAttribute("activitywall");
						
						if (activityWallBean != null) {
							if (activityWallBean.getAllActivities() != null) {
								updateSocialActivities(accountData, userId,	activityWallBean.getAllActivities());
							}
						}
						
						// updating Goal Wall data
						GoalWallBean goalWallBean = (GoalWallBean) userSession.getAttribute("goalwall");
						
						if (goalWallBean != null) {
							if (goalWallBean.getAllActivities() != null) {
								updateSocialActivities(accountData, userId,	goalWallBean.getAllActivities());
							}
						}
						
						// updating goal maker data
						LearnBean learningGoalsBean = (LearnBean) userSession.getAttribute("learninggoals");
						
						if (learningGoalsBean != null) {
							for (GoalDataCache goalDataCache : learningGoalsBean.getGoals()) {
								if (goalDataCache.getData().getCreator() != null && 
										goalDataCache.getData().getCreator().getId() == userId) {
									updateUserData(accountData, goalDataCache.getData().getCreator());
									
									
									// updating comments on activities
									if (goalDataCache.isCompetencesInitialized()) {
										List<CompetenceDataCache> competences = goalDataCache.getCompetences();
									
										for (CompetenceDataCache competenceDataCache : competences) {
											List<ActivityWallData> activities = competenceDataCache.getActivities();
											
											if (activities != null) {
												for (ActivityWallData activityWallData : activities) {
													if (activityWallData.getComments() != null) {
														List<SocialActivityCommentData> comments = activityWallData.getComments();
														
														for (SocialActivityCommentData activityCommentData : comments) {
															updateUserData(accountData, activityCommentData.getMaker());
														}
													}
												}
											}
										}
									}
								}
							}
						}
						
						// updating notification data
						TopNotificationsBean topNotification = (TopNotificationsBean) userSession.getAttribute("topNotificationsBean");
						
						if (topNotification != null) {
							List<NotificationData> notifications = topNotification.getNotifications();
							
							if (notifications != null) {
								for (NotificationData notificationData : notifications) {
									updateUserData(accountData, notificationData.getActor());
								}
							}
						}
						
						// updating messages data
						TopInboxBean topInbox = (TopInboxBean) userSession.getAttribute("topInboxBean");
						
						if (topInbox != null) {
							List<MessagesThreadData> messages = topInbox.getMessagesThreads();
							
							if (messages != null) {
								for (MessagesThreadData messageData : messages) {
									updateUserData(accountData, messageData.getLatest().getActor());
								}
							}
						}
					}
				}
			}

		});
	}
	
	private void updateSocialActivities(final AccountData accountData,
			long userId, List<SocialActivityData> activities) {
		
		for (SocialActivityData socialActivityData : activities) {
			
			updateUserData(accountData, socialActivityData.getActor());
			
			if (socialActivityData.getComments() != null) {
				for (SocialActivityCommentData commentData : socialActivityData.getComments()) {
					updateUserData(accountData, commentData.getMaker());
				}
			}
		}
	}
	
	private void updateUserData(AccountData accountData, UserData actor) {
		if (actor != null && actor.getId() == accountData.getId()) {
			actor.setName(accountData.getFirstName() + " " + accountData.getLastName());
			actor.setAvatarUrl(accountData.getAvatarPath());
			actor.setPosition(accountData.getPosition());
		}
	}
	
	private String newAvatar;
	private String cropCoordinates;
	
	public void crop() {
		String[] cropCoords = cropCoordinates.split("_");
		
		int top = Integer.parseInt(cropCoords[0]);
		int left = Integer.parseInt(cropCoords[1]);
		int width = Integer.parseInt(cropCoords[2]);
		int height = Integer.parseInt(cropCoords[3]);
		String imagePath = AvatarUtils.getPathFromUrl(newAvatar);
		
		if (width == 0 || height == 0) {
			logger.debug("Width or height are zaro.");
			PageUtil.fireErrorMessage(":profileForm:profileFormGrowl", "Width and height need to be higher than zero.");
			return;
		}
		
		try {
			String newAvatarPath = avatarProcessor.cropImage(loggedUser.getUser(), imagePath, left, top, width, height);
			User updatedUser = userManager.changeAvatar(loggedUser.getUser(), newAvatarPath);
			
			loggedUser.setUser(updatedUser);
			loggedUser.initializeAvatar();
			
			accountData.setAvatarPath(loggedUser.getBigAvatar());
			
			newAvatar = null;
			
			PageUtil.fireSuccessfulInfoMessage(":profileForm:profileFormGrowl", "Profile photo updated!");
			
			initializeAccountData();
			asyncUpdateUserDataInSocialActivities(accountData);
		} catch (IOException e) {
			logger.error(e);
			PageUtil.fireErrorMessage(":profileForm:profileFormGrowl", "There was an error changing profile photo.");
		}
	}
	
	public void cancelCrop() {
		newAvatar = null;
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	
	public AccountData getAccountData() {
		return accountData;
	}

	public OrganizationalUnit[] getDepartments() {
		return departments;
	}

	public OrganizationalUnit getTempDepartment() {
		return tempDepartment;
	}
	
	public void setTempDepartment(OrganizationalUnit tempDepartment) {
		this.tempDepartment = tempDepartment;
	}

	public String getNewAvatar() {
		return newAvatar;
	}
	
	public String getCropCoordinates() {
		return cropCoordinates;
	}

	public void setCropCoordinates(String cropCoordinates) {
		this.cropCoordinates = cropCoordinates;
	}
}
