package org.prosolo.web.achievements;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.achievements.data.CredentialAchievementsData;
import org.prosolo.web.achievements.data.TargetCredentialData;
import org.prosolo.web.datatopagemappers.CredentialAchievementsDataToPageMapper;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import java.io.Serializable;
import java.util.List;

/**
 * @author "Musa Paljos"
 * 
 */
@ManagedBean(name = "credentialAchievementsBean")
@Component("credentialAchievementsBean")
@Scope("view")
public class CredentialAchievementsBean implements Serializable {

	private static final long serialVersionUID = 1649841825780123183L;

	protected static Logger logger = Logger.getLogger(CredentialAchievementsBean.class);

	@Autowired
	private CredentialManager credentialManager;
	@Autowired
	private LoggedUserBean loggedUser;
	
	private CredentialAchievementsData completedCredentialAchievementsData;
	private CredentialAchievementsData inProgressCredentialAchievementsData;

	public void initCompletedCredentials() {
		try {
			List<TargetCredential1> targetCredential1List = credentialManager.getAllCompletedCredentials(
					loggedUser.getUserId(), 
					false);
			
			completedCredentialAchievementsData = new CredentialAchievementsDataToPageMapper()
					.mapDataToPageObject(targetCredential1List);
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage(ResourceBundleUtil.getMessage("label.credential") + " data could not be loaded!");
			logger.error("Error while loading target credentials with progress == 100 Error:\n" + e);
		}
	}

	public void initInProgressCredentials() {
		try {
			List<TargetCredential1> targetCredential1List = credentialManager
					.getAllInProgressCredentials(loggedUser.getUserId(), false);
			
			inProgressCredentialAchievementsData = new CredentialAchievementsDataToPageMapper()
					.mapDataToPageObject(targetCredential1List);
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage(ResourceBundleUtil.getMessage("label.credential") +" data could not be loaded!");
			logger.error("Error while loading target credentials with progress < 100 Error:\n" + e);
		}
	}

	public void hideCompletedTargetCredentialFromProfile(Long id) {
		TargetCredentialData data = completedCredentialAchievementsData.getTargetCredentialDataByid(id);
		boolean hideFromProfile = data.isHiddenFromProfile();
		String hiddenOrShown = hideFromProfile ? "hidden from" : "displayed in";
		
		try {
			credentialManager.updateHiddenTargetCredentialFromProfile(id, hideFromProfile);
			PageUtil.fireSuccessfulInfoMessage("The" + ResourceBundleUtil.getMessage("label.credential").toLowerCase() + " will be " + hiddenOrShown + " your profile");
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage("Error updating " + ResourceBundleUtil.getMessage("label.credential").toLowerCase() + " visibility");
			logger.error("Error while updating credential visibility in a profile!\n" + e);
		}
	}

	public void hideInProgressTargetCredentialFromProfile(Long id) {
		TargetCredentialData data = inProgressCredentialAchievementsData.getTargetCredentialDataByid(id);
		boolean hideFromProfile = data.isHiddenFromProfile();
		String hiddenOrShown = hideFromProfile ? "hidden from" : "displayed in";
		
		try {
			credentialManager.updateHiddenTargetCredentialFromProfile(id, hideFromProfile);
			PageUtil.fireSuccessfulInfoMessage("The " + ResourceBundleUtil.getMessage("label.credential").toLowerCase() + " will be " + hiddenOrShown + " your profile");
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage("Error updating " + ResourceBundleUtil.getMessage("label.credential").toLowerCase() + " visibility");
			logger.error("Error updating credential visibility in a profile!\n" + e);
		}

	}

	public CredentialAchievementsData getCompletedCredentialAchievementsData() {
		return completedCredentialAchievementsData;
	}

	public void setCompletedCredentialAchievementsData(CredentialAchievementsData completedCredentialAchievementsData) {
		this.completedCredentialAchievementsData = completedCredentialAchievementsData;
	}

	public CredentialAchievementsData getInProgressCredentialAchievementsData() {
		return inProgressCredentialAchievementsData;
	}

	public void setInProgressCredentialAchievementsData(
			CredentialAchievementsData inProgressCredentialAchievementsData) {
		this.inProgressCredentialAchievementsData = inProgressCredentialAchievementsData;
	}

}
