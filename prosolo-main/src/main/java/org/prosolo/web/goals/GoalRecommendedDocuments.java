package org.prosolo.web.goals;

import java.io.Serializable;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.recommendation.impl.RecommendedDocument;
import org.prosolo.web.LoggedUserBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
zoran
 */
@ManagedBean(name="goalRecommendedDocuments")
@Component("goalRecommendedDocuments")
@Scope("session")
public class GoalRecommendedDocuments implements Serializable {
	
	private static final long serialVersionUID = 4899283893691955786L;

	private static Logger logger = Logger.getLogger(GoalRecommendedDocuments.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private  LearningGoalsBean goalBean;
	
//	public boolean getHasGoalRecommendedDocuments(){
//		return goalBean.getSelectedGoalData().getDocuments().size() > 0;
//	}
	
	@PostConstruct
	public void init() {
		logger.debug("Initializing managed bean " + this.getClass().getSimpleName());
	}
	
	public boolean getHasMoreGoalRecommendedDocuments() {
		if (goalBean.getSelectedGoalData() != null && goalBean.getSelectedGoalData().getDocuments() != null) {
			return goalBean.getSelectedGoalData().getDocuments().size() > Settings.getInstance().config.application.defaultSideBoxElementsNumber;
		} else {
			return false;
		}
		
	}
	
	public int getGoalRecommendedDocumentsSize(){
		if (goalBean.getSelectedGoalData() == null)
			return 0;
		
		Collection<RecommendedDocument> documents = goalBean.getSelectedGoalData().getDocuments();
		
		if (documents.size() > Settings.getInstance().config.application.defaultSideBoxElementsNumber) {
			return Settings.getInstance().config.application.defaultSideBoxElementsNumber;
		} else {
			return documents.size();
		}
	}

	/*
	 * GETTERS / SETTERS
	 */
	public Collection<RecommendedDocument> getDocuments() {
		if (goalBean.getSelectedGoalData() != null) {
			return goalBean.getSelectedGoalData().getDocuments();
		}
		return null;
	}

}
