package org.prosolo.web.achievements;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.achievements.data.CompetenceAchievementsData;
import org.prosolo.web.achievements.data.TargetCompetenceData;
import org.prosolo.web.datatopagemappers.CompetenceAchievementsDataToPageMapper;
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

@ManagedBean(name = "competenceAchievementsBean")
@Component("competenceAchievementsBean")
@Scope("view")
public class CompetenceAchievementsBean implements Serializable {

	private static final long serialVersionUID = 1649841824780123183L;

	protected static Logger logger = Logger.getLogger(CompetenceAchievementsBean.class);

	@Autowired
	private Competence1Manager competenceManager;
	@Autowired
	private LoggedUserBean loggedUser;

	private CompetenceAchievementsData competenceAchievementsData;

	public void init() {
		try {
			List<TargetCompetence1> targetCompetence1List = competenceManager.getAllCompletedCompetences(
					loggedUser.getUserId(),
					false);

			competenceAchievementsData = new CompetenceAchievementsDataToPageMapper()
					.mapDataToPageObject(targetCompetence1List);
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage("Competency data could not be loaded!");
			logger.error("Error while loading target competencies with progress == 100 Error:\n" + e);
		}
	}

	public void hideTargetCompetenceFromProfile(Long id) {
		TargetCompetenceData data = competenceAchievementsData.getTargetCompetenceDataByid(id);
		boolean hiddenFromProfile = data.isHiddenFromProfile();
	
		try {
			competenceManager.updateHiddenTargetCompetenceFromProfile(id, hiddenFromProfile);
			String hiddenOrShown = hiddenFromProfile ? "hidden from" : "shown in";
			PageUtil.fireSuccessfulInfoMessage("Competency is successfully " + hiddenOrShown + " profile.");
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage("Error while updating competency visibility in a profile");
			logger.error("Error while updating competency visibility in a profile!\n" + e);
		}
	}

	public CompetenceAchievementsData getCompetenceAchievementsData() {
		return competenceAchievementsData;
	}

}
