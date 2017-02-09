package org.prosolo.services.notifications.eventprocessing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.ContextName;
import org.prosolo.common.event.context.LearningContext;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public class CommentLikeEventProcessor extends NotificationEventProcessor {

	private static Logger logger = Logger.getLogger(CommentLikeEventProcessor.class);

	private Activity1Manager activityManager;
	
	private Comment1 comment;
	private ResourceType commentedResourceType;
	private ContextJsonParserService contextJsonParserService;
	
	public CommentLikeEventProcessor(Event event, Session session,
			NotificationManager notificationManager,
			NotificationsSettingsManager notificationsSettingsManager,
			Activity1Manager activityManager,
			UrlIdEncoder idEncoder,
			ContextJsonParserService contextJsonParserService) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
		this.activityManager = activityManager;
		this.contextJsonParserService = contextJsonParserService;
		setResource();
		setCommentedResourceType();
	}

	protected void setResource() {
		this.comment = (Comment1) session.load(event.getObject().getClass(), event.getObject().getId());
	}
	
	private void setCommentedResourceType() {
		switch(comment.getResourceType()) {
			case Activity:
				commentedResourceType = ResourceType.Activity;
				break;
			case Competence:
				commentedResourceType = ResourceType.Competence;	
				break;
			case SocialActivity:
				commentedResourceType = ResourceType.SocialActivity;	
				break;
			case ActivityResult:
				commentedResourceType = ResourceType.ActivityResult;	
				break;
		}
	}

	@Override
	List<NotificationReceiverData> getReceiversData() {
		List<NotificationReceiverData> receivers = new ArrayList<>();
		try {
			Long resCreatorId = comment.getUser().getId();
			String prefix = comment.isManagerComment() ? "/manage" : "";
			receivers.add(new NotificationReceiverData(resCreatorId, prefix + getNotificationLink(), 
					false));
			return receivers;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			return new ArrayList<>();
		}
	}

	@Override
	long getSenderId() {
		return event.getActorId();
	}

	@Override
	boolean isConditionMet(long sender, long receiver) {
		if (receiver != 0 && sender != receiver) {
			return true;
		}
		return false;
	}

	@Override
	NotificationType getNotificationType() {
		return NotificationType.Comment_Like;
	}

	@Override
	ResourceType getObjectType() {
		return ResourceType.Comment;
	}

	@Override
	long getObjectId() {
		return comment.getId();
	}

	private String getNotificationLink() {
		switch(commentedResourceType) {
		case Activity:
			Long compId = activityManager.getCompetenceIdForActivity(comment.getCommentedResourceId());
			if (compId != null) {
//				return "/activity.xhtml?compId=" + idEncoder.encodeId(compId) + "&actId=" 
//						+ idEncoder.encodeId(getObjectId()) + "&comment=" +
//								idEncoder.encodeId(comment.getId());
				
				return "/competences/" +
						idEncoder.encodeId(compId) + "/" +
						idEncoder.encodeId(comment.getCommentedResourceId()) +
						"?comment=" +  idEncoder.encodeId(comment.getId());
			}
			break;
		case Competence:
//			return "/competence.xhtml?compId=" + idEncoder.encodeId(getObjectId()) + "&comment=" +
//				idEncoder.encodeId(comment.getId());
			return "/competences/" +
					idEncoder.encodeId(comment.getCommentedResourceId()) +
					"?comment=" +  idEncoder.encodeId(comment.getId());
		case SocialActivity:
			return "/posts/" +
				idEncoder.encodeId(comment.getCommentedResourceId()) +
				"?comment=" +  idEncoder.encodeId(comment.getId());
		case ActivityResult:
			LearningContext learningContext = contextJsonParserService.
				parseCustomContextString(event.getPage(), event.getContext(), event.getService());
		
			long idsRead = 0;	// counting if we have read all the ids
			Context credentialContext = learningContext.getSubContextWithName(ContextName.CREDENTIAL);
			Context competenceContext = learningContext.getSubContextWithName(ContextName.COMPETENCE);
			Context activityContext = learningContext.getSubContextWithName(ContextName.ACTIVITY);
			
			long credentialId = 0;
			long competenceId = 0;
			long activityId = 0;
			
			if (credentialContext != null) {
				credentialId = credentialContext.getId();
				idsRead++;
			}
			if (competenceContext != null) {
				competenceId = competenceContext.getId();
				idsRead++;
			}
			if (activityContext != null) {
				activityId = activityContext.getId();
				idsRead++;
			}
			if (idsRead != 3) {
				logger.error("Can not find ids of a credential, competence or activity");
			}
			return "/credentials/" +
				idEncoder.encodeId(credentialId) + "/" +
				idEncoder.encodeId(competenceId) + "/" +
				idEncoder.encodeId(activityId) + "/" +
				"responses/" + 
				idEncoder.encodeId(comment.getCommentedResourceId()) + 
				"?comment=" +  idEncoder.encodeId(comment.getId());
		default:
			break;
		}
		return null;
	}

}
