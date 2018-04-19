package org.prosolo.web.achievements;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.services.nodes.data.competence.TargetCompetenceData;
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

	private List<TargetCompetenceData> targetCompetence1List;

	public void init() {
		try {
			targetCompetence1List = competenceManager.getAllCompletedCompetences(
					loggedUser.getUserId(),
					false);
		} catch (DbConnectionException e) {
			logger.error("Error while loading target competencies with progress == 100 Error:\n" + e);
			PageUtil.fireErrorMessage(ResourceBundleUtil.getMessage("label.competence") + " data could not be loaded!");
		}
	}

	public void hideTargetCompetenceFromProfile(Long id) {
		boolean hiddenFromProfile = false;
		for (TargetCompetenceData data : targetCompetence1List) {
			if (data.getId() == id) {
				hiddenFromProfile = data.isHiddenFromProfile();
			}
		}
	
		try {
			competenceManager.updateHiddenTargetCompetenceFromProfile(id, hiddenFromProfile);
			String hiddenOrShown = hiddenFromProfile ? "hidden from" : "displayed in";
			PageUtil.fireSuccessfulInfoMessage("The " + ResourceBundleUtil.getMessage("label.competence").toLowerCase() + " will be " + hiddenOrShown + " your profile");
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage("Error updating the " + ResourceBundleUtil.getMessage("label.competence").toLowerCase() + " visibility");
			logger.error("Error while updating competency visibility in a profile!\n" + e);
		}
	}

	public List<TargetCompetenceData> getTargetCompetence1List() {
		return targetCompetence1List;
	}
}
