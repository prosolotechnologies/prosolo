package org.prosolo.services.reporting;

import static org.prosolo.common.domainmodel.activities.events.EventType.Comment;
import static org.prosolo.common.domainmodel.activities.events.EventType.Dislike;
import static org.prosolo.common.domainmodel.activities.events.EventType.Like;
import static org.prosolo.common.domainmodel.activities.events.EventType.MENTIONED;
import static org.prosolo.common.domainmodel.activities.events.EventType.SEND_MESSAGE;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.interaction.AnalyticalServiceCollector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.reporting.SocialInteractionStatisticsObserver")
public class SocialInteractionStatisticsObserver extends EventObserver {
	
	protected static Logger logger = Logger.getLogger(SocialInteractionStatisticsObserver.class);
	
	@Autowired
	private AnalyticalServiceCollector collector;

	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[] { Comment, Like, Dislike, MENTIONED, SEND_MESSAGE };
	}

	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return null;
	}

	@Override
	public void handleEvent(Event event) {
		System.out.println("SocialInteractionStatisticsObserver handling event");
		logger.info("comming in event with action: " + event.getAction());
		logger.info("comming in event with actor: " + event.getActor());
		logger.info("comming in event with object: " + event.getObject());
		logger.info("comming in event with target: " + event.getTarget());
		//TODO: to extract real course id here
		/*
		This is the list of events that should be processed
		COMMENT,Comment,NodeComment
EVALUATION,EVALUATION_REQUEST,AchievedCompetenceRequest
EVALUATION,EVALUATION_REQUEST,NodeRequest
EVALUATION,SERVICEUSE,ASK_FOR_EVALUATION_DIALOG
JOIN,JOIN_GOAL_INVITATION,NodeRequest
JOIN,JOIN_GOAL_REQUEST,NodeRequest
JOIN,SERVICEUSE,INVITE_GOAL_COLLABORATOR_DIALOG
JOIN,SERVICEUSE,REQUEST_TO_JOIN_GOAL_DIALOG
LIKE,Like,Competence
LIKE,Like,CourseSocialActivity
LIKE,Like,DefaultSocialActivity
LIKE,Like,GoalNoteSocialActivity
LIKE,Like,NodeComment
LIKE,Like,NodeSocialActivity
LIKE,Like,NodeUserSocialActivity
LIKE,Like,PostSocialActivity
LIKE,Like,ResourceActivity
LIKE,Like,SocialActivityComment
LIKE,Like,TwitterPostSocialActivity
LIKE,Like,UserSocialActivity
MESSAGE,SEND_MESSAGE,SimpleOfflineMessage
MESSAGE,SERVICEUSE,DIRECT_MESSAGE_DIALOG
MESSAGE,START_MESSAGE_THREAD,MessagesThread
MESSAGE,UPDATE_MESSAGE_THREAD,MessagesThread
MESSAGE,SERVICEUSE,INBOX
MESSAGE,SERVICEUSE,USERDIALOG
MESSAGE,SERVICEUSE,USER_DIALOG
SEARCH,SERVICEUSE,SEARCHPEOPLE
SEARCH,SERVICEUSE,SEARCH_PEOPLE
		 */
		long courseid=1;
		long source = event.getActor().getId();
		
		long target;
		if (event.getAction().equals(SEND_MESSAGE)){
			target =Long.valueOf(event.getParameters().get("user"));
		} else if (event.getAction().equals(Comment)) {
			target = Long.valueOf(event.getParameters().get("targetUserId")); 
		} else if (event.getAction().equals(Like) || event.getAction().equals(Dislike)){
			if (event.getTarget() == null) {
				return;
			}
			target = event.getTarget().getId();
		} else {
			throw new IllegalStateException("Event type " + event.getAction() + " not supported.");
		}
		
		if (source == target) return;
		
		collector.increaseSocialInteractionCount(courseid, source, target);
	}

}
