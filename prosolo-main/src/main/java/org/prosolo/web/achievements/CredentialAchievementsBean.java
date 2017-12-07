package org.prosolo.web.achievements;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.achievements.data.TargetCredentialData;
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
	
	private List<TargetCredentialData> targetCredential1List;
	private List<TargetCredentialData> targetCredential1ListInProgress;

	public void initCompletedCredentials() {
		try {
			targetCredential1List = credentialManager.getAllCompletedCredentials(
					loggedUser.getUserId(), 
					false);
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage(ResourceBundleUtil.getMessage("label.credential") + " data could not be loaded!");
			logger.error("Error while loading target credentials with progress == 100 Error:\n" + e);
		}
	}

	public void initInProgressCredentials() {
		try {
			targetCredential1ListInProgress = credentialManager
					.getAllInProgressCredentials(loggedUser.getUserId(), false);
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage(ResourceBundleUtil.getMessage("label.credential") +" data could not be loaded!");
			logger.error("Error while loading target credentials with progress < 100 Error:\n" + e);
		}
	}

	public void hideCompletedTargetCredentialFromProfile(Long id) {
		boolean hideFromProfile = false;
		for (TargetCredentialData data : targetCredential1List) {
			if (data.getId() == id) {
				hideFromProfile = data.isHiddenFromProfile();
				break;
			}
		}
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
		boolean hideFromProfile = false;
		for (TargetCredentialData data : targetCredential1ListInProgress) {
			if (data.getId() == id) {
				hideFromProfile = data.isHiddenFromProfile();
				break;
			}
		}
		String hiddenOrShown = hideFromProfile ? "hidden from" : "displayed in";
		
		try {
			credentialManager.updateHiddenTargetCredentialFromProfile(id, hideFromProfile);
			PageUtil.fireSuccessfulInfoMessage("The " + ResourceBundleUtil.getMessage("label.credential").toLowerCase() + " will be " + hiddenOrShown + " your profile");
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage("Error updating " + ResourceBundleUtil.getMessage("label.credential").toLowerCase() + " visibility");
			logger.error("Error updating credential visibility in a profile!\n" + e);
		}

	}

	public List<TargetCredentialData> getTargetCredential1List() {
		return targetCredential1List;
	}

	public void setTargetCredential1List(List<TargetCredentialData> targetCredential1List) {
		this.targetCredential1List = targetCredential1List;
	}

	public List<TargetCredentialData> getTargetCredential1ListInProgress() {
		return targetCredential1ListInProgress;
	}

	public void setTargetCredential1ListInProgress(List<TargetCredentialData> targetCredential1ListInProgress) {
		this.targetCredential1ListInProgress = targetCredential1ListInProgress;
	}
}
