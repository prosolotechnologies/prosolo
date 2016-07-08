package org.prosolo.web.goals;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.RecommendationType;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.nodes.NodeRecommendationManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.home.MessagesBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "recommendLearningGoalBean")
@Component("recommendLearningGoalBean")
@Scope("view")
public class RecommendLearningGoalBean extends MessagesBean {
	
	private static final long serialVersionUID = 5363673157968015524L;
	
	private static Logger logger = Logger.getLogger(RecommendLearningGoalBean.class);

	@Autowired private UserManager userManager;
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private NodeRecommendationManager nodeRecommendation;
	@Autowired private LearnBean learningGoalsBean;
	
	private long id;
	private String title;

	//private LearningGoal learningGoal;
	
	private UserData receiver;
	
	public void init(UserData userData) {
//		try {
			this.receiver = userData;
			this.setId(learningGoalsBean.getSelectedGoalData().getData().getGoalId());
			this.setTitle(learningGoalsBean.getSelectedGoalData().getData().getTitle());
//			User userToRecommend = userManager.loadResource(User.class, userData.getId());
//			getReceivers().clear();
//			getReceivers().add(userToRecommend);
			messageContent = "I would like to recommend you this learning goal";
//			this.addReceiver(userToRecommend);
//		} catch (ResourceCouldNotBeLoadedException e) {
//			e.printStackTrace();
//		}
	}

	public void sendLearningGoalRecommendation() {
  	long resourceId = learningGoalsBean.getSelectedGoalData().getData().getGoalId();
		LearningGoal learningGoal=null;
		try {
			 learningGoal=userManager.loadResource(LearningGoal.class, resourceId);
		} catch (ResourceCouldNotBeLoadedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
//		for (User recommendedTo : getReceivers()) {
//			try {
//				nodeRecommendation.sendRecommendation(
//						loggedUser.getUserId(), 
//						receiver.getId(), 
//						learningGoal, 
//						RecommendationType.USER);
				
				PageUtil.fireSuccessfulInfoMessage("You have sent recommendation to " + receiver.getName());
//			} catch (ResourceCouldNotBeLoadedException e) {
//				logger.error(e);
//			}
//		}
	}
	
	/*
	 * GETTERS / SETTERS
	 */
//	public LearningGoal getLearningGoal() {
//		return learningGoal;
//	}
//
//	public void setLearningGoal(LearningGoal learningGoal) {
//		this.learningGoal = learningGoal;
//	}

	public UserData getReceiver() {
		return receiver;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
}
