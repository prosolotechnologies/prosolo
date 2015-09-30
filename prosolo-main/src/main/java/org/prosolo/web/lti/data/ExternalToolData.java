package org.prosolo.web.lti.data;

import java.io.Serializable;

import org.prosolo.web.activitywall.data.NodeData;

/**
 * @author Nikola Milikic
 * @version 0.5
 *			
 */
public class ExternalToolData implements Serializable {
	
	private static final long serialVersionUID = -3099994321890833951L;

	private long id;
	private String title;
	private NodeData resource;
	
	public ExternalToolData(long id, String title, NodeData resource) {
		this.id = id;
		this.title = title;
		this.resource = resource;
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public NodeData getResource() {
		return resource;
	}

	public void setResource(NodeData resource) {
		this.resource = resource;
	}

}
