package org.prosolo.domainmodel.activitywall;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.domainmodel.activitywall.SocialActivity;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.general.Node;

/**
@author Zoran Jeremic Jan 1, 2014
 */
@Entity
public class NodeSocialActivity extends SocialActivity{

	private static final long serialVersionUID = 271754380822015705L;

	private Node nodeObject;
	private Node nodeTarget;
	
	@Override
	@Transient
	public BaseEntity getObject() {
		return nodeObject;
	}

	@Override
	public void setObject(BaseEntity object) {
		this.nodeObject = (Node) object;
	}

	@OneToOne(fetch = FetchType.LAZY)
	public Node getNodeObject() {
		return nodeObject;
	}

	public void setNodeObject(Node nodeObject) {
		this.nodeObject = nodeObject;
	}

	@Override
	public void setTarget(BaseEntity object) {
		this.nodeTarget = (Node) object;
	}

	@Override
	@Transient
	public BaseEntity getTarget() {
		return nodeTarget;
	}

	@OneToOne
	public Node getNodeTarget() {
		return nodeTarget;
	}

	public void setNodeTarget(Node target) {
		this.nodeTarget = target;
	}

}
