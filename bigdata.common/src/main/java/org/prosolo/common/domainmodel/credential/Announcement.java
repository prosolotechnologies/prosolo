package org.prosolo.common.domainmodel.credential;

import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

@Entity
public class Announcement extends BaseEntity {

	private static final long serialVersionUID = 1L;
	
	private Credential1 credential;
	private User createdBy;
	private String text;
	private List<SeenAnnouncement> seenAnouncements;
	private AnnouncementPublishMode publishMode;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public Credential1 getCredential() {
		return credential;
	}

	public void setCredential(Credential1 credential) {
		this.credential = credential;
	}

	@OneToMany(mappedBy = "announcement", cascade = CascadeType.REMOVE, orphanRemoval = true)
	@LazyCollection(LazyCollectionOption.EXTRA)
	public List<SeenAnnouncement> getSeenAnouncements() {
		return seenAnouncements;
	}

	public void setSeenAnouncements(List<SeenAnnouncement> seenAnouncements) {
		this.seenAnouncements = seenAnouncements;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	@Column(name="announcement_text",length=5000)
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Enumerated(EnumType.STRING)
	@Column(columnDefinition = "VARCHAR(255) DEFAULT 'ALL_STUDENTS'", nullable = false)
	public AnnouncementPublishMode getPublishMode() {
		return publishMode;
	}

	public void setPublishMode(AnnouncementPublishMode publishMode) {
		this.publishMode = publishMode;
	}
}
