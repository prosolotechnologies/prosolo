package org.prosolo.web.dialogs;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.web.goals.cache.CompetenceDataCache;
import org.prosolo.web.goals.competences.CompetencesBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "deleteCompetenceDialog")
@Component("deleteCompetenceDialog")
@Scope("view")
public class DeleteCompetenceDialogBean implements Serializable {
	
	private static final long serialVersionUID = -3961506516951412446L;

	private static Logger logger = Logger.getLogger(DeleteCompetenceDialogBean.class);

	@Autowired private CompetencesBean competencesBean;

	private boolean deleteActivities = true;
	private CompetenceDataCache compData;
	
	@PostConstruct
	public void init() {
		logger.debug("Initializing managed bean " + this.getClass().getSimpleName());
	}
	
	public void init(CompetenceDataCache targetComp) {
		this.compData = targetComp;
	}
	
	/*
	 * ACTIONS
	 */
	
	public void deleteCompetence() {
		if (compData != null) {
			competencesBean.deleteCompetence(compData);
		}
	}
	
	/*
	 * GETTERS / SETTERS
	 */

	public boolean isDeleteActivities() {
		return deleteActivities;
	}

	public void setDeleteActivities(boolean deleteActivities) {
		this.deleteActivities = deleteActivities;
	}

}
