package org.prosolo.services.interfaceSettings.eventProcessors;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activitywall.old.comments.Comment;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.Event;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.goals.LearnBean;

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
				event.getActorId(), session);
	}
	
	private void updateCommentsOfActivityInCachesOfOnlineUsers(Comment comment, TargetActivity activity, long userId, Session session) {
		List<User> usersSubscribedToEvent = activityManager.getUsersHavingTargetActivityInLearningGoal(activity, session);
		
		// removing user
		Iterator<User> userIterator = usersSubscribedToEvent.iterator();
		
		while (userIterator.hasNext()) {
			User u = (User) userIterator.next();
			
			if (u.getId() == userId) {
				userIterator.remove();
				break;
			}
		}
		
		List<HttpSession> usersSessions = applicationBean.getHttpSessionsOfUsers(usersSubscribedToEvent);
		
    	for (HttpSession httpSession : usersSessions) {
			LearnBean learningGoalsBean = (LearnBean) httpSession.getAttribute("learninggoals");
			if (learningGoalsBean != null) {
				learningGoalsBean.getData().addCommentToActivity(activity.getId(), comment);
			}
		}
	}

}
