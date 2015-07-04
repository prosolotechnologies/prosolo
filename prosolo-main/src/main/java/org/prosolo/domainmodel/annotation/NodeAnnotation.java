package org.prosolo.domainmodel.annotation;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.domainmodel.annotation.Annotation;
import org.prosolo.domainmodel.annotation.AnnotationType;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.general.Node;

/**
@author Zoran Jeremic Jan 1, 2014
 */
@Entity
public class NodeAnnotation extends Annotation {
	
	private static final long serialVersionUID = 6052385565833566454L;

	private Node node;
	
	public NodeAnnotation(AnnotationType ann) {
		super(ann);
	}
	
	public NodeAnnotation(){}

	@Transient
	@Override
	public BaseEntity getResource() {
		return this.node;
	}

	@Override
	public void setResource(BaseEntity resource) {
		this.node = (Node) resource;
	}
	
	@OneToOne
	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

}
