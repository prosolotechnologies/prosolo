package org.prosolo.services.activityWall.impl.data;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.SocialStreamSubViewType;
import org.prosolo.common.domainmodel.activitywall.TwitterPostSocialActivity;
import org.prosolo.common.domainmodel.content.ContentType;
import org.prosolo.common.domainmodel.content.GoalNote;
import org.prosolo.common.domainmodel.content.Post;
import org.prosolo.common.domainmodel.content.RichContent;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CourseEnrollment;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.ServiceType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserType;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.common.web.activitywall.data.PublishingServiceData;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.activityWall.impl.util.SocialActivityConverterUtil;
import org.prosolo.services.nodes.data.activity.attachmentPreview.AttachmentPreview;
import org.prosolo.services.nodes.data.activity.attachmentPreview.NodeData;
import org.prosolo.web.activitywall.data.SocialActivityCommentData;
import org.prosolo.web.activitywall.data.UserDataFactory;
import org.prosolo.web.util.AvatarUtils;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */

public class SocialActivityData implements Serializable {
	
	private static final long serialVersionUID = 5385110450493430564L;
	
	private Date dateCreated;
	private Date lastAction;
	private boolean updated;
	private String text;
	
	private NodeData socialActivity;

	private UserData actor = new UserData();
	private boolean anonUser;
	private boolean maker;
	private UserType twitterUserType;
	
	private PublishingServiceData publishingService;
	
	private NodeData object;
	
	/**
	 * Name of the action to be displayed on the interface. Examples: 'posted', 'liked', 'created'...
	 */
	private String actionName;
	private EventType action;

	private NodeData target;
	private UserData targetActor;
	
	/**
	 * If target exists, what is the relation to it, i.e. 'to', 'with' etc
	 */
	private String relationToTarget;
	
	private List<String> hashtags;

	private long configId;
	
	// options
//	private boolean optionsDisabled;
	private boolean commentsDisabled;
	private boolean unfollowedHashtags;
	private boolean userCanBeUnfollowed = false;
	private boolean showHiddenComments;
	private boolean shareable = false;
	
	private SocialStreamSubViewType subViewType;
	
	private int likeCount;
	private int dislikeCount;
	private int shareCount;
	private boolean liked;
	private boolean disliked;
	private boolean shared;
	private AttachmentPreview attachmentPreview;
	
	private List<SocialActivityCommentData> comments = new ArrayList<SocialActivityCommentData>();
	private String newComment;
	private String mentionedUsersInComment;
	
	private VisibilityType visibility;
	
	public SocialActivityData() {}
	
	public SocialActivityData(
			BigInteger socialActivityId,
			String socialActivityDType,
			Date dateCreated,
			Date lastAction,
			Character updated,
			BigInteger actorId,
			String actorName,
			String actorLastname,
			String actorAvatarUrl,
			String actorPosition,
			String actorProfileUrl,
			BigInteger postObjectId,
			String postObjectDType,
			BigInteger nodeObjectId,
			String nodeObjectDType,
			String nodeObjectTitle,
			String nodeObjectDescription,
			BigInteger courseEnrollmentObjectId,
			BigInteger courseId,
			String courseTitle,
			String courseDescription,
			BigInteger userObjectId,
			String action,
			BigInteger nodeTargetId,
			String nodeTargetDType,
			String nodeTargetTitle,
			BigInteger goalTargetId,
			String goalTargetTitle,
			BigInteger userTargetId,
			BigInteger socialActivityConfigId,
			Character commentsDisabled,
			String text,
			Integer likeCount,
			Integer dislikeCount,
			Integer shareCount,
			String visibility,
			String twitterPosterName,
			String twitterPosterNickname,
			String twitterPosterProfileUrl,
			String twitterPosterAvatarUrl,
			String twitterPostUrl,
			Integer twitterUserType,
			String postRichContentTitle,
			String postRichContentDescription,
			String postRichContentContentType,
			String postRichContentImageUrl,
			String postRichContentLink,
			Integer liked,
			Integer disliked
		) {
		this.socialActivity = new NodeData(
				socialActivityId.longValue(), 
				null, 
				SocialActivityConverterUtil.resolveSocialActivityClass(socialActivityDType), 
				null);
		
		this.dateCreated = dateCreated;
		this.lastAction = lastAction;
		this.updated = updated.charValue() == 'T';
		
		if (socialActivityConfigId != null) {
			this.configId = socialActivityConfigId.longValue();
		}
		
		if (actorId != null) {
			this.actor.setId(actorId.longValue());
			this.actor.setName(actorName + " " + actorLastname);
			this.actor.setAvatarUrl(AvatarUtils.getAvatarUrlInFormat(actorAvatarUrl, ImageFormat.size120x120));
			this.actor.setPosition(actorPosition);
			this.actor.setProfileUrl(actorProfileUrl);;
		}
		
		if (socialActivityDType.equals("TwitterPostSocialActivity")) {
			
			if (twitterUserType.intValue() == UserType.TWITTER_USER.ordinal()) {
				this.anonUser = true;
				this.twitterUserType = UserType.TWITTER_USER;
				this.actor.setExternalUser(true);
			} else {
				this.twitterUserType = UserType.REGULAR_USER;
				this.actor.setExternalUser(false);
			}

			this.actor.setAvatarUrl(twitterPosterAvatarUrl);
			this.actor.setProfileUrl(twitterPosterProfileUrl);
			this.actor.setName(twitterPosterName);
			
			this.publishingService = new PublishingServiceData(ServiceType.TWITTER, twitterPosterNickname, twitterPosterProfileUrl);
			
			this.actor.setPublishingService(publishingService);
			
			// since user.id is used on the interface to uniquely identify a user (e.g. for tooltip), we are setting socialActivity.id as an identifier
//			wallActivity.getActor().setId(socialActivity.getId());
//			
//			wallActivity.setActivityUrl(twitterPostSA.getPostUrl());
			
		}

		if (postObjectId != null) {
			this.object = new NodeData();
			this.object.setId(postObjectId.longValue());
			
//			System.out.println("nodeObjectDType - "+nodeObjectDType);
			if (postObjectDType.equals("GoalNote")) {
				this.object.setClazz(GoalNote.class);
			} else if (postObjectDType.equals("Post")) {
				this.object.setClazz(Post.class);
			}
		}
		
		if (nodeObjectId != null) {
			this.object = new NodeData();
			this.object.setId(nodeObjectId.longValue());
			this.object.setClazz(SocialActivityConverterUtil.resolveNodeClass(nodeObjectDType));
			this.object.setTitle(nodeObjectTitle);
			this.object.setDescription(nodeObjectDescription);
		}
		
		if (courseId != null) {
			this.object = new NodeData();
			this.object.setId(courseId.longValue());
			this.object.setTitle(courseTitle);
			this.object.setClazz(Course.class);
		}
		
//		if (userObjectId != null) {
//			object.setId(userObjectId.longValue());
//			object.setClazz(User.class);
//		}
		
		this.action = EventType.valueOf(action);
		
		if (nodeTargetId != null) {
			this.target = new NodeData();
			this.target.setId(nodeTargetId.longValue());
			this.target.setClazz(SocialActivityConverterUtil.resolveNodeClass(nodeTargetDType));
			this.target.setTitle(nodeTargetTitle);
		}
		
		if (goalTargetId != null) {
			this.target = new NodeData();
			this.target.setId(goalTargetId.longValue());
			this.target.setClazz(LearningGoal.class);
			this.target.setTitle(goalTargetTitle);
		}
		
		if (userTargetId != null) {
			this.targetActor = new UserData();
			this.targetActor.setId(userTargetId.longValue());
		}
		
		if (commentsDisabled != null) {
			if (commentsDisabled.charValue() == 'F') {
				this.commentsDisabled = false;
			} else {
				this.commentsDisabled = true;
			}
		}
		
		this.text = text;
		this.likeCount = likeCount;
		this.dislikeCount = dislikeCount;
		this.shareCount = shareCount;
		this.liked = liked == 1;
		this.disliked = disliked == 1;
		
		if (postRichContentTitle != null) {
			this.attachmentPreview = new AttachmentPreview();
			this.attachmentPreview.setContentType(ContentType.valueOf(postRichContentContentType));
			
			if (this.attachmentPreview.getContentType().equals(ContentType.LINK)) {
				attachmentPreview.setTitle(postRichContentTitle);
				attachmentPreview.setDescription(postRichContentDescription);
			} else if (this.attachmentPreview.getContentType().equals(ContentType.UPLOAD)) {
				attachmentPreview.setUploadTitle(postRichContentTitle);
				attachmentPreview.setUploadDescription(postRichContentDescription);
			}
			
			this.attachmentPreview.setLink(postRichContentLink);
			this.attachmentPreview.setImage(postRichContentImageUrl);
		}
		
		if (visibility != null) {
			this.visibility = VisibilityType.valueOf(visibility);
		}
		
	}
	
	public SocialActivityData(SocialActivity socialActivity) {
		this.socialActivity = new NodeData(
				socialActivity.getId(), 
				null, 
				socialActivity.getClass(), 
				socialActivity.getTitle());
		
		this.dateCreated = socialActivity.getDateCreated();
		
		//this.actor = new UserData(socialActivity.getMaker());
		this.actor=UserDataFactory.createUserData(socialActivity.getMaker());
		
		if (socialActivity instanceof TwitterPostSocialActivity) {
			
			TwitterPostSocialActivity twitterPostSocialActivity = (TwitterPostSocialActivity) socialActivity;
			this.anonUser = true;
			if(this.actor!=null){
				this.actor.setAvatarUrl(twitterPostSocialActivity.getAvatarUrl());
				this.actor.setProfileUrl(twitterPostSocialActivity.getProfileUrl());
				this.actor.setName(twitterPostSocialActivity.getName());
			}else{
				System.out.println("Actor is null here...");
			}

			
			this.publishingService = new PublishingServiceData(
					ServiceType.TWITTER, 
					twitterPostSocialActivity.getNickname(), 
					twitterPostSocialActivity.getProfileUrl());
			
			this.actor.setPublishingService(publishingService);
			
			// since user.id is used on the interface to uniquely identify a user (e.g. for tooltip), we are setting socialActivity.id as an identifier
//			wallActivity.getActor().setId(socialActivity.getId());
//			
//			wallActivity.setActivityUrl(twitterPostSA.getPostUrl());
		}
				
		if (socialActivity.getObject() != null) {
			if (socialActivity.getObject() instanceof CourseEnrollment) {
				this.object = new NodeData(((CourseEnrollment) socialActivity.getObject()).getCourse());
			} else {
				this.object = new NodeData(socialActivity.getObject());
			}
		}

		this.action = socialActivity.getAction();
		
		if (socialActivity.getTarget() != null) {
			if (socialActivity.getTarget() instanceof Node) {
				if (socialActivity.getTarget() instanceof CourseEnrollment) {
					this.target = new NodeData(((CourseEnrollment) socialActivity.getTarget()).getCourse());
				} else {
					this.target = new NodeData(socialActivity.getTarget());
				}
			} else if (socialActivity.getTarget() instanceof User) {
				//this.targetActor = new UserData((User) socialActivity.getTarget());
				this.targetActor = UserDataFactory.createUserData((User) socialActivity.getTarget());
			}
		}
		
		this.commentsDisabled = socialActivity.isCommentsDisabled();
		
		this.text = socialActivity.getText();
		this.likeCount = socialActivity.getLikeCount();
		this.dislikeCount = socialActivity.getDislikeCount();
		this.shareCount = socialActivity.getShareCount();
		
		RichContent richContent = socialActivity.getRichContent();
		
		if (richContent != null) {
			this.attachmentPreview = new AttachmentPreview();
			this.attachmentPreview.setContentType(richContent.getContentType());
			
			if (richContent.getContentType().equals(ContentType.LINK)) {
				attachmentPreview.setTitle(richContent.getTitle());
				attachmentPreview.setDescription(richContent.getDescription());
			} else if (richContent.getContentType().equals(ContentType.UPLOAD)) {
				attachmentPreview.setUploadTitle(richContent.getTitle());
				attachmentPreview.setUploadDescription(richContent.getDescription());
			}
			
			this.attachmentPreview.setLink(richContent.getLink());
			this.attachmentPreview.setImage(richContent.getImageUrl());
		}
		
		this.visibility = socialActivity.getVisibility();
	}
	
	public Date getDateCreated() {
		return dateCreated;
	}
	
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	public Date getLastAction() {
		return lastAction;
	}

	public void setLastAction(Date lastAction) {
		this.lastAction = lastAction;
	}

	public boolean isUpdated() {
		return updated;
	}

	public void setUpdated(boolean updated) {
		this.updated = updated;
	}

	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public NodeData getSocialActivity() {
		return socialActivity;
	}
	
	public void setSocialActivity(NodeData socialActivity) {
		this.socialActivity = socialActivity;
	}
	
	public UserData getActor() {
		return actor;
	}
	
	public void setActor(UserData actor) {
		this.actor = actor;
	}
	
	public UserType getTwitterUserType() {
		return twitterUserType;
	}

	public void setTwitterUserType(UserType twitterUserType) {
		this.twitterUserType = twitterUserType;
	}

	public PublishingServiceData getPublishingService() {
		return publishingService;
	}

	public void setPublishingService(PublishingServiceData publishingService) {
		this.publishingService = publishingService;
	}

	public boolean isAnonUser() {
		return anonUser;
	}
	
	public void setAnonUser(boolean anonUser) {
		this.anonUser = anonUser;
	}
	
	public boolean isMaker() {
		return maker;
	}
	
	public void setMaker(boolean maker) {
		this.maker = maker;
	}
	
	public NodeData getObject() {
		return object;
	}
	
	public void setObject(NodeData object) {
		this.object = object;
	}
	
	public String getActionName() {
		return actionName;
	}
	
	public void setActionName(String actionName) {
		this.actionName = actionName;
	}
	
	public EventType getAction() {
		return action;
	}
	
	public void setAction(EventType action) {
		this.action = action;
	}
	
	public NodeData getTarget() {
		return target;
	}
	
	public void setTarget(NodeData target) {
		this.target = target;
	}
	
	public UserData getTargetActor() {
		return targetActor;
	}
	
	public void setTargetActor(UserData targetActor) {
		this.targetActor = targetActor;
	}
	
	public String getRelationToTarget() {
		return relationToTarget;
	}
	
	public void setRelationToTarget(String relationToTarget) {
		this.relationToTarget = relationToTarget;
	}
	
	public List<String> getHashtags() {
		return hashtags;
	}
	
	public void setHashtags(List<String> hashtags) {
		this.hashtags = hashtags;
	}
	
	public long getConfigId() {
		return configId;
	}
	
	public void setConfigId(long configId) {
		this.configId = configId;
	}
	
//	public boolean isOptionsDisabled() {
//		return optionsDisabled;
//	}
//	
//	public void setOptionsDisabled(boolean optionsDisabled) {
//		this.optionsDisabled = optionsDisabled;
//	}
	
	public boolean isCommentsDisabled() {
		return commentsDisabled;
	}
	
	public void setCommentsDisabled(boolean commentsDisabled) {
		this.commentsDisabled = commentsDisabled;
	}
	
	public boolean isUnfollowedHashtags() {
		return unfollowedHashtags;
	}
	
	public void setUnfollowedHashtags(boolean unfollowedHashtags) {
		this.unfollowedHashtags = unfollowedHashtags;
	}
	
	public boolean isUserCanBeUnfollowed() {
		return userCanBeUnfollowed;
	}
	
	public void setUserCanBeUnfollowed(boolean userCanBeUnfollowed) {
		this.userCanBeUnfollowed = userCanBeUnfollowed;
	}
	
	public boolean isShowHiddenComments() {
		return showHiddenComments;
	}
	
	public void setShowHiddenComments(boolean showHiddenComments) {
		this.showHiddenComments = showHiddenComments;
	}
	
	public boolean isShareable() {
		return shareable;
	}
	
	public void setShareable(boolean shareable) {
		this.shareable = shareable;
	}
	
	public SocialStreamSubViewType getSubViewType() {
		return subViewType;
	}
	
	public void setSubViewType(SocialStreamSubViewType subViewType) {
		this.subViewType = subViewType;
	}
	
	public int getLikeCount() {
		return likeCount;
	}
	
	public void setLikeCount(int likeCount) {
		this.likeCount = likeCount;
	}
	
	public int getDislikeCount() {
		return dislikeCount;
	}
	
	public void setDislikeCount(int dislikeCount) {
		this.dislikeCount = dislikeCount;
	}
	
	public int getShareCount() {
		return shareCount;
	}
	
	public void setShareCount(int shareCount) {
		this.shareCount = shareCount;
	}
	
	public boolean isLiked() {
		return liked;
	}
	
	public void setLiked(boolean liked) {
		this.liked = liked;
	}
	
	public boolean isDisliked() {
		return disliked;
	}
	
	public void setDisliked(boolean disliked) {
		this.disliked = disliked;
	}
	
	public boolean isShared() {
		return shared;
	}
	
	public void setShared(boolean shared) {
		this.shared = shared;
	}
	
	public AttachmentPreview getAttachmentPreview() {
		return attachmentPreview;
	}
	
	public void setAttachmentPreview(AttachmentPreview attachmentPreview) {
		this.attachmentPreview = attachmentPreview;
	}
	
	public List<SocialActivityCommentData> getComments() {
		return comments;
	}
	
	public void setComments(List<SocialActivityCommentData> comments) {
		this.comments = comments;
	}
	
	public void addComment(SocialActivityCommentData commentData) {
		comments.add(commentData);
	}
	
	public String getNewComment() {
		return newComment;
	}
	
	public void setNewComment(String newComment) {
		this.newComment = newComment;
	}
	
	public String getMentionedUsersInComment() {
		return mentionedUsersInComment;
	}
	
	public void setMentionedUsersInComment(String mentionedUsersInComment) {
		this.mentionedUsersInComment = mentionedUsersInComment;
	}
	
	public VisibilityType getVisibility() {
		return visibility;
	}
	
	public void setVisibility(VisibilityType visibility) {
		this.visibility = visibility;
	}
	
	public String getWhen() {
		if(lastAction!=null){
			return DateUtil.getTimeAgoFromNow(lastAction);
		}else
			return "";
		
		
	}

	@Override
	public String toString() {
		return "SocialActivityData1 [dateCreated=" + dateCreated + ", text=" + text + ", socialActivity=" + socialActivity + ", actor=" + actor
				+ ", anonUser=" + anonUser + ", maker=" + maker + ", object=" + object + ", actionName=" + actionName + ", action=" + action
				+ ", target=" + target + ", targetActor=" + targetActor + ", relationToTarget=" + relationToTarget + ", hashtags=" + hashtags
				+ ", notificationId=" + configId + ", commentsDisabled=" + commentsDisabled + ", unfollowedHashtags=" + unfollowedHashtags
				+ ", userCanBeUnfollowed=" + userCanBeUnfollowed + ", showHiddenComments=" + showHiddenComments + ", shareable=" + shareable
				+ ", subViewType=" + subViewType + ", likeCount=" + likeCount + ", dislikeCount=" + dislikeCount + ", shareCount=" + shareCount
				+ ", liked=" + liked + ", disliked=" + disliked + ", shared=" + shared + ", attachmentPreview=" + attachmentPreview + ", comments="
				+ comments + ", newComment=" + newComment + ", mentionedUsersInComment=" + mentionedUsersInComment + ", visibility=" + visibility
				+ "]";
	}

}
