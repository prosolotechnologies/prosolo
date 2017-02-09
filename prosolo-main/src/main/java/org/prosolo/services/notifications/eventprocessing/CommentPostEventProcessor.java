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
import org.prosolo.services.interaction.CommentManager;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.data.Role;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public class CommentPostEventProcessor extends NotificationEventProcessor {

	private static Logger logger = Logger.getLogger(CommentPostEventProcessor.class);
	
	private Comment1 resource;
	private ResourceType objectType;
	private CommentManager commentManager;
	private ContextJsonParserService contextJsonParserService;
	
	public CommentPostEventProcessor(Event event, Session session,
			NotificationManager notificationManager, 
			NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder, 
			CommentManager commentManager, ContextJsonParserService contextJsonParserService) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
		this.commentManager = commentManager;
		this.contextJsonParserService = contextJsonParserService;
		setResource();
		setObjectType();
	}

	private void setObjectType() {
		switch(resource.getResourceType()) {
			case Activity:
				objectType = ResourceType.Activity;
				break;
			case Competence:
				objectType = ResourceType.Competence;	
				break;
			case SocialActivity:
				objectType = ResourceType.SocialActivity;	
				break;
			case ActivityResult:
				objectType = ResourceType.ActivityResult;	
				break;
		}
	}

	protected void setResource() {
		this.resource = (Comment1) session.load(event.getObject().getClass(), event.getObject().getId());
	}
	
	@Override
	List<NotificationReceiverData> getReceiversData() {
		List<NotificationReceiverData> receiversData = new ArrayList<>();
		
		try {
			Long resCreatorId = commentManager.getCommentedResourceCreatorId(resource.getResourceType(), 
					resource.getCommentedResourceId());
			if (resCreatorId != null) {
				List<Long> usersToExclude = new ArrayList<>();
				usersToExclude.add(resCreatorId);
				
				String link = getNotificationLink();
				//get ids of all users who posted a comment as regular users
				List<Long> users = commentManager.getIdsOfUsersThatCommentedResource(
						resource.getResourceType(), resource.getCommentedResourceId(), 
						Role.User, usersToExclude);
				for(Long id : users) {
					receiversData.add(new NotificationReceiverData(id, link, false));
				}
				usersToExclude.addAll(users);
				//get ids of all users who posted a comment as managers
				List<Long> managers = commentManager.getIdsOfUsersThatCommentedResource(
						resource.getResourceType(), resource.getCommentedResourceId(), Role.Manager, 
						usersToExclude);
				for(long id : managers) {
					receiversData.add(new NotificationReceiverData(id, "/manage" + link, false));
				}
				/*
				 * determine role for user as a creator of this resource and add appropriate
				 * prefix to notification url based on that
				 */
				Role creatorRole = commentManager.getCommentedResourceCreatorRole(
						resource.getResourceType(), resource.getCommentedResourceId());
				String prefix =  creatorRole == Role.Manager ? "/manage" : "";
				receiversData.add(new NotificationReceiverData(resCreatorId, prefix + link, true));
			}
			return receiversData;
		} catch(Exception e) {
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
		return NotificationType.Comment;
	}

	@Override
	ResourceType getObjectType() {
		return objectType;
	}

	@Override
	long getObjectId() {
		return resource.getCommentedResourceId();
	}

	private String getNotificationLink() {
		LearningContext learningContext = null;
		Context competenceContext = null;
		switch(objectType) {
			case Activity:
				learningContext = contextJsonParserService.
					parseCustomContextString(event.getPage(), event.getContext(), event.getService());
				competenceContext = learningContext.getSubContextWithName(ContextName.COMPETENCE);
				long compId = competenceContext != null ? competenceContext.getId() : 0;
				if (compId > 0) {
					return "/competences/" +
							idEncoder.encodeId(compId) + "/" +
							idEncoder.encodeId(resource.getCommentedResourceId())+
							"?comment=" +  idEncoder.encodeId(resource.getId());
				}
				break;
			case Competence:
				return "/competences/" +
						idEncoder.encodeId(resource.getCommentedResourceId()) +
						"?comment=" +  idEncoder.encodeId(resource.getId());
			case SocialActivity:
				return "/posts/" +
					idEncoder.encodeId(resource.getCommentedResourceId()) +
					"?comment=" +  idEncoder.encodeId(resource.getId());
			case ActivityResult:
				learningContext = contextJsonParserService.
					parseCustomContextString(event.getPage(), event.getContext(), event.getService());
			
				long idsRead = 0;	// counting if we have read all the ids
				Context credentialContext = learningContext.getSubContextWithName(ContextName.CREDENTIAL);
				competenceContext = learningContext.getSubContextWithName(ContextName.COMPETENCE);
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
					idEncoder.encodeId(resource.getCommentedResourceId()) + 
					"?comment=" +  idEncoder.encodeId(resource.getId());
			default:
				break;
		}
		return null;
	}

}
