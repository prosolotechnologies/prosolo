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
import org.prosolo.web.util.page.PageSection;

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

				String userSectionLink = getNotificationLink(PageSection.STUDENT);
				//if link is null or empty it means there is no enough information to create notification
				if (userSectionLink != null && !userSectionLink.isEmpty()) {
					//get ids of all users who posted a comment as regular users
					List<Long> users = commentManager.getIdsOfUsersThatCommentedResource(
							getResource().getResourceType(), getResource().getCommentedResourceId(),
							Role.User, usersToExclude);
					for (Long id : users) {
						receiversData.add(new NotificationReceiverData(id, userSectionLink, false, PageSection.STUDENT));
					}
					usersToExclude.addAll(users);
				}

				String manageSectionLink = getNotificationLink(PageSection.MANAGE);
				//if link is null or empty it means there is no enough information to create notification
				if (manageSectionLink != null && !manageSectionLink.isEmpty()) {
					//get ids of all users who posted a comment as managers
					List<Long> managers = commentManager.getIdsOfUsersThatCommentedResource(
							getResource().getResourceType(), getResource().getCommentedResourceId(),
							Role.Manager,
							usersToExclude);
					for (long id : managers) {
						receiversData.add(new NotificationReceiverData(id, manageSectionLink, false, PageSection.MANAGE));
					}
				}
				/*
				 * determine role for user as a creator of this resource
				 */
				Role creatorRole = commentManager.getCommentedResourceCreatorRole(
						getResource().getResourceType(), getResource().getCommentedResourceId());
				String creatorLink = creatorRole == Role.User ? userSectionLink : manageSectionLink;
				PageSection section = creatorRole == Role.User ? PageSection.STUDENT : PageSection.MANAGE;
				if (creatorLink != null && !creatorLink.isEmpty()) {
					receiversData.add(new NotificationReceiverData(resCreatorId, creatorLink, true, section));
				}
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
