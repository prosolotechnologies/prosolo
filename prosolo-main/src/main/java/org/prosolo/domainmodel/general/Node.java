package org.prosolo.domainmodel.general;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

import org.prosolo.domainmodel.annotation.Tag;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.general.Node;
import org.prosolo.domainmodel.organization.VisibilityType;
import org.prosolo.domainmodel.organization.Visible;
import org.prosolo.domainmodel.user.User;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Node extends BaseEntity implements Comparable<Node>, Visible {
	
	private static final long serialVersionUID = 769615772045191484L;
	
	private VisibilityType visibility;
	private User maker;
	private Set<Tag> tags;
	private Set<Tag> hashtags;
	
	public Node() {
		tags = new HashSet<Tag>();
		hashtags = new HashSet<Tag>();
	}
	
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
	
	@ManyToMany(fetch = FetchType.LAZY)
	public Set<Tag> getTags() {
		return tags;
	}
	
	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}
	
	@ManyToMany(fetch = FetchType.LAZY)
	public Set<Tag> getHashtags() {
		return hashtags;
	}
	
	public void setHashtags(Set<Tag> hashtags) {
		this.hashtags = hashtags;
	}
	
	@Override
	public int compareTo(Node o) {
		return this.getTitle().compareTo(o.getTitle());
	}
	
}
