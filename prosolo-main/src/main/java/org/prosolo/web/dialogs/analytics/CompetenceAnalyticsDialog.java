package org.prosolo.web.dialogs.analytics;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.web.goals.competences.CompetenceAnalyticsBean;
import org.prosolo.web.goals.data.CompetenceAnalyticsData;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "competenceAnalyticsBean")
@Component("competenceAnalyticsBean")
@Scope("view")
public class CompetenceAnalyticsDialog implements Serializable {

	private static final long serialVersionUID = 2191874428167658814L;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CompetenceAnalyticsDialog.class);
	
	@Autowired private CompetenceAnalyticsBean competenceAnalyticsBean;
	@Autowired private LoggingNavigationBean actionLogger;
	
	private CompetenceAnalyticsData data;
	
	/*
	 * ACTIONS
	 */

//	public void initialize(Competence competence) {
//		data = competenceAnalyticsBean.analyzeCompetence(competence.getId());
//	}
	
	public void initializeById(long competenceId, String context) {
		data = competenceAnalyticsBean.analyzeCompetence(competenceId);
		
		actionLogger.logServiceUse(
				ComponentName.COMPETENCE_ANALYTICS_DIALOG, 
				"context", context,
				"compId", String.valueOf(competenceId));
	}
	
	/*
	 * GETTERS / SETTERS
	 */

	public CompetenceAnalyticsData getData() {
		return data;
	}

}
