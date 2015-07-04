/**
 * 
 */
package org.prosolo.common.domainmodel.activitywall;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToMany;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.activitywall.SocialStreamSubViewType;

/**
 * @author "Nikola Milikic"
 * 
 */
@Entity
public class SocialStreamSubView extends BaseEntity {

	private static final long serialVersionUID = 7850986230358368776L;

	private SocialStreamSubViewType type;
	private Set<Node> relatedResources;
	private Set<Tag> hashtags;
	
	public SocialStreamSubView() {
		relatedResources = new HashSet<Node>();
		hashtags = new HashSet<Tag>();
	}

	@Enumerated(EnumType.STRING)
	public SocialStreamSubViewType getType() {
		return type;
	}

	public void setType(SocialStreamSubViewType type) {
		this.type = type;
	}

	@ManyToMany
	public Set<Node> getRelatedResources() {
		return relatedResources;
	}

	public void setRelatedResources(Set<Node> relatedResources) {
		this.relatedResources = relatedResources;
	}
	
	public Set<Node> findRelatedResourceOfType(Class<? extends Node> clazz) {
		Set<Node> relatedResourcesOfType = new HashSet<Node>();
		
		for (Node relatedRes : relatedResources) {
			if (relatedRes.getClass().equals(clazz)) {
				relatedResourcesOfType.add(relatedRes);
			}
		}
		return relatedResourcesOfType;
	}

	@ManyToMany
	public Set<Tag> getHashtags() {
		return hashtags;
	}

	public void setHashtags(Set<Tag> hashtags) {
		this.hashtags = hashtags;
	}
	
}
