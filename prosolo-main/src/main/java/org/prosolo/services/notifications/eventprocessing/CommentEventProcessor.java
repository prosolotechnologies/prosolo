package org.prosolo.services.notifications.eventprocessing;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.ContextName;
import org.prosolo.common.event.context.LearningContext;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.data.Role;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public abstract class CommentEventProcessor extends NotificationEventProcessor {

	private static Logger logger = Logger.getLogger(CommentEventProcessor.class);
	
	private Comment1 resource;
	private ResourceType commentedResourceType;
	private ContextJsonParserService contextJsonParserService;
	private Activity1Manager activityManager;
	
	public CommentEventProcessor(Event event, Session session,
								 NotificationManager notificationManager,
								 NotificationsSettingsManager notificationsSettingsManager, Activity1Manager activityManager,
								 UrlIdEncoder idEncoder, ContextJsonParserService contextJsonParserService) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
		this.contextJsonParserService = contextJsonParserService;
		setResource();
		setCommentedResourceType();
		this.activityManager = activityManager;
	}

	private void setCommentedResourceType() {
		switch(resource.getResourceType()) {
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

	protected void setResource() {
		this.resource = (Comment1) session.load(event.getObject().getClass(), event.getObject().getId());
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
	abstract List<NotificationReceiverData> getReceiversData();

	@Override
	abstract NotificationType getNotificationType();

	@Override
	abstract ResourceType getObjectType();

	@Override
	abstract long getObjectId();

	protected final String getNotificationLink(Role role) {
		LearningContext learningContext = null;
		Context competenceContext = null;
		switch(commentedResourceType) {
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
			
				//long idsRead = 0;	// counting if we have read all the ids
				Context credentialContext = learningContext.getSubContextWithName(ContextName.CREDENTIAL);
				competenceContext = learningContext.getSubContextWithName(ContextName.COMPETENCE);
				Context activityContext = learningContext.getSubContextWithName(ContextName.ACTIVITY);
				
				long credentialId = 0;
				long competenceId = 0;
				long activityId = 0;
				
				if (credentialContext != null) {
					credentialId = credentialContext.getId();
					//idsRead++;
				}
				if (competenceContext != null) {
					competenceId = competenceContext.getId();
					//idsRead++;
				}
				if (activityContext != null) {
					activityId = activityContext.getId();
					//idsRead++;
				}

				if (activityId > 0) {
					if (role == Role.User) {
						/*
						this has to be done because there are pages from which activity response can be commented
						where competence id is not passed and not available in context
						 */
						if (competenceId == 0) {
							competenceId = activityManager.getCompetenceIdForActivity(activityId);
						}
						if (credentialId == 0) {
							return "/competences/" +
									idEncoder.encodeId(competenceId) + "/" +
									idEncoder.encodeId(activityId) + "/" +
									"responses/" +
									idEncoder.encodeId(resource.getCommentedResourceId()) +
									"?comment=" + idEncoder.encodeId(resource.getId());
						} else {
							return "/credentials/" +
									idEncoder.encodeId(credentialId) + "/" +
									idEncoder.encodeId(competenceId) + "/" +
									idEncoder.encodeId(activityId) + "/" +
									"responses/" +
									idEncoder.encodeId(resource.getCommentedResourceId()) +
									"?comment=" + idEncoder.encodeId(resource.getId());
						}
					} else {
						//for manage section we have a different page than in user section
						/*
						if credential id can't be extracted we don't know to which assessment should we
						send the manager so for now notification is not created in this case
						TODO maybe it doesn't make sense to send manager to assessment page - he can comment
						the response from assessment for one credential and notification can send him to
						the assessment for other credential.
						 */
						if (credentialId > 0) {
							return "/manage/credentials/"
									+ idEncoder.encodeId(credentialId) + "/assessments/activities/"
									+ idEncoder.encodeId(activityId) + "/"
									+ idEncoder.encodeId(resource.getCommentedResourceId())
									+ "?comment=" + idEncoder.encodeId(resource.getId());
						}
					}
				}
				break;
			default:
				break;
		}
		return null;
	}

	protected Comment1 getResource() {
		return resource;
	}

	public ResourceType getCommentedResourceType() {
		return commentedResourceType;
	}
	
}
