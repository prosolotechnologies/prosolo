package org.prosolo.services.activityWall.impl.data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.interaction.data.CommentsData;
import org.prosolo.services.nodes.data.activity.attachmentPreview.AttachmentPreview1;

public class SocialActivityData1 implements Serializable {
	
	private static final long serialVersionUID = 3165333943660060147L;
	
	private long id;
	private Date dateCreated;
	private Date lastAction;
	private SocialActivityType type;
	/*
	 * next group of fields together form post text that will be shown
	 */
	private org.prosolo.services.nodes.data.UserData actor;
	private String predicate;
	private ObjectData object;
	private String relationToTarget;
	private ObjectData target;
	private String text;
	private AttachmentPreview1 attachmentPreview;
	
	private SocialActivityData1 originalSocialActivity;
	
	private List<String> hashtags;
	
	private boolean commentsDisabled;
	
	private int likeCount;
	private boolean liked;
	private boolean shared;
	
	private CommentsData comments;
	private String newComment;
	private String mentionedUsersInComment;
	
	public SocialActivityData1() {
		attachmentPreview = new AttachmentPreview1();
	}
	
	public String getDatePretty() {
		return DateUtil.getTimeAgoFromNow(dateCreated);
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

	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public org.prosolo.services.nodes.data.UserData getActor() {
		return actor;
	}
	
	public void setActor(org.prosolo.services.nodes.data.UserData actor) {
		this.actor = actor;
	}
	
	public List<String> getHashtags() {
		return hashtags;
	}
	
	public void setHashtags(List<String> hashtags) {
		this.hashtags = hashtags;
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
	
	public int getLikeCount() {
		return likeCount;
	}
	
	public void setLikeCount(int likeCount) {
		this.likeCount = likeCount;
	}
	
	public boolean isLiked() {
		return liked;
	}
	
	public void setLiked(boolean liked) {
		this.liked = liked;
	}
	
	public boolean isShared() {
		return shared;
	}
	
	public void setShared(boolean shared) {
		this.shared = shared;
	}
	
	public AttachmentPreview1 getAttachmentPreview() {
		return attachmentPreview;
	}
	
	public void setAttachmentPreview(AttachmentPreview1 attachmentPreview) {
		this.attachmentPreview = attachmentPreview;
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

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public ObjectData getObject() {
		return object;
	}

	public void setObject(ObjectData object) {
		this.object = object;
	}

	public CommentsData getComments() {
		return comments;
	}

	public void setComments(CommentsData comments) {
		this.comments = comments;
	}

	public ObjectData getTarget() {
		return target;
	}

	public void setTarget(ObjectData target) {
		this.target = target;
	}

	public String getRelationToTarget() {
		return relationToTarget;
	}

	public void setRelationToTarget(String relationToTarget) {
		this.relationToTarget = relationToTarget;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public SocialActivityType getType() {
		return type;
	}

	public void setType(SocialActivityType type) {
		this.type = type;
	}

	public SocialActivityData1 getOriginalSocialActivity() {
		return originalSocialActivity;
	}

	public void setOriginalSocialActivity(SocialActivityData1 originalSocialActivity) {
		this.originalSocialActivity = originalSocialActivity;
	}

}
