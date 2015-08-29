package org.prosolo.web.goals;


import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.es.MoreNodesLikeThis;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.similarity.ResourceTokenizer;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.goals.util.AvailableLearningPlanConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
@author Zoran Jeremic Mar 28, 2015
 *
 */
//@ManagedBean(name = "recommendedactivitiesbean")
//@Component("recommendedactivitiesbean")
//@Scope("session")
public class RecommendedActivitiesBean implements Serializable {
	
	private static final long serialVersionUID = -6862038702075220736L;

	private static Logger logger = Logger.getLogger(RecommendedActivitiesBean.class);
	
	@Autowired private ActivityManager activityManager;
	@Autowired private MoreNodesLikeThis mnlt;
	@Autowired private CompetenceManager competenceManager;
	@Autowired private ResourceTokenizer resourceTokenizer;
	@Autowired private AvailableLearningPlanConverter availableLearningPlanConverter;
	@Autowired private LoggedUserBean loggedUser;
	
	private TargetCompetence selectedComp;
	private boolean inCourse=false;

	public void init(long targetLearningGoalId, long targetCompetenceId) {
		System.out.println("RECOMMENDED ACTIVITIES BEAN INIT - OLD METHOD");
		try {
			TargetLearningGoal targetLearningGoal = activityManager.loadResource(TargetLearningGoal.class, targetLearningGoalId, true);
			if (targetLearningGoal.getCourseEnrollment() != null) {
				inCourse = true;
			} else {
				inCourse = false;
			}
	 
			TargetCompetence targetCompetence = activityManager.loadResource(TargetCompetence.class, targetCompetenceId, true);
			String tokenizedString = resourceTokenizer.getTokenizedStringForTargetCompetence(targetCompetence);
			Competence competence = targetCompetence.getCompetence();
			List<Long> ignoredActivities = competenceManager.getActivitiesIds(targetCompetenceId);
			init(targetCompetence);
			
			if (competence != null) {
					List<Activity> recommendedActivities = mnlt.getSuggestedActivitiesForCompetence(
						tokenizedString, 
						ignoredActivities,
						competence.getId(), 
						10);
				
				for (Activity recActivity : recommendedActivities) {
					System.out.println("Recommended:" + recActivity.getId() + " title:" + recActivity.getTitle() + " description:"
							+ recActivity.getDescription());
				}
			}
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public void init(TargetCompetence targetCompetence) {
		logger.debug("Initializing RecommendedLearningPlansBean");
		this.setSelectedComp(activityManager.merge(targetCompetence));
	}
	
	public TargetCompetence getSelectedComp() {
		return selectedComp;
	}
	
	public void setSelectedComp(TargetCompetence selectedComp) {
		this.selectedComp = selectedComp;
	}
	
	public boolean isInCourse() {
		return inCourse;
	}
}

