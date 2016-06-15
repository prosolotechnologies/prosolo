package org.prosolo.services.notifications.eventprocessing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ObjectType;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public class LikeEventProcessing extends NotificationEventProcessor {

	private static Logger logger = Logger.getLogger(LikeEventProcessing.class);

	private Activity1Manager activityManager;
	
	private Comment1 comment;
	private ObjectType commentedResourceType;
	
	public LikeEventProcessing(Event event, Session session,
			NotificationManager notificationManager,
			NotificationsSettingsManager notificationsSettingsManager,
			Activity1Manager activityManager,
			UrlIdEncoder idEncoder) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
		this.activityManager = activityManager;
		setResource();
		setCommentedResourceType();
	}

	protected void setResource() {
		this.comment = (Comment1) session.load(event.getObject().getClass(), event.getObject().getId());
		this.comment = HibernateUtil.initializeAndUnproxy(this.comment);
	}
	
	private void setCommentedResourceType() {
		switch(comment.getResourceType()) {
			case Activity:
				commentedResourceType = ObjectType.Activity;
				break;
			case Competence:
				commentedResourceType = ObjectType.Competence;	
				break;
		}
	}

	@Override
	List<Long> getReceiverIds() {
		List<Long> users = new ArrayList<>();
		try {
			Long resCreatorId = comment.getUser().getId();
			
			users.add(resCreatorId);
		} catch(Exception e) {
			logger.error(e);
		}
		
		return users;
	}

	@Override
	long getSenderId() {
		return event.getActor().getId();
	}

	@Override
	boolean isConditionMet(long sender, long receiver) {
		if (receiver != 0 && sender != receiver) {
			return true;
		} else {
			logger.error("Commenting on the resource of a type: " + 
					comment.getClass() + " is not captured.");
			return false;
		}
	}

	@Override
	NotificationType getNotificationType() {
		return NotificationType.Comment_Like;
	}

	@Override
	ObjectType getObjectType() {
		return ObjectType.Comment;
	}

	@Override
	long getObjectId() {
		return comment.getCommentedResourceId();
	}

	@Override
	String getNotificationLink() {
		switch(commentedResourceType) {
		case Activity:
			Long compId = activityManager.getCompetenceIdForActivity(getObjectId());
			if (compId != null) {
				return "/activity.xhtml?compId=" + idEncoder.encodeId(compId) + "&actId=" 
						+ idEncoder.encodeId(getObjectId()) + "&comment=" +
								idEncoder.encodeId(comment.getId());
			}
			break;
		case Competence:
			return "/competence.xhtml?compId=" + idEncoder.encodeId(getObjectId()) + "&comment=" +
				idEncoder.encodeId(comment.getId());
		default:
			break;
		}
		return null;
	}

}
