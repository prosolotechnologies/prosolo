package org.prosolo.web.goals;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.services.urlencoding.UrlIdEncoder;
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

	@Autowired private LearnBean learningGoalsBean;
	@Autowired private ActivitiesRecommendationBean activitiesRecommendationBean;
	@Inject private UrlIdEncoder idEncoder;
	
	// PARAMETERS
	private String id;
	private String targetCompId;
	
	private long decodedId;
	private long decodedTargetCompId;
	
	
	public void init() {
		logger.info("Initializing managed bean " + this.getClass().getSimpleName());
		learningGoalsBean.resetNewGoalFormData();
		
		decodedId = idEncoder.decodeId(id);
		decodedTargetCompId = idEncoder.decodeId(targetCompId);
		
		if (decodedId > 0) {			
			GoalDataCache dataForGoal = learningGoalsBean.getData().getDataForTargetGoal(decodedId);
			if (dataForGoal != null) {
				learningGoalsBean.setSelectedGoalData(dataForGoal);
			}
		} else {
			learningGoalsBean.initializeGoals();
			if (!learningGoalsBean.getData().getGoals().isEmpty()){
				learningGoalsBean.selectGoal(learningGoalsBean.getGoals().get(0));
			}
		}
		if (decodedTargetCompId > 0) {
			if (learningGoalsBean.getSelectedGoalData() != null) {
				learningGoalsBean.getSelectedGoalData().selectCompetence(decodedTargetCompId);
				activitiesRecommendationBean.setCompData(learningGoalsBean.getSelectedGoalData().getSelectedCompetence());
				activitiesRecommendationBean.initializeActivities();
			}
		}
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		logger.debug("setId " +  id);
		this.id = id;
	}
	
	public String getTargetCompId() {

		return targetCompId;
	}
	
	public void setTargetCompId(String targetCompId){
		this.targetCompId = targetCompId;
	}

	public long getDecodedId() {
		return decodedId;
	}

	public void setDecodedId(long decodedId) {
		this.decodedId = decodedId;
	}

	public long getDecodedTargetCompId() {
		return decodedTargetCompId;
	}

	public void setDecodedTargetCompId(long decodedTargetCompId) {
		this.decodedTargetCompId = decodedTargetCompId;
	}
	
	
	
	/*public void setGoalIdString(String goalIdString) {
		logger.debug("setGoalIdString " +  goalIdString);
		if (goalIdString != null && !goalIdString.isEmpty()) {
			try {
				setId(Long.parseLong(goalIdString));
			} catch (NumberFormatException nfe) {
				logger.debug("Id passed for goal id is not long. Value is: "+goalIdString);
			}
		}
	}*/

	/*public String getGoalIdString() {
		return String.valueOf(id);
	}*/
	
	/*public void setTargetCompIdString(String targetCompId) {
		if (targetCompId != null && !targetCompId.isEmpty()) {
			try {
				setTargetCompId(Long.parseLong(targetCompId));
			} catch (NumberFormatException nfe) {
				logger.debug("Id passed for goal id is not long. Value is: "+targetCompId);
			}
		}
	}*/
	
	/*public String getTargetCompIdString() {
		return String.valueOf(targetCompId);
	}*/

}
