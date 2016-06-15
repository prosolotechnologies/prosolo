package org.prosolo.services.notifications.eventprocessing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ObjectType;
import org.prosolo.services.event.Event;
import org.prosolo.services.interaction.CommentManager;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.util.roles.RoleNames;

public class CommentEventProcessing extends NotificationEventProcessor {

	private static Logger logger = Logger.getLogger(CommentEventProcessing.class);
	
	private Comment1 resource;
	private ObjectType objectType;
	private Activity1Manager activityManager;
	private CommentManager commentManager;
	private RoleManager roleManager;

	public CommentEventProcessing(Event event, Session session,
			NotificationManager notificationManager, 
			NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
			Activity1Manager activityManager, CommentManager commentManager, RoleManager roleManager) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
		this.activityManager = activityManager;
		this.commentManager = commentManager;
		this.roleManager = roleManager;
		setResource();
		setObjectType();
	}

	private void setObjectType() {
		switch(resource.getResourceType()) {
			case Activity:
				objectType = ObjectType.Activity;
				break;
			case Competence:
				objectType = ObjectType.Competence;	
				break;
		}
	}

	protected void setResource() {
		this.resource = (Comment1) session.merge(event.getObject());
	}

	@Override
	List<Long> getReceiverIds() {
		List<Long> users = null;
		try {
			Long resCreatorId = commentManager.getCommentedResourceCreatorId(resource.getResourceType(), 
					resource.getCommentedResourceId());
			if(resCreatorId != null) {
				List<Long> usersToExclude = new ArrayList<>();
				usersToExclude.add(resCreatorId);
				users = commentManager.getIdsOfUsersThatCommentedResource(resource.getResourceType(),
						resource.getCommentedResourceId(), usersToExclude);
				users.add(resCreatorId);
			}
		} catch(Exception e) {
			logger.error(e);
			return null;
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
//			logger.error("Commenting on the resource of a type: " + 
//					((Comment1) resource).getObject().getClass() + " is not captured.");
			return false;
		}
	}

	@Override
	NotificationType getNotificationType() {
		return NotificationType.Comment;
	}

	@Override
	ObjectType getObjectType() {
		return objectType;
	}

	@Override
	long getObjectId() {
		return resource.getCommentedResourceId();
	}

	@Override
	String getNotificationLink() {
		switch(objectType) {
			case Activity:
				Long compId = activityManager.getCompetenceIdForActivity(getObjectId());
				if(compId != null) {
					return "/activity.xhtml?compId=" + idEncoder.encodeId(compId) + "&actId=" 
							+ idEncoder.encodeId(getObjectId()) + "&comment=" +
									idEncoder.encodeId(resource.getId());
				}
				break;
			case Competence:
				return "/competence.xhtml?compId=" + idEncoder.encodeId(getObjectId()) + "&comment=" +
					idEncoder.encodeId(resource.getId());
			default:
				break;
		}
		return null;
	}
	
	@Override
	protected String getUrlSection(long userId) {
		List<String> roles = new ArrayList<>();
		roles.add(RoleNames.MANAGER);
		roles.add(RoleNames.INSTRUCTOR);
		boolean hasManagerOrInstructorRole = roleManager.hasAnyRole(userId, roles);
		if(hasManagerOrInstructorRole) {
			return "/manage";
		} 
		return "";
	}

}
