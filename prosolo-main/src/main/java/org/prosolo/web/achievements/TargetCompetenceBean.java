package org.prosolo.web.achievements;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.achievements.data.CompetenceAchievementsData;
import org.prosolo.web.achievements.data.CredentialAchievementsData;
import org.prosolo.web.achievements.data.TargetCompetenceData;
import org.prosolo.web.achievements.data.TargetCredentialData;
import org.prosolo.web.datatopagemappers.CompetenceAchievementsDataToPageMapper;
import org.prosolo.web.datatopagemappers.CredentialAchievementsDataToPageMapper;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author "Musa Paljos"
 * 
 */

@ManagedBean(name = "targetCompetenceBean")
@Component("targetCompetenceBean")
@Scope("view")
public class TargetCompetenceBean implements Serializable {

	private static final long serialVersionUID = 1649841824780123183L;

	protected static Logger logger = Logger.getLogger(TargetCompetenceBean.class);

	@Autowired
	CompetenceManager competenceManager;
	@Autowired
	private LoggedUserBean loggedUser;
	@Autowired
	private UrlIdEncoder idEncoder;
	CompetenceAchievementsData competenceAchievementsData;

	@PostConstruct
	public void init() {
		try {
			List<TargetCompetence1> targetCompetence1List = competenceManager
					.getAllCompletedCompetences(loggedUser.getUser().getId());

			competenceAchievementsData = new CompetenceAchievementsDataToPageMapper(idEncoder)
					.mapDataToPageObject(targetCompetence1List);
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage("Credential data could not be loaded!");
			logger.error("Error while loading target credentials with progress == 100 Error:\n" + e);
		}
	}

	public void hideTargetCompetenceFromProfile(Long id) {
		TargetCompetenceData data = competenceAchievementsData.getTargetCompetenceDataByid(id);
		boolean hiddenFromProfile = data.isHiddenFromProfile();
		try {
			competenceManager.updateHiddenTargetCompetenceFromProfile(id, hiddenFromProfile);
			PageUtil.fireSuccessfulInfoMessage("Competence is successfully hidden from profile.");
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage("Error while hidding competence from profile!");
			logger.error("Error while hidding competence from profile!\n" + e);
		}

	}

	public CompetenceManager getCompetenceManager() {
		return competenceManager;
	}

	public void setCompetenceManager(CompetenceManager competenceManager) {
		this.competenceManager = competenceManager;
	}

	public UrlIdEncoder getIdEncoder() {
		return idEncoder;
	}

	public void setIdEncoder(UrlIdEncoder idEncoder) {
		this.idEncoder = idEncoder;
	}

	public CompetenceAchievementsData getCompetenceAchievementsData() {
		return competenceAchievementsData;
	}

	public void setCompetenceAchievementsData(CompetenceAchievementsData competenceAchievementsData) {
		this.competenceAchievementsData = competenceAchievementsData;
	}

}
