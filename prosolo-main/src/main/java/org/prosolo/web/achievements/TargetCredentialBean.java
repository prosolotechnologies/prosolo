package org.prosolo.web.achievements;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.services.lti.exceptions.DbConnectionException;
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
	CredentialAchievementsData credentialAchievementsData;

	@PostConstruct
	public void init() {
		try {
			List<TargetCredential1> targetCredential1List = credentialManager
					.getAllCompletedCredentials(loggedUser.getUser().getId());
			credentialAchievementsData = new CredentialAchievementsDataToPageMapper(idEncoder)
					.mapDataToPageObject(targetCredential1List);
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage("Credential data could not be loaded!");
			logger.error("Error while loading target credentials with progress == 100 Error:\n" + e);
		}
	}

	public void hideTargetCredentialFromProfile(Long id) {
		TargetCredentialData data = credentialAchievementsData.getTargetCredentialDataByid(id);
		boolean hideFromProfile = data.isHiddenFromProfile();
		try {
			credentialManager.updateHiddenTargetCredentialFromProfile(id, hideFromProfile);
			PageUtil.fireSuccessfulInfoMessage("Credential is successfully hidden from profile.");
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage("Error while hidding redential from profile!");
			logger.error("Error while hidding redential from profile!\n" + e);
		}

	}

	public CredentialAchievementsData getCredentialAchievementsData() {
		return credentialAchievementsData;
	}

	public void setCredentialAchievementsData(CredentialAchievementsData credentialAchievementsData) {
		this.credentialAchievementsData = credentialAchievementsData;
	}

	public UrlIdEncoder getIdEncoder() {
		return idEncoder;
	}

	public void setIdEncoder(UrlIdEncoder idEncoder) {
		this.idEncoder = idEncoder;
	}

}
