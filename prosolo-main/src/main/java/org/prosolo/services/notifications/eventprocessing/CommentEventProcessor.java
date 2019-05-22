package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.user.notifications.NotificationActorRole;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.ContextName;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.notifications.eventprocessing.data.NotificationSenderData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.List;

public abstract class CommentEventProcessor extends NotificationEventProcessor {

	private static Logger logger = Logger.getLogger(CommentEventProcessor.class);
	
	private Comment1 resource;
	private ResourceType commentedResourceType;
	private Activity1Manager activityManager;
	private Context context;
	
	public CommentEventProcessor(Event event, Session session,
								 NotificationManager notificationManager,
								 NotificationsSettingsManager notificationsSettingsManager, Activity1Manager activityManager,
								 UrlIdEncoder idEncoder) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
		context = ContextJsonParserService.parseContext(event.getContext());
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
		this.resource = (Comment1) session.get(event.getObject().getClass(), event.getObject().getId());
	}

	@Override
	NotificationSenderData getSenderData() {
		return new NotificationSenderData(event.getActorId(), NotificationActorRole.OTHER, false);
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

	protected final String getNotificationLink(PageSection section) {
		switch (commentedResourceType) {
			case Activity:
				long compId = Context.getIdFromSubContextWithName(context, ContextName.COMPETENCE);
				if (compId > 0) {
					return  section.getPrefix() + "/competences/" +
							idEncoder.encodeId(compId) + "/" +
							idEncoder.encodeId(resource.getCommentedResourceId())+
							"?comment=" +  idEncoder.encodeId(resource.getId());
				} else {
					logger.error("Activity comment notification link can't be constructed because competence id is not available in learning context.");
				}
				break;
			case Competence:
				return 	section.getPrefix() + "/competences/" +
						idEncoder.encodeId(resource.getCommentedResourceId()) +
						"?comment=" +  idEncoder.encodeId(resource.getId());
			case SocialActivity:
				return 	section.getPrefix() + "/posts/" +
						idEncoder.encodeId(resource.getCommentedResourceId()) +
						"?comment=" +  idEncoder.encodeId(resource.getId());
			case ActivityResult:
				//long idsRead = 0;	// counting if we have read all the ids
				long credentialId = Context.getIdFromSubContextWithName(context, ContextName.CREDENTIAL);
				long competenceId = Context.getIdFromSubContextWithName(context, ContextName.COMPETENCE);
				long activityId = Context.getIdFromSubContextWithName(context, ContextName.ACTIVITY);

				if (activityId > 0) {
					if (section.equals(PageSection.STUDENT)) {
						/*
						this has to be done because there are pages from which activity response can be commented
						where competence id is not passed and not available in context
						 */
						if (competenceId == 0) {
							competenceId = activityManager.getCompetenceIdForActivity(activityId);
						}
						if (credentialId == 0) {
							return 	section.getPrefix() + "/competences/" +
									idEncoder.encodeId(competenceId) + "/" +
									idEncoder.encodeId(activityId) + "/" +
									"responses/" +
									idEncoder.encodeId(resource.getCommentedResourceId()) +
									"?comment=" + idEncoder.encodeId(resource.getId());
						} else {
							return 	section.getPrefix() + "/credentials/" +
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
							return 	section.getPrefix() + "/credentials/"
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

	public Context getContext() {
		return context;
	}
}
