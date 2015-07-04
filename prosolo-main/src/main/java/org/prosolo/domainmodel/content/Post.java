package org.prosolo.domainmodel.content;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.organization.VisibilityType;
import org.prosolo.domainmodel.organization.Visible;
import org.prosolo.domainmodel.user.User;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Post extends BaseEntity implements Visible {
	
	private static final long serialVersionUID = 4406665182656107440L;
	
	private User maker;
	private VisibilityType visibility;
	private String content;
	private String link;
	private Post reshareOf;
	private RichContent richContent;
	private Set<User> mentionedUsers;
	private Date updated;
	
	@Enumerated(EnumType.STRING)
	public VisibilityType getVisibility() {
		return visibility;
	}
	
	public void setVisibility(VisibilityType visibility) {
		this.visibility = visibility;
	}
	
	@OneToOne(fetch = FetchType.LAZY)
	public User getMaker() {
		return maker;
	}
	
	public void setMaker(User maker) {
		if (null != maker) {
			this.maker = maker;
		}
	}
	
	@Column(length = 90000)
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		if (null != content) {
			this.content = content;
		}
	}
	
	@Column(length = 700)
	public String getLink() {
		return link;
	}
	
	public void setLink(String link) {
		this.link = link;
	}
	
	@OneToOne
	public Post getReshareOf() {
		return reshareOf;
	}
	
	public void setReshareOf(Post reshareOf) {
		if (null != reshareOf) {
			this.reshareOf = reshareOf;
		}
	}
	
	@OneToOne
	public RichContent getRichContent() {
		return richContent;
	}
	
	public void setRichContent(RichContent richContent) {
		this.richContent = richContent;
	}
	
	@ManyToMany
	public Set<User> getMentionedUsers() {
		return mentionedUsers;
	}
	
	public void setMentionedUsers(Set<User> mentionedUsers) {
		this.mentionedUsers = mentionedUsers;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(length = 19)
	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}
	
}
