package org.prosolo.web.goals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.recommendation.CollaboratorsRecommendation;
import org.prosolo.web.LoggedUserBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name="collaboratorRecommendBean")
@Component("collaboratorRecommendBean")
@Scope("session")
@Deprecated
public class CollaboratorRecommendationBean implements Serializable {
	
	private static final long serialVersionUID = -9140163614469291635L;
	
	private static final Logger logger = Logger.getLogger(AppendedActivitiesBean.class.getName());
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private CollaboratorsRecommendation collaboratorsRecommendation;
	
	private List<User> recommendedCollaborators = new ArrayList<User>();
	
	@PostConstruct
	public void init() {
		logger.debug("Initializing managed bean " + this.getClass().getSimpleName());
	}
	
	public void init(LearningGoal selectedGoal) {
		recommendedCollaborators.clear();
		
		if (selectedGoal != null) {
			recommendedCollaborators = collaboratorsRecommendation
					.getRecommendedCollaboratorsForLearningGoal(loggedUser.getUserId(), selectedGoal.getId(),
							Settings.getInstance().config.application.defaultLikeThisItemsNumber);
			
			if (recommendedCollaborators.size() > 2) {
				recommendedCollaborators = recommendedCollaborators.subList(0, 2);
			}
		}
	}

	public List<User> getRecommendedCollaborators() {
		return recommendedCollaborators;
	}

	public void setRecommendedCollaborators(List<User> recommendedCollaborators) {
		this.recommendedCollaborators = recommendedCollaborators;
	}
	
	public int getRecommendedUsersSize() {
		return recommendedCollaborators.size();
	}

	// TODO: delete and change on the page calling it to 'not empty hasRecommendedCollaborators' 
	public boolean hasRecommendedCollaborators() {
		return recommendedCollaborators.size() > 0;
	}

}
