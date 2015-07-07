package org.prosolo.web.portfolio;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.organization.Visible;
import org.prosolo.common.domainmodel.portfolio.AchievedCompetence;
import org.prosolo.common.domainmodel.portfolio.ExternalCredit;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.services.event.EventException;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.nodes.VisibilityManager;
import org.prosolo.services.nodes.exceptions.VisibilityCoercionError;
import org.prosolo.services.nodes.util.VisibilityUtil;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.data.GoalData;
import org.prosolo.web.dialogs.data.ExternalCreditData;
import org.prosolo.web.goals.LearningGoalsBean;
import org.prosolo.web.goals.cache.CompetenceDataCache;
import org.prosolo.web.portfolio.data.AchievedCompetenceData;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name="portfolioutil")
@Component("portfolioutil")
@Scope("view")
public class PortfolioUtilBean implements Serializable {

	private static final long serialVersionUID = -2247565235943266676L;

	private static Logger logger = Logger.getLogger(PortfolioUtilBean.class);
	
	@Autowired private LearningGoalsBean learningGoalsBean;
	@Autowired private VisibilityManager visibilityManager;
	@Autowired private DefaultManager defaultManager;
	@Autowired private LoggedUserBean loggedUser;
	
	public void changeNodeVisibility(Visible visibleResourceData) {
		String newVisibility = PageUtil.getPostParameter("visType");
		String context = PageUtil.getPostParameter("context");
		
		VisibilityType visibility;
		
		try {
			visibility = VisibilityUtil.parseVisibilityType(newVisibility);
		} catch (VisibilityCoercionError e1) {
			logger.error(e1);
			PageUtil.fireErrorMessage("Error changig visibility");
			return;
		}
		
		Visible visibleRes = null;
	
		try {
			if (visibleResourceData instanceof AchievedCompetenceData) {
				AchievedCompetenceData achievedComData = (AchievedCompetenceData) visibleResourceData;
				
				// check if it is for the completed Competence
				if (achievedComData.getId() > 0) {
					visibleRes = defaultManager.loadResource(AchievedCompetence.class, achievedComData.getId());
				}
				// it is for ongoing competence
				else if (achievedComData.getTargetCompetenceId() > 0) {
					visibleRes = defaultManager.loadResource(TargetCompetence.class, achievedComData.getTargetCompetenceId());
					
					CompetenceDataCache compData = learningGoalsBean.getData().getDataForTargetCompetence(achievedComData.getTargetCompetenceId());
					
					if (compData != null) {
						compData.getData().setVisibility(visibility);
					}
				}
			} else if (visibleResourceData instanceof GoalData) {
				GoalData goalData = (GoalData) visibleResourceData;
				
				visibleRes = defaultManager.loadResource(TargetLearningGoal.class, goalData.getTargetGoalId());
			} else if (visibleResourceData instanceof ExternalCreditData) {
				ExternalCreditData exCreditData = (ExternalCreditData) visibleResourceData;
				
				visibleRes = defaultManager.loadResource(ExternalCredit.class, exCreditData.getExternalCredit().getId());
			}
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
		
		
		try {
			if (visibleRes != null) {
				visibilityManager.setResourceVisibility(loggedUser.getUser(), visibleRes, visibility, context);

				visibleResourceData.setVisibility(visibility);
				
				PageUtil.fireSuccessfulInfoMessage("Visibility updated.");
			}
		} catch (EventException e) {
			logger.error(e);
		}
	}
	
}