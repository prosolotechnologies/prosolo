package org.prosolo.common.domainmodel.activitywall;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.content.RichContent;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;

/**
 * @author "Nikola Milikic"
 * 
 */
//@SqlResultSetMapping(
//        name="SocialActivityData",
//        classes={
//                @ConstructorResult(
//                        targetClass=SocialActivityData.class,
//                        columns={
//                                @ColumnResult(name="sa_id", type = BigInteger.class),
//                        }
//                )
//        }
//)

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class SocialActivity extends BaseEntity {

	private static final long serialVersionUID = -7549004676128854583L;

	private String text;
	private RichContent richContent;
	private boolean commentsDisabled;
	private List<SocialActivity> comments;
	private Date lastAction;
	private Set<Tag> hashtags;
	
	private int likeCount;
	private int dislikeCount;
	private int shareCount;
	private int bookmarkCount;
	
	/**
	 * User who has created the event.
	 */
	private User maker;
	
	/**
	 * Type of the event.
	 */
	private EventType action;
	
	/**
	 * User or resource for which the event is created for.
	 */
	private Node reason;
	
	private VisibilityType visibility;
	
	public SocialActivity() {
		comments = new ArrayList<SocialActivity>();
		hashtags = new HashSet<Tag>();
	}
	
	@Column(length = 90000)
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	@OneToOne
	public RichContent getRichContent() {
		return richContent;
	}

	public void setRichContent(RichContent richContent) {
		this.richContent = richContent;
	}

	@Type(type="true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isCommentsDisabled() {
		return commentsDisabled;
	}

	public void setCommentsDisabled(boolean commentsDisabled) {
		this.commentsDisabled = commentsDisabled;
	}

	@OneToMany
	public List<SocialActivity> getComments() {
		return comments;
	}

	public void setComments(List<SocialActivity> comments) {
		this.comments = comments;
	}
	
	public boolean addComment(SocialActivity comment) {
		if (comment != null) {
			return getComments().add(comment);
		}
		return false;
	}
	
	public Date getLastAction() {
		return lastAction;
	}

	public void setLastAction(Date lastAction) {
		this.lastAction = lastAction;
	}
	
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "social_activity_hashtags")
	public Set<Tag> getHashtags() {
		return hashtags;
	}

	public void setHashtags(Set<Tag> hashtags) {
		this.hashtags = hashtags;
	}

	public int getLikeCount() {
		return likeCount;
	}
	
	public int getDislikeCount() {
		return dislikeCount;
	}

	public void setDislikeCount(int dislikeCount) {
		this.dislikeCount = dislikeCount;
	}

	public void setLikeCount(int likeCount) {
		this.likeCount = likeCount;
	}

	public int getShareCount() {
		return shareCount;
	}

	public void setShareCount(int shareCount) {
		this.shareCount = shareCount;
	}

	public int getBookmarkCount() {
		return bookmarkCount;
	}

	public void setBookmarkCount(int bookmarkCount) {
		this.bookmarkCount = bookmarkCount;
	}
	
	@OneToOne(fetch = FetchType.LAZY)
	public User getMaker() {
		return maker;
	}

	public void setMaker(User maker) {
		this.maker = maker;
	}
	
	@Enumerated(EnumType.STRING)
	public EventType getAction() {
		return action;
	}

	public void setAction(EventType action) {
		this.action = action;
	}
	
	@Transient
	public abstract BaseEntity getObject();
	public abstract void setObject(BaseEntity object);
	
	@Transient
	public abstract BaseEntity getTarget();
	public abstract void setTarget(BaseEntity object);

	@OneToOne(fetch = FetchType.LAZY)
	public Node getReason() {
		return reason;
	}

	public void setReason(Node reason) {
		this.reason = reason;
	}

	@Enumerated (EnumType.STRING)
	public VisibilityType getVisibility() {
		return visibility;
	}

	public void setVisibility(VisibilityType visibility) {
		this.visibility = visibility;
	}

	@Override
	public String toString() {
		return "SocialActivity [text=" + text
				+ ", richContent=" + richContent + ", commentsDisabled="
				+ commentsDisabled + ", comments=" + comments + ", lastAction="
				+ lastAction + ", hashtags=" + hashtags + ", likeCount="
				+ likeCount + ", dislikeCount=" + dislikeCount
				+ ", shareCount=" + shareCount + ", bookmarkCount="
				+ bookmarkCount + ", actor=" + maker + ", action=" + action
				+ ", reason=" + reason + ", visibility=" + visibility + "]";
	}
	
}
