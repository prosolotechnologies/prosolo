package org.prosolo.common.domainmodel.activitywall.comments;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.activitywall.comments.Comment;

/**
@author Zoran Jeremic Jan 1, 2014
 */
@Entity
public class NodeComment extends Comment {

	private static final long serialVersionUID = -5622438801049103300L;
	
	private Node commentedNode;
	
	@OneToOne (fetch = FetchType.LAZY)
	public Node getCommentedNode() {
		return commentedNode;
	}
	
	public void setCommentedNode(Node commentedNode) {
		this.commentedNode = commentedNode;
	}
	
	@Transient
	@Override
	public BaseEntity getObject() {
		return commentedNode;
	}

	@Override
	public void setObject(BaseEntity commentedRes) {
		this.commentedNode = (Node) commentedRes;
	}

	@Override
	public void setTarget(BaseEntity object) { }

	@Override
	@Transient
	public BaseEntity getTarget() {
		return null;
	}

}
