package org.prosolo.web.goals;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.web.goals.cache.GoalDataCache;
import org.prosolo.web.goals.competences.ActivitiesRecommendationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "learn")
@Component("learn")
@Scope("view")
public class LearnViewScopedBean implements Serializable {
	
	private static final long serialVersionUID = -8476292757990454327L;

	private static Logger logger = Logger.getLogger(LearnViewScopedBean.class);

	@Autowired private LearningGoalsBean learningGoalsBean;
	@Autowired private ActivitiesRecommendationBean activitiesRecommendationBean;
	
	// PARAMETERS
	private long id;
	private long targetCompId;
	
	public void init() {
		logger.info("Initializing managed bean " + this.getClass().getSimpleName());
		learningGoalsBean.resetNewGoalFormData();
		if (id > 0) {			
			GoalDataCache dataForGoal = learningGoalsBean.getData().getDataForTargetGoal(id);
			if (dataForGoal != null) {
				learningGoalsBean.setSelectedGoalData(dataForGoal);
			}
		} else {
			if (!learningGoalsBean.getData().getGoals().isEmpty()){
				learningGoalsBean.selectGoal(learningGoalsBean.getGoals().get(0));
			}
		}
		if (targetCompId > 0) {
			if (learningGoalsBean.getSelectedGoalData() != null) {
				learningGoalsBean.getSelectedGoalData().selectCompetence(targetCompId);
				activitiesRecommendationBean.setCompData(learningGoalsBean.getSelectedGoalData().getSelectedCompetence());
				activitiesRecommendationBean.initializeActivities();
			}
		}
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		logger.debug("setId " +  id);
		this.id = id;
	}
	
	public long getTargetCompId() {

		return targetCompId;
	}
	
	public void setGoalIdString(String goalIdString) {
		logger.debug("setGoalIdString " +  goalIdString);
		if (goalIdString != null && !goalIdString.isEmpty()) {
			try {
				setId(Long.parseLong(goalIdString));
			} catch (NumberFormatException nfe) {
				logger.debug("Id passed for goal id is not long. Value is: "+goalIdString);
			}
		}
	}

	public String getGoalIdString() {
		return String.valueOf(id);
	}
	
	public void setTargetCompIdString(String targetCompId) {
		if (targetCompId != null && !targetCompId.isEmpty()) {
			try {
				setTargetCompId(Long.parseLong(targetCompId));
			} catch (NumberFormatException nfe) {
				logger.debug("Id passed for goal id is not long. Value is: "+targetCompId);
			}
		}
	}
	
	public String getTargetCompIdString() {
		return String.valueOf(targetCompId);
	}
	
	public void setTargetCompId(long targetCompId) {
		this.targetCompId = targetCompId;
	}

}
