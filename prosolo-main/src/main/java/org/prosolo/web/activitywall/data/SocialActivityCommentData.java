/**
 * 
 */
package org.prosolo.web.activitywall.data;

import java.io.Serializable;
import java.util.Date;

import org.prosolo.common.domainmodel.activitywall.comments.Comment;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.activityWall.impl.data.SocialActivityData;
import org.prosolo.util.date.DateUtil;

/**
 * @author "Nikola Milikic"
 * 
 */
public class SocialActivityCommentData implements Serializable {

	private static final long serialVersionUID = 8298985822870123069L;

	private long id;
	private String text;
	private UserData maker;
	private Date created;

	private boolean liked;
	private boolean disliked;
	private int likeCount;
	private int dislikeCount;

	private SocialActivityData wallData;

	/**
	 * @param comment
	 * @param liked 
	 * @param likeCount 
	 * @param wallActivity 
	 */
	public SocialActivityCommentData(Comment comment, int likeCount, boolean liked, SocialActivityData wallData) {
		this.id = comment.getId();
		this.text = comment.getText();
		this.maker = new UserData(comment.getMaker());
		this.created = comment.getDateCreated();
		this.likeCount = likeCount;
		this.liked = liked;
		this.wallData = wallData;
	}
	
	public SocialActivityCommentData(String text, User maker, Date created, SocialActivityData wallData) {
		this.text = text;
		this.maker = new UserData(maker);
		this.created = created;
		this.likeCount = 0;
		this.dislikeCount = 0;
		this.wallData = wallData;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public UserData getMaker() {
		return maker;
	}

	public void setMaker(UserData maker) {
		this.maker = maker;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
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

	public SocialActivityData getWallData() {
		return wallData;
	}

	public void setWallData(SocialActivityData wallData) {
		this.wallData = wallData;
	}

	public String getWhen() {
		// always calculating the latest time difference
		return DateUtil.getTimeAgoFromNow(created);
	}

	public boolean isDisliked() {
		return disliked;
	}

	public void setDisliked(boolean disliked) {
		this.disliked = disliked;
	}

	public int getDislikeCount() {
		return dislikeCount;
	}

	public void setDislikeCount(int dislikeCount) {
		this.dislikeCount = dislikeCount;
	}
	
}
