package org.prosolo.domainmodel.user;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.general.Node;
import org.prosolo.domainmodel.user.FollowedEntity;

/**
 * @author Zoran Jeremic Dec 30, 2013
 */
@Entity
public class FollowedResourceEntity extends FollowedEntity {

	private static final long serialVersionUID = 669453763869324182L;

	private Node followedNode;

	@Transient
	public BaseEntity getFollowedResource() {
		return getFollowedNode();
	}

	@Transient
	public void setFollowedResource(BaseEntity followedResource) {
		this.setFollowedNode((Node) followedResource);
	}

	@OneToOne
	public Node getFollowedNode() {
		return followedNode;
	}

	public void setFollowedNode(Node followedNode) {
		this.followedNode = followedNode;
	}
}
