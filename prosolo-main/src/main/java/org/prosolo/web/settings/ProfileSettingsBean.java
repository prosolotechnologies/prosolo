/**
 * 
 */
package org.prosolo.web.settings;

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
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.SocialNetworksManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.twitter.UserOauthTokensManager;
import org.prosolo.services.upload.AvatarProcessor;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.datatopagemappers.AccountDataToPageMapper;
import org.prosolo.web.datatopagemappers.SocialNetworksDataToPageMapper;
import org.prosolo.web.profile.data.SocialNetworkAccountData;
import org.prosolo.web.profile.data.SocialNetworksData;
import org.prosolo.web.settings.data.AccountData;
import org.prosolo.web.util.AvatarUtils;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

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
					eventFactory.generateEvent(EventType.Edit_Profile, loggedUser.getUserContext(),
							null, null, null, null);
				} catch (EventException e) {
					logger.error(e);
				}
	
				init();
				//asyncUpdateUserDataInSocialActivities(accountData);
			}
			PageUtil.fireSuccessfulInfoMessage("Changes have been saved");
		} catch (ResourceCouldNotBeLoadedException e1) {
			logger.error(e1);
			PageUtil.fireErrorMessage("There was a problem saving the changes");
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

			PageUtil.fireSuccessfulInfoMessage(":profileForm:profileFormGrowl", "The profile photo has been updated");

			init();
		} catch (IOException | ResourceCouldNotBeLoadedException e) {
			logger.error(e);
			PageUtil.fireErrorMessage(":profileForm:profileFormGrowl", "There was an error changing the profile photo.");
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
					eventFactory.generateEvent(EventType.UpdatedSocialNetworks, loggedUser.getUserContext(),
							null, null, null, null);
				} catch (EventException e) {
					logger.error(e);
				}
			}
			PageUtil.fireSuccessfulInfoMessage("Social networks have been updated");
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage("There was an error updating social networks");
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
