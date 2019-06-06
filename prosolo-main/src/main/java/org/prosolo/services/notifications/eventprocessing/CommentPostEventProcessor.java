package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.Event;
import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.ContextName;
import org.prosolo.services.activityWall.SocialActivityManager;
import org.prosolo.services.interaction.CommentManager;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.Role;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommentPostEventProcessor extends CommentEventProcessor {

	private static Logger logger = Logger.getLogger(CommentPostEventProcessor.class);
	
	private CommentManager commentManager;
	private CredentialManager credentialManager;
	private SocialActivityManager socialActivityManager;

	public CommentPostEventProcessor(Event event, Session session,
									 NotificationManager notificationManager,
									 NotificationsSettingsManager notificationsSettingsManager, Activity1Manager activityManager,
									 UrlIdEncoder idEncoder, CommentManager commentManager, CredentialManager credentialManager,
									 SocialActivityManager socialActivityManager) {
		super(event, session, notificationManager, notificationsSettingsManager, activityManager, idEncoder);
		this.commentManager = commentManager;
		this.credentialManager = credentialManager;
		this.socialActivityManager = socialActivityManager;
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

				/*
				if a student has posted a comment, we want to notify his instructor if delivery id available and
				instructor is assigned
				 */
				long studentInstructorId = 0;
				boolean isStudentComment = !getResource().isManagerComment();
				long credentialId = Context.getIdFromSubContextWithName(getContext(), ContextName.CREDENTIAL);

				if (isStudentComment && credentialId > 0) {
					TargetCredential1 tc = credentialManager.getTargetCredentialForStudentAndCredential(credentialId, event.getActorId(), session);
					if (tc.getInstructor() != null) {
						studentInstructorId = tc.getInstructor().getUser().getId();
						usersToExclude.add(studentInstructorId);
					}
				}

				String userSectionLink = getNotificationLink(PageSection.STUDENT);

				// if link is null or empty it means there is no enough information to create notification
				if (userSectionLink != null && !userSectionLink.isEmpty()) {
					//get ids of all users who posted a comment as regular users
					List<Long> users = commentManager.getIdsOfUsersThatCommentedResource(
							getResource().getResourceType(), getResource().getCommentedResourceId(),
							Role.User, usersToExclude);

					// in case of SocialActivity or ActivityResult, we want to notify the creator of the post/activity result
					if (getResource().getResourceType() == CommentedResourceType.SocialActivity) {
						users.add(socialActivityManager.getSocialActivityActorId(getResource().getCommentedResourceId()));
					} else if (getResource().getResourceType() == CommentedResourceType.ActivityResult) {
						users.add(activityManager.getTargetActivityOwnerId(getResource().getCommentedResourceId()));
					}

					for (Long id : users) {
						receiversData.add(new NotificationReceiverData(id, userSectionLink, id == resCreatorId, PageSection.STUDENT));
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
						receiversData.add(new NotificationReceiverData(id, manageSectionLink, id == resCreatorId, PageSection.MANAGE));
					}
					/*
					 * add student instructor (if exists) to the collection of receivers with a link
					 * to manage section
					 */
					if (studentInstructorId > 0) {
						receiversData.add(new NotificationReceiverData(studentInstructorId, manageSectionLink, studentInstructorId == resCreatorId, PageSection.MANAGE));
					}
				}
			}
			return receiversData;
		} catch (Exception e) {
			logger.error("Error", e);
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
