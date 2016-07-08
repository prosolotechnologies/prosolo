package org.prosolo.services.interaction;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.old.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.old.TwitterPostSocialActivity;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.content.Post;
import org.prosolo.common.domainmodel.content.RichContent;
import org.prosolo.common.domainmodel.content.TwitterPost;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.interaction.impl.PostManagerImpl.PostEvent;
import org.prosolo.services.nodes.data.activity.attachmentPreview.AttachmentPreview;

public interface PostManager extends AbstractManager{

	PostEvent createNewPost(long userId, String text, VisibilityType visibility, 
			AttachmentPreview attachmentPreview, long[] mentionedUsers, 
			boolean propagateManuallyToSocialStream, String context,
			String page, String learningContext, String service) throws EventException, ResourceCouldNotBeLoadedException;
	
	PostEvent createNewGoalNote(long userId, long goalId, String text,
			AttachmentPreview attachmentPreview, VisibilityType visibility, 
			boolean propagateManuallyToSocialStream, boolean connectNewPostWithStatus,
			String context)	throws EventException, ResourceCouldNotBeLoadedException;
	
	Post resharePost(User user, Post originalPost) throws EventException;
	
//	Post resharePost(User user, String originalPostUri) throws EventException, ResourceCouldNotBeLoadedException;
	
	PostEvent resharePost(long userId, SocialActivity socialActivity, boolean propagateManuallyToSocialStream) throws EventException, ResourceCouldNotBeLoadedException;
	
	PostEvent reshareSocialActivity(long userId, String text,
			VisibilityType visibility, AttachmentPreview attachmentPreview,
			SocialActivity originalSocialActivity,
			boolean propagateManuallyToSocialStream, String page,
			String learningContext, String service) throws EventException, ResourceCouldNotBeLoadedException;
	
	RichContent createRichContent(AttachmentPreview attachmentPreview);
	
	PostEvent createNewPost(User user, 
			Date created, String postLink, String text,
			VisibilityType visibility, boolean propagateManuallyToSocialStream) throws EventException;
	
	TwitterPost createNewTwitterPost(User maker, Date created, String postLink, long tweetId, String creatorName,
			String screenName, String userUrl, String profileImage, String text, VisibilityType visibility, 
			Collection<String> hashtags, boolean toSave) throws EventException;

	PostEvent shareResource(long userId, String text,
			VisibilityType visibility, Node resource,
			boolean propagateManuallyToSocialStream,
			String context, String page,
			String learningContext, String service) throws EventException, ResourceCouldNotBeLoadedException;

	int getNumberOfTwitterPostsCreatedBefore(Session session,Date createDate);

	void deleteAllTwitterPostsCreatedBefore(Session session, Date createDate,
			int firstResult, int maxResults, int inThreads);

	int bulkDeleteTwitterPostSocialActivitiesCreatedBefore(Session session,
			Date createDate);

	List<User> getUsersWhoSharedSocialActivity(long socialActivityId);

	boolean isSharedByUser(SocialActivity socialActivity, User user);

	List<TwitterPostSocialActivity> getTwitterPosts(Collection<Tag> hashtags, Date date);

	SocialActivity updatePost(long userId, long socialActivityId, String text, String context) throws ResourceCouldNotBeLoadedException;
}
