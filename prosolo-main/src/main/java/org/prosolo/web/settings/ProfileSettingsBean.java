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
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.socialNetworks.ServiceType;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkAccount;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.nodes.SocialNetworksManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.factory.UserDataFactory;
import org.prosolo.services.twitter.UserOauthTokensManager;
import org.prosolo.services.upload.AvatarProcessor;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.profile.data.SocialNetworkAccountData;
import org.prosolo.web.profile.data.UserSocialNetworksData;
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
	@Qualifier("taskExecutor")
	private ThreadPoolTaskExecutor taskExecutor;
	@Inject
	private SocialNetworksManager socialNetworksManager;
	@Inject
	private UserOauthTokensManager oauthAccessTokenManager;
	@Inject
	private UserDataFactory userDataFactory;

	//URL PARAMS
	private boolean twitterConnected;
	
	private UserData accountData;
	private UserSocialNetworksData userSocialNetworksData;
	private boolean connectedToTwitter;

	public void init() {
		initAccountData();
		initSocialNetworksData();

		if(twitterConnected) {
			PageUtil.fireSuccessfulInfoMessage("You have connected your Twitter account with ProSolo");
		}
	}

	private void initAccountData() {
		accountData = userManager.getUserData(loggedUser.getUserId());
	}

	public void initSocialNetworksData() {
		try {
			userSocialNetworksData = socialNetworksManager.getUserSocialNetworkData(loggedUser.getUserId());

			connectedToTwitter = oauthAccessTokenManager.hasOAuthAccessToken(loggedUser.getUserId(), ServiceType.TWITTER);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error loading the data");
		}
	}

	/*
	 * ACTIONS
	 */
	public void saveAccountChanges() {
		try {
			userManager.saveAccountChanges(accountData, loggedUser.getUserContext());

			loggedUser.reinitializeSessionData(accountData, loggedUser.getOrganizationId());

			PageUtil.fireSuccessfulInfoMessage("Changes have been saved");
		} catch (Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while saving account data");
		}
		PageUtil.fireSuccessfulInfoMessage("Changes have been saved");
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

			accountData.setAvatarUrl(loggedUser.getAvatar());

			newAvatar = null;

			PageUtil.fireSuccessfulInfoMessage(":profileForm:profileFormGrowl", "The profile photo has been updated");

			init();
		} catch (IOException | ResourceCouldNotBeLoadedException e) {
			logger.error(e);
			PageUtil.fireErrorMessage(":profileForm:profileFormGrowl", "There was an error changing the profile photo.");
		}
	}

	public void saveSocialNetworkChanges() {

		Map<String, SocialNetworkAccountData> newSocialNetworkAccounts = userSocialNetworksData.getSocialNetworkAccountsData();
		
		try {
			for (SocialNetworkAccountData socialNetowrkAccountData : newSocialNetworkAccounts.values()) {
				if (socialNetowrkAccountData.isChanged()) {
					SocialNetworkAccount account;
					SocialNetworkAccountData userAccount = socialNetworksManager.getSocialNetworkAccountData(loggedUser.getUserId(), socialNetowrkAccountData.getSocialNetworkName());
					if (socialNetowrkAccountData.getId() == 0) {
						account = socialNetworksManager.createSocialNetworkAccount(socialNetowrkAccountData.getSocialNetworkName(),
								socialNetowrkAccountData.getLinkEdit(),
								loggedUser.getUserContext());
						userAccount = new SocialNetworkAccountData(account);
						userSocialNetworksData.getSocialNetworkAccountsData().put(userAccount.getSocialNetworkName().toString(), userAccount);
					} else {
						try {
							socialNetworksManager.updateSocialNetworkAccount(userAccount.getId(), socialNetowrkAccountData.getLinkEdit());
							socialNetowrkAccountData.setLink(socialNetowrkAccountData.getLinkEdit());
							socialNetowrkAccountData.setChanged(false);
						} catch (ResourceCouldNotBeLoadedException e) {
							logger.error(e);
						}
					}
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

	public String getNewAvatar() {
		return newAvatar;
	}

	public String getCropCoordinates() {
		return cropCoordinates;
	}

	public void setCropCoordinates(String cropCoordinates) {
		this.cropCoordinates = cropCoordinates;
	}

	public UserSocialNetworksData getUserSocialNetworksData() {
		return userSocialNetworksData;
	}

	public void setUserSocialNetworksData(UserSocialNetworksData userSocialNetworksData) {
		this.userSocialNetworksData = userSocialNetworksData;
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

	public UserData getAccountData() {
		return accountData;
	}
}
