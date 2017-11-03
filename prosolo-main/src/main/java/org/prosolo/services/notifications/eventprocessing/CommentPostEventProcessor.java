package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.event.Event;
import org.prosolo.services.interaction.CommentManager;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.data.Role;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.urlencoding.UrlIdEncoder;

import java.util.ArrayList;
import java.util.List;

public class CommentPostEventProcessor extends CommentEventProcessor {

	private static Logger logger = Logger.getLogger(CommentPostEventProcessor.class);
	
	private CommentManager commentManager;
	
	public CommentPostEventProcessor(Event event, Session session,
									 NotificationManager notificationManager,
									 NotificationsSettingsManager notificationsSettingsManager, Activity1Manager activityManager,
									 UrlIdEncoder idEncoder, CommentManager commentManager, ContextJsonParserService contextJsonParserService) {
		super(event, session, notificationManager, notificationsSettingsManager, activityManager, idEncoder,
				contextJsonParserService);
		this.commentManager = commentManager;
	}
	
	@Override
	List<NotificationReceiverData> getReceiversData() {
		List<NotificationReceiverData> receiversData = new ArrayList<>();
		
		try {
			Long resCreatorId = commentManager.getCommentedResourceCreatorId(
					getResource().getResourceType(), 
					getResource().getCommentedResourceId());
			if (resCreatorId != null) {
				List<Long> usersToExclude = new ArrayList<>();
				usersToExclude.add(resCreatorId);

				//get ids of all users who posted a comment as regular users
				List<Long> users = commentManager.getIdsOfUsersThatCommentedResource(
						getResource().getResourceType(), getResource().getCommentedResourceId(), 
						Role.User, usersToExclude);
				String userSectionLink = getNotificationLink(Role.User);
				for(Long id : users) {
					receiversData.add(new NotificationReceiverData(id, userSectionLink, false));
				}
				usersToExclude.addAll(users);
				//get ids of all users who posted a comment as managers
				List<Long> managers = commentManager.getIdsOfUsersThatCommentedResource(
						getResource().getResourceType(), getResource().getCommentedResourceId(), 
						Role.Manager, 
						usersToExclude);
				String manageSectionLink = getNotificationLink(Role.Manager);
				for(long id : managers) {
					receiversData.add(new NotificationReceiverData(id, manageSectionLink, false));
				}
				/*
				 * determine role for user as a creator of this resource
				 */
				Role creatorRole = commentManager.getCommentedResourceCreatorRole(
						getResource().getResourceType(), getResource().getCommentedResourceId());
				String creatorLink = creatorRole == Role.User ? userSectionLink : manageSectionLink;
				receiversData.add(new NotificationReceiverData(resCreatorId, creatorLink, true));
			}
			return receiversData;
		} catch(Exception e) {
			logger.error(e);
			return new ArrayList<>();
		}
	}

	@Override
	NotificationType getNotificationType() {
		return NotificationType.Comment;
	}

	@Override
	ResourceType getObjectType() {
		return getCommentedResourceType();
	}

	@Override
	long getObjectId() {
		return getResource().getCommentedResourceId();
	}

}
