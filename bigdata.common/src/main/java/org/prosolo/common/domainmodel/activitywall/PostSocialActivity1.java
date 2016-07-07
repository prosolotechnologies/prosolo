package org.prosolo.common.domainmodel.activitywall;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.content.RichContent1;

@Entity
public class PostSocialActivity1 extends SocialActivity1 {

	private static final long serialVersionUID = 7157471416779853233L;
	
	private Set<Tag> hashtags;
	private int shareCount;
	private RichContent1 richContent;

	public PostSocialActivity1() {
		hashtags = new HashSet<Tag>();
	}
	
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name="title", column=@Column(name="rich_content_title")),
		@AttributeOverride(name="description", column=@Column(name="rich_content_description", length=9000)),
	    @AttributeOverride(name="link", column=@Column(name="rich_content_link")),
	    @AttributeOverride(name="embedId", column=@Column(name="rich_content_embed_id")),
	    @AttributeOverride(name="imageUrl", column=@Column(name="rich_content_image_url")),
	    @AttributeOverride(name="lastIndexingDate", column=@Column(name="rich_content_last_indexing_update")),
	    @AttributeOverride(name="contentType", column=@Column(name="rich_content_content_type")),
	    @AttributeOverride(name="imageSize", column=@Column(name="rich_content_image_size"))
	    })
	public RichContent1 getRichContent() {
		return richContent;
	}

	public void setRichContent(RichContent1 richContent) {
		this.richContent = richContent;
	}
	
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "social_activity_hashtags")
	public Set<Tag> getHashtags() {
		return hashtags;
	}

	public void setHashtags(Set<Tag> hashtags) {
		this.hashtags = hashtags;
	}
	
	public int getShareCount() {
		return shareCount;
	}

	public void setShareCount(int shareCount) {
		this.shareCount = shareCount;
	}
	
}
