package org.prosolo.domainmodel.activitywall;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.domainmodel.activitywall.SocialActivity;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.general.Node;
import org.prosolo.domainmodel.user.User;

@Entity
public class NodeUserSocialActivity extends SocialActivity{

	private static final long serialVersionUID = 5542029393842231807L;

	private Node nodeObject;
	private User userTarget;
	
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
	public void setTarget(BaseEntity target) {
		this.userTarget = (User) target;
	}

	@Override
	@Transient
	public BaseEntity getTarget() {
		return userTarget;
	}

	@OneToOne
	public User getUserTarget() {
		return userTarget;
	}

	public void setUserTarget(User userTarget) {
		this.userTarget = userTarget;
	}

}
