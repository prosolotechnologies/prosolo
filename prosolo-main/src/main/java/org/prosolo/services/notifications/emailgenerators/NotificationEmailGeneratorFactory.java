/**
 * 
 */
package org.prosolo.services.notifications.emailgenerators;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.Announcement;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.nodes.DefaultManager;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class NotificationEmailGeneratorFactory {
	
	@Inject
	private DefaultManager defaultManager;
	
	private static Logger logger = Logger.getLogger(NotificationEmailGeneratorFactory.class);
	
	public NotificationEmailGenerator getNotificationEmailContentGenerator(String name, String actor, String predicate, long objectId, ResourceType objectType, String objectTitle,
			String date, String link, NotificationType type, Session session) {
		
		switch (type) {
			case Follow_User:
				return new FollowUserNotificationEmailGenerator(name, actor, predicate, objectTitle, date, link);
			case Comment:
				return new CommentNotificationEmailGenerator(name, actor, predicate, objectTitle, date, link);
			case Comment_Like:
				String targetTypeStr = null;
				String targetTitleStr = null;
				
				try {
					Comment1 comment = defaultManager.loadResource(Comment1.class, objectId, true);
					
					switch (comment.getResourceType()) {
					case Competence:
						Competence1 comp = defaultManager.loadResource(Competence1.class, comment.getCommentedResourceId(), true);
						targetTypeStr = "competence";
						targetTitleStr = comp.getTitle();
						break;
					case Activity:
						targetTypeStr = "activity";
						Activity1 activity = defaultManager.loadResource(Activity1.class, comment.getCommentedResourceId(), true);
						targetTitleStr = activity.getTitle();
						break;
					default:
						break;
					}
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
				
				return new CommentLikeNotificationEmailGenerator(name, actor, predicate, targetTypeStr, targetTitleStr, date, link);
			case Social_Activity_Like:
				return new SocialActivityLikeNotificationEmailGenerator(name, actor, predicate, date, link);
			case Mention:
				return new MentionNotificationEmailGenerator(name, actor, predicate, objectTitle, date, link);
			case Assessment_Requested:
			case Assessment_Comment:
			case Assessment_Approved:
			case GradeAdded:
				return new AssessmentNotificationEmailGenerator(name, actor, predicate, objectTitle, date, link, type);
			case AnnouncementPublished:
				try {
					Announcement ann = defaultManager.loadResource(Announcement.class, objectId, session);
					return new AnnouncementPublishedNotificationEmailGenerator(name, actor, objectTitle, ann.getCredential().getTitle(), predicate, date, link, ann.getTitle(), ann.getText());
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
			default:
				break;
			}
		return null;
	}
}