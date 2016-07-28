/**
 * 
 */
package org.prosolo.services.notifications.emailgenerators;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ObjectType;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.interfaceSettings.eventProcessors.InterfaceEventProcessorFactory;
import org.prosolo.services.nodes.DefaultManager;
import org.springframework.stereotype.Service;

@Service
public class NotificationEmailGeneratorFactory {
	
	@Inject
	private DefaultManager defaultManager;
	
	private static Logger logger = Logger.getLogger(InterfaceEventProcessorFactory.class);
	
	public NotificationEmailGenerator getNotificationEmailContentGenerator(String name, String actor, String predicate, long objectId, ObjectType objectType, String objectTitle,
			String date, String link, NotificationType type) {
		
		switch (type) {
			case Follow_User:
				return new FollowUserNotificationEmailGenerator(name, actor, predicate, objectTitle, date, link);
			case Comment:
				return new CommentNotificationEmailGenerator(name, actor, predicate, objectTitle, date, link);
			case Comment_Like:
				String targetType = null;
				String targetTitle = null;
				
				try {
					Comment1 comment = defaultManager.loadResource(Comment1.class, objectId, true);
					
					switch (comment.getResourceType()) {
					case Competence:
						Competence1 comp = defaultManager.loadResource(Competence1.class, comment.getCommentedResourceId(), true);
						targetType = "competence";
						targetTitle = comp.getTitle();
						break;
					case Activity:
						targetType = "activity";
						Activity1 activity = defaultManager.loadResource(Activity1.class, comment.getCommentedResourceId(), true);
						targetTitle = activity.getTitle();
						break;
					default:
						break;
					}
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
				
				return new CommentLikeNotificationEmailGenerator(name, actor, predicate, targetType, targetTitle, date, link);
			case Mention:
				return new MentionNotificationEmailGenerator(name, actor, predicate, objectTitle, date, link);
//			case Assessment_Requested:
//				return new FollowUserNotificationEmailGenerator(name, actor, predicate, objectTitle, date, link);
			case Assessment_Requested:
			case Assessment_Comment:
			case Assessment_Approved:
				return new AssessmentNotificationEmailGenerator(name, actor, predicate, objectTitle, date, link, type);
//			case Assessment_Comment:
//				return new FollowUserNotificationEmailGenerator(name, actor, predicate, objectTitle, date, link);
			default:
				break;
			}
		return null;
	}
}