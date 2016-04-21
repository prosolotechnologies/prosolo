/**
 * 
 */
package org.prosolo.services.notifications.impl;

import java.io.Serializable;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.evaluation.Badge;
import org.prosolo.common.domainmodel.evaluation.Evaluation;
import org.prosolo.common.domainmodel.evaluation.EvaluationSubmission;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.portfolio.AchievedCompetence;
import org.prosolo.common.domainmodel.portfolio.ExternalCredit;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.notifications.EvaluationUpdater;
import org.prosolo.web.data.GoalData;
import org.prosolo.web.dialogs.data.ExternalCreditData;
import org.prosolo.web.goals.LearnBean;
import org.prosolo.web.goals.cache.CompetenceDataCache;
import org.prosolo.web.goals.cache.GoalDataCache;
import org.prosolo.web.portfolio.PortfolioBean;
import org.prosolo.web.portfolio.data.AchievedCompetenceData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author "Nikola Milikic"
 *
 */
@Service("org.prosolo.services.notifications.EvaluationUpdater")
public class EvaluationUpdaterImpl extends AbstractManagerImpl implements EvaluationUpdater, Serializable {
	
	private static final long serialVersionUID = -2171458485849125770L;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(EvaluationUpdaterImpl.class);
	
	@Autowired private DefaultManager defaultManager;

	@Override
	public void updateEvaluationData(long evSubmissionId, HttpSession userSession, Session session) throws ResourceCouldNotBeLoadedException {
		if (userSession != null) {
			EvaluationSubmission evSubmission = defaultManager.loadResource(EvaluationSubmission.class, evSubmissionId, session);
	
			BaseEntity resource = evSubmission.getRequest().getResource();
			
			LearnBean goalsBean = (LearnBean) userSession.getAttribute("learninggoals");
			
			if (goalsBean != null) {
				if (resource instanceof TargetCompetence) {
					CompetenceDataCache compData = goalsBean.getData().getDataForTargetCompetence(resource.getId());
					
					Evaluation ev = evSubmission.getPrimaryEvaluation();
					incrementEvaluationCount(compData, ev.getBadges(), ev.isAccepted());
				} else if (resource instanceof TargetLearningGoal) {
					GoalDataCache goalData = goalsBean.getData().getDataForTargetGoal(resource.getId());
					
					if (goalData != null) {
						
						evaluationsLoop: for (Evaluation ev : evSubmission.getEvaluations()) {
							if (ev.getResource() instanceof TargetLearningGoal) {
								incrementEvaluationCount(goalData.getData(), ev.getBadges(), ev.isAccepted());
							} else if (ev.getResource() instanceof TargetCompetence) {
								
								// loop through goal's competences and look for the one evaluation is for
								for (CompetenceDataCache compData : goalData.getCompetences()) {
									if (compData.getData().getId() == ev.getResource().getId()) {
										incrementEvaluationCount(compData, ev.getBadges(), ev.isAccepted());
										
										continue evaluationsLoop;
									}
								}
							}
						}
					}
				}
			}
			
			PortfolioBean portfolioBean = (PortfolioBean) userSession.getAttribute("portfolio");
			
			if (portfolioBean != null) {
				if (resource instanceof TargetLearningGoal) {
					if (portfolioBean.getCompletedGoals() != null && !portfolioBean.getCompletedGoals().isEmpty()) {
						
						for (GoalData completedGoalData : portfolioBean.getCompletedGoals()) {
							
							if (completedGoalData.getGoalId() == resource.getId()) {
								Evaluation ev = evSubmission.getPrimaryEvaluation();
								
								incrementEvaluationCount(completedGoalData, ev.getBadges(), ev.isAccepted());
								
								break;
							}
						}
						
						evaluationsLoop: for (Evaluation ev : evSubmission.getEvaluations()) {
							if (ev.getResource() instanceof TargetCompetence) {
								
								// loop through goal's competences and look for the one evaluation is for
								for (AchievedCompetenceData achievedCompData : portfolioBean.getCompletedAchievedComps()) {
									if (achievedCompData.getCompetenceId() == ev.getResource().getId()) {
										incrementEvaluationCount(achievedCompData, ev.getBadges(), ev.isAccepted());
										
										break evaluationsLoop;
									}
								}
							}
						}
					}
				} else if (resource instanceof AchievedCompetence || resource instanceof TargetCompetence) {
					for (AchievedCompetenceData achievedCompData : portfolioBean.getCompletedAchievedComps()) {
						if (achievedCompData.getId() == resource.getId()) {
							
							Evaluation ev = evSubmission.getPrimaryEvaluation();
							incrementEvaluationCount(achievedCompData, ev.getBadges(), ev.isAccepted());
							
							break;
						}
					}
				} else if (resource instanceof ExternalCredit) {
					if (portfolioBean.getExternalCredits() != null && !portfolioBean.getExternalCredits().isEmpty()) {
						
						for (ExternalCreditData externalCreditData : portfolioBean.getExternalCredits()) {
							if (externalCreditData.getId() == resource.getId()) {
								Evaluation ev = evSubmission.getPrimaryEvaluation();
								incrementEvaluationCount(externalCreditData, ev.getBadges(), ev.isAccepted());
								
								break;
							}
						}
					}
				}
			}
		}
	}

	private void incrementEvaluationCount(CompetenceDataCache compData, Set<Badge> badges, boolean accepted) {
		if (compData != null) {
			
			if (accepted) {
				compData.setEvaluationCount(compData.getEvaluationCount() + 1);
			} else {
				compData.setRejectedEvaluationCount(compData.getRejectedEvaluationCount() + 1);
			}
			
			if (!badges.isEmpty()) {
				compData.setBadgeCount(compData.getBadgeCount() + 1);
			}
		}
	}
	
	private void incrementEvaluationCount(GoalData goalData, Set<Badge> badges, boolean accepted) {
		if (goalData != null) {
			if (accepted) {
				goalData.setEvaluationCount(goalData.getEvaluationCount() + 1);
			} else {
				goalData.setRejectedEvaluationCount(goalData.getRejectedEvaluationCount() + 1);
			}
			
			if (!badges.isEmpty()) {
				goalData.setBadgeCount(goalData.getBadgeCount() + 1);
			}
		}
	}
	
	private void incrementEvaluationCount(AchievedCompetenceData compData, Set<Badge> badges, boolean accepted) {
		if (compData != null) {
			if (accepted) {
				compData.setEvaluationCount(compData.getEvaluationCount() + 1);
			} else {
				compData.setRejectedEvaluationCount(compData.getRejectedEvaluationCount() + 1);
			}
			
			if (!badges.isEmpty()) {
				compData.setBadgeCount(compData.getBadgeCount() + 1);
			}
		}
	}
	
	private void incrementEvaluationCount(ExternalCreditData exCreditData, Set<Badge> badges, boolean accepted) {
		if (exCreditData != null) {
			if (accepted) {
				exCreditData.setEvaluationCount(exCreditData.getEvaluationCount() + 1);
			} else {
				exCreditData.setRejectedEvaluationCount(exCreditData.getRejectedEvaluationCount() + 1);
			}
			
			if (!badges.isEmpty()) {
				exCreditData.setBadgeCount(exCreditData.getBadgeCount() + 1);
			}
		}
	}
	
}
