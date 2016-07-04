/**
 * 
 */
package org.prosolo.common.domainmodel.activitywall.old;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.activitywall.old.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.old.SocialStreamSubView;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

/**
 * @author "Nikola Milikic"
 *
 */
@Entity
@Deprecated
public class SocialActivityNotification extends BaseEntity {
	
	private static final long serialVersionUID = -8805823423276796583L;

	private boolean hidden = false;
	private boolean liked = false;
	private boolean disliked = false;
	private boolean shared = false;
	private boolean bookmarked = false;;
	private boolean creator = false;
	
	private List<SocialStreamSubView> subViews;
	
	private SocialActivity socialActivity;
	private User user;
	
	public SocialActivityNotification() { 
		subViews = new ArrayList<SocialStreamSubView>();
	}
	
	public SocialActivityNotification(SocialActivity socialActivity, boolean creator) {
		this();
		this.socialActivity = socialActivity;
		this.creator = creator;
		this.setDateCreated(socialActivity.getDateCreated());
	}

	@OneToOne
	@Cascade({CascadeType.MERGE})
	public SocialActivity getSocialActivity() {
		return socialActivity;
	}

	public void setSocialActivity(SocialActivity socialActivity) {
		this.socialActivity = socialActivity;
	}

	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isLiked() {
		return liked;
	}

	public void setLiked(boolean liked) {
		this.liked = liked;
	}
	
	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isDisliked() {
		return disliked;
	}

	public void setDisliked(boolean disliked) {
		this.disliked = disliked;
	}

	@Type(type="true_false")
	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}

	@Type(type="true_false")
	public boolean isBookmarked() {
		return bookmarked;
	}

	public void setBookmarked(boolean bookmarked) {
		this.bookmarked = bookmarked;
	}

	@Type(type="true_false")
	public boolean isCreator() {
		return creator;
	}

	public void setCreator(boolean creator) {
		this.creator = creator;
	}

	@OneToMany
	public List<SocialStreamSubView> getSubViews() {
		return subViews;
	}

	public void setSubViews(List<SocialStreamSubView> subViews) {
		this.subViews = subViews;
	}

	@OneToOne
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
