package org.prosolo.web.achievements;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.achievements.data.CredentialAchievementsData;
import org.prosolo.web.achievements.data.TargetCredentialData;
import org.prosolo.web.datatopagemappers.CredentialAchievementsDataToPageMapper;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author "Musa Paljos"
 * 
 */
@ManagedBean(name = "targetCredentialBean")
@Component("targetCredentialBean")
@Scope("view")
public class TargetCredentialBean implements Serializable {

	private static final long serialVersionUID = 1649841825780123183L;

	protected static Logger logger = Logger.getLogger(TargetCredentialBean.class);

	@Autowired
	CredentialManager credentialManager;
	@Autowired
	private LoggedUserBean loggedUser;
	@Autowired
	private UrlIdEncoder idEncoder;
	private CredentialAchievementsData completedCredentialAchievementsData;
	private CredentialAchievementsData inProgressCredentialAchievementsData;

	public void initCompletedCredentials() {
		try {
			List<TargetCredential1> targetCredential1List = credentialManager
					.getAllCompletedCredentials(loggedUser.getUserId());
			completedCredentialAchievementsData = new CredentialAchievementsDataToPageMapper(idEncoder)
					.mapDataToPageObject(targetCredential1List);
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage("Credential data could not be loaded!");
			logger.error("Error while loading target credentials with progress == 100 Error:\n" + e);
		}
	}

	public void initInProgressCredentials() {
		try {
			List<TargetCredential1> targetCredential1List = credentialManager
					.getAllInProgressCredentials(loggedUser.getUserId());
			inProgressCredentialAchievementsData = new CredentialAchievementsDataToPageMapper(idEncoder)
					.mapDataToPageObject(targetCredential1List);
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage("Credential data could not be loaded!");
			logger.error("Error while loading target credentials with progress < 100 Error:\n" + e);
		}
	}

	public void hideCompletedTargetCredentialFromProfile(Long id) {
		TargetCredentialData data = completedCredentialAchievementsData.getTargetCredentialDataByid(id);
		boolean hideFromProfile = data.isHiddenFromProfile();
		String hiddenOrShown = hideFromProfile ? "shown in" : "hidden from";
		try {
			credentialManager.updateHiddenTargetCredentialFromProfile(id, hideFromProfile);
			PageUtil.fireSuccessfulInfoMessage("Credential is successfully " + hiddenOrShown + " profile.");
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage("Error while hidding redential from profile!");
			logger.error("Error while hidding redential from profile!\n" + e);
		}
	}

	public void hideInProgressTargetCredentialFromProfile(Long id) {
		TargetCredentialData data = inProgressCredentialAchievementsData.getTargetCredentialDataByid(id);
		boolean hideFromProfile = data.isHiddenFromProfile();
		String hiddenOrShown = hideFromProfile ? "shown in" : "hidden from";
		try {
			credentialManager.updateHiddenTargetCredentialFromProfile(id, hideFromProfile);
			PageUtil.fireSuccessfulInfoMessage("Credential is successfully " + hiddenOrShown + " profile.");
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage("Error while hidding redential from profile!");
			logger.error("Error while hidding redential from profile!\n" + e);
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

	public UrlIdEncoder getIdEncoder() {
		return idEncoder;
	}

	public void setIdEncoder(UrlIdEncoder idEncoder) {
		this.idEncoder = idEncoder;
	}

}
