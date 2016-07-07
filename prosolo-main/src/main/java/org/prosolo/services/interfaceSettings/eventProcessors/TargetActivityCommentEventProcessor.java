package org.prosolo.services.interfaceSettings.eventProcessors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activitywall.old.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.old.comments.Comment;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.messaging.data.ServiceType;
import org.prosolo.services.activityWall.ActivityWallManager;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.CommentUpdater;
import org.prosolo.services.messaging.SessionMessageDistributer;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.util.StringUtils;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.goals.LearnBean;
import org.springframework.beans.factory.annotation.Autowired;

public class TargetActivityCommentEventProcessor extends InterfaceEventProcessor {

	private ActivityManager activityManager;
	private ApplicationBean applicationBean;
	
	public TargetActivityCommentEventProcessor(Session session, Event event, BaseEntity object, 
			 ActivityManager activityManager, ApplicationBean applicationBean) {
		super(session, event, object);
		this.activityManager = activityManager;
		this.applicationBean = applicationBean;
	}

	@Override
	void process() {
		Comment comment = (Comment) object;
		BaseEntity commentedResource = comment.getObject();
		updateCommentsOfActivityInCachesOfOnlineUsers(comment, (TargetActivity) commentedResource, 
				event.getActor(), session);
	}
	
	private void updateCommentsOfActivityInCachesOfOnlineUsers(Comment comment, TargetActivity activity, User user, Session session) {
		List<User> usersSubscribedToEvent = activityManager.getUsersHavingTargetActivityInLearningGoal(activity, session);
		usersSubscribedToEvent.remove(user);
		
		List<HttpSession> usersSessions = applicationBean.getHttpSessionsOfUsers(usersSubscribedToEvent);
		
    	for (HttpSession httpSession : usersSessions) {
			LearnBean learningGoalsBean = (LearnBean) httpSession.getAttribute("learninggoals");
			if (learningGoalsBean != null) {
				learningGoalsBean.getData().addCommentToActivity(activity.getId(), comment);
			}
		}
	}

}
