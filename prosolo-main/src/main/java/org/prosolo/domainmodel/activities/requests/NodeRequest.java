package org.prosolo.domainmodel.activities.requests;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.domainmodel.activities.requests.Request;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.general.Node;

@Entity
public class NodeRequest extends Request {

	private static final long serialVersionUID = 1540460086117377554L;
	
	private Node nodeResource;
	
	// resource created as a result of accepting the request
	private Node resolutionResource;
	
	@OneToOne
	public Node getNodeResource() {
		return nodeResource;
	}

	public void setNodeResource(Node nodeResource) {
		this.nodeResource = nodeResource;
	}

	@Override
	@Transient
	public BaseEntity getResource() {
		return nodeResource;
	}

	@Override
	public void setResource(BaseEntity resource) {
		this.nodeResource = (Node) resource;
	}

	@OneToOne
	public Node getResolutionResource() {
		return resolutionResource;
	}

	public void setResolutionResource(Node resolutionResource) {
		this.resolutionResource = resolutionResource;
	}
	
}
