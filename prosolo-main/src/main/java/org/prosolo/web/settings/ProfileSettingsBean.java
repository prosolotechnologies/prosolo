/**
 * 
 */
package org.prosolo.web.settings;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.prosolo.app.Settings;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.socialNetworks.ServiceType;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkAccount;
import org.prosolo.common.domainmodel.user.socialNetworks.UserSocialNetworks;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.activityWall.impl.data.SocialActivityData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.SocialNetworksManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.twitter.UserOauthTokensManager;
import org.prosolo.services.upload.AvatarProcessor;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.ActivityWallBean1;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.activitywall.data.SocialActivityCommentData;
import org.prosolo.web.datatopagemappers.AccountDataToPageMapper;
import org.prosolo.web.datatopagemappers.SocialNetworksDataToPageMapper;
import org.prosolo.web.goals.GoalWallBean;
import org.prosolo.web.goals.LearnBean;
import org.prosolo.web.goals.cache.CompetenceDataCache;
import org.prosolo.web.goals.cache.GoalDataCache;
import org.prosolo.web.notification.TopNotificationsBean;
import org.prosolo.web.notification.data.NotificationData;
import org.prosolo.web.profile.data.SocialNetworkAccountData;
import org.prosolo.web.profile.data.SocialNetworksData;
import org.prosolo.web.settings.data.AccountData;
import org.prosolo.web.util.AvatarUtils;
import org.prosolo.web.util.page.PageUtil;
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

	@Inject
	private LoggedUserBean loggedUser;
	@Inject
	private UserManager userManager;
	@Inject
	private AvatarProcessor avatarProcessor;
	@Inject
	private EventFactory eventFactory;
	@Inject
	private ApplicationBean applicationBean;
	@Inject
	@Qualifier("taskExecutor")
	private ThreadPoolTaskExecutor taskExecutor;
	@Inject
	private SocialNetworksManager socialNetworksManager;
	@Inject
	private UserOauthTokensManager oauthAccessTokenManager;

	//URL PARAMS
	private boolean twitterConnected;
	
	private AccountData accountData;
	private SocialNetworksData socialNetworksData;
	private boolean connectedToTwitter;

	private UserSocialNetworks userSocialNetworks;

	public void init() {
		initAccountData();
		initSocialNetworksData();
		
		if(twitterConnected) {
			PageUtil.fireSuccessfulInfoMessage("You have connected your Twitter account with ProSolo");
		}
	}

	public void initSocialNetworksData() {
		if (socialNetworksData == null) {
			try {
				userSocialNetworks = socialNetworksManager.getSocialNetworks(loggedUser.getUserId());
				socialNetworksData = new SocialNetworksDataToPageMapper()
						.mapDataToPageObject(userSocialNetworks);
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
				PageUtil.fireErrorMessage("Error loading the data");
			}
			
			connectedToTwitter = oauthAccessTokenManager.hasOAuthAccessToken(loggedUser.getUserId(), ServiceType.TWITTER);
		}
	}

	private void initAccountData() {
		try {
			User user = userManager.loadResource(User.class, loggedUser.getUserId());
			accountData = new AccountDataToPageMapper().mapDataToPageObject(user);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}

	/*
	 * ACTIONS
	 */
	public void saveAccountChanges() {
		try {
			User user = userManager.loadResource(User.class, loggedUser.getUserId());
		
			boolean changed = false;
	
			if (!accountData.getFirstName().equals(user.getName())) {
				user.setName(accountData.getFirstName());
				changed = true;
			}
	
			if (!accountData.getLastName().equals(user.getLastname())) {
				user.setLastname(accountData.getLastName());
				changed = true;
			}
	
			if (!accountData.getPosition().equals(user.getPosition())) {
				user.setPosition(accountData.getPosition());
				changed = true;
			}
			
			if ((accountData.getLocationName() != null && user.getLocationName() == null)
					|| (!accountData.getLocationName().equals(user.getLocationName()))) {
				try {
					user.setLocationName(accountData.getLocationName());
					user.setLatitude(Double.valueOf(accountData.getLatitude()));
					user.setLongitude(Double.valueOf(accountData.getLongitude()));
					changed = true;
				} catch (NumberFormatException nfe) {
					logger.debug("Can not convert to double. " + nfe);
				}
			}
	
			if (changed) {
				userManager.saveEntity(user);
				loggedUser.reinitializeSessionData(user);
				
				try {
					String page = PageUtil.getPostParameter("page");
					String lContext = PageUtil.getPostParameter("learningContext");
					
					eventFactory.generateEvent(EventType.Edit_Profile, loggedUser.getUserId(), null, null, page, lContext, null, null);
				} catch (EventException e) {
					logger.error(e);
					PageUtil.fireErrorMessage("Changes are not saved!");
				}
	
				init();
				//asyncUpdateUserDataInSocialActivities(accountData);
			}
			PageUtil.fireSuccessfulInfoMessage("Changes are saved");
		} catch (ResourceCouldNotBeLoadedException e1) {
			logger.error(e1);
			PageUtil.fireErrorMessage("Changes are not saved!");
		}
	}

	public void handleFileUpload(FileUploadEvent event) {
		try {
			UploadedFile uploadedFile = event.getFile();
			String relativePath = avatarProcessor.storeTempAvatar(loggedUser.getUserId(), uploadedFile.getInputstream(),
					uploadedFile.getFileName(), 300, 300);
			newAvatar = CommonSettings.getInstance().config.appConfig.domain + Settings.getInstance().config.fileManagement.urlPrefixFolder + relativePath;
		} catch (IOException ioe) {
			logger.error(ioe.getMessage());

			PageUtil.fireErrorMessage("The file was not uploaded!");
		}
	}

	@Deprecated
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
						ActivityWallBean1 activityWallBean = (ActivityWallBean1) userSession.getAttribute("activitywall");

						if (activityWallBean != null) {
							if (activityWallBean.getAllActivities() != null) {
								updateSocialActivities(accountData, userId, activityWallBean.getAllActivities());
							}
						}

						// updating Goal Wall data
						GoalWallBean goalWallBean = (GoalWallBean) userSession.getAttribute("goalwall");

						if (goalWallBean != null) {
							if (goalWallBean.getAllActivities() != null) {
								updateSocialActivities(accountData, userId, goalWallBean.getAllActivities());
							}
						}

						// updating goal maker data
						LearnBean learningGoalsBean = (LearnBean) userSession.getAttribute("learninggoals");

						if (learningGoalsBean != null) {
							for (GoalDataCache goalDataCache : learningGoalsBean.getGoals()) {
								if (goalDataCache.getData().getCreator() != null
										&& goalDataCache.getData().getCreator().getId() == userId) {
									updateUserData(accountData, goalDataCache.getData().getCreator());

									// updating comments on activities
									if (goalDataCache.isCompetencesInitialized()) {
										List<CompetenceDataCache> competences = goalDataCache.getCompetences();

										for (CompetenceDataCache competenceDataCache : competences) {
											List<ActivityWallData> activities = competenceDataCache.getActivities();

											if (activities != null) {
												for (ActivityWallData activityWallData : activities) {
													if (activityWallData.getComments() != null) {
														List<SocialActivityCommentData> comments = activityWallData
																.getComments();

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
						TopNotificationsBean topNotification = (TopNotificationsBean) userSession
								.getAttribute("topNotificationsBean");

						if (topNotification != null) {
							List<NotificationData> notifications = topNotification.getNotifications();

							if (notifications != null) {
								for (NotificationData notificationData : notifications) {
									updateUserData(accountData, notificationData.getActor());
								}
							}
						}

//						MessagesBean messagesBean = (MessagesBean) userSession.getAttribute("messagesBean");
//
//						if (messagesBean != null) {
//							List<MessagesThreadData> messages = messagesBean.getMessagesThreads();
//
//							if (messages != null) {
//								for (MessagesThreadData messageData : messages) {
//									updateUserData(accountData, messageData.getLatest().getActor());
//								}
//							}
//						}
					}
				}
			}

		});
	}

	private void updateSocialActivities(final AccountData accountData, long userId,
			List<SocialActivityData> activities) {

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
			String newAvatarPath = avatarProcessor.cropImage(loggedUser.getUserId(), imagePath, left, top, width, height);
			User updatedUser = userManager.changeAvatar(loggedUser.getUserId(), newAvatarPath);

			loggedUser.getSessionData().setAvatar(updatedUser.getAvatarUrl());
			loggedUser.initializeAvatar();

			accountData.setAvatarPath(loggedUser.getAvatar());

			newAvatar = null;

			PageUtil.fireSuccessfulInfoMessage(":profileForm:profileFormGrowl", "Profile photo updated!");

			init();
			asyncUpdateUserDataInSocialActivities(accountData);
		} catch (IOException | ResourceCouldNotBeLoadedException e) {
			logger.error(e);
			PageUtil.fireErrorMessage(":profileForm:profileFormGrowl", "There was an error changing profile photo.");
		}
	}

	public void saveSocialNetworkChanges() {
		boolean newSocialNetworkAccountIsAdded = false;

		Map<String, SocialNetworkAccountData> newSocialNetworkAccounts = socialNetworksData.getSocialNetworkAccountsData();
		
		try {
			for (SocialNetworkAccountData socialNetowrkAccountData : newSocialNetworkAccounts.values()) {
				if (socialNetowrkAccountData.isChanged()) {
					SocialNetworkAccount account;
					if (socialNetowrkAccountData.getId() == 0) {
						account = socialNetworksManager.createSocialNetworkAccount(
								socialNetowrkAccountData.getSocialNetworkName(),
								socialNetowrkAccountData.getLinkEdit());
						userSocialNetworks.getSocialNetworkAccounts().add(account);
						newSocialNetworkAccountIsAdded = true;
					} else {
						try {
							socialNetworksManager.updateSocialNetworkAccount(socialNetowrkAccountData.getId(), socialNetowrkAccountData.getLinkEdit());
							socialNetowrkAccountData.setLink(socialNetowrkAccountData.getLinkEdit());
							socialNetowrkAccountData.setChanged(false);
						} catch (ResourceCouldNotBeLoadedException e) {
							logger.error(e);
						}
					}
				}
			}
			
			if (newSocialNetworkAccountIsAdded) {
				socialNetworksManager.saveEntity(userSocialNetworks);
				try {
					eventFactory.generateEvent(EventType.UpdatedSocialNetworks, loggedUser.getUserId());
				} catch (EventException e) {
					logger.error(e);
				}
			}
			PageUtil.fireSuccessfulInfoMessage("Social networks updated!");
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage("There was an error changing profile photo.");
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

	public String getNewAvatar() {
		return newAvatar;
	}

	public String getCropCoordinates() {
		return cropCoordinates;
	}

	public void setCropCoordinates(String cropCoordinates) {
		this.cropCoordinates = cropCoordinates;
	}

	public SocialNetworksData getSocialNetworksData() {
		return socialNetworksData;
	}

	public void setSocialNetworksData(SocialNetworksData socialNetworksData) {
		this.socialNetworksData = socialNetworksData;
	}

	public boolean isConnectedToTwitter() {
		return connectedToTwitter;
	}

	public void setConnectedToTwitter(boolean connectedToTwitter) {
		this.connectedToTwitter = connectedToTwitter;
	}

	public boolean isTwitterConnected() {
		return twitterConnected;
	}

	public void setTwitterConnected(boolean twitterConnected) {
		this.twitterConnected = twitterConnected;
	}
	
}
