package org.prosolo.web.lti.data;

import java.io.Serializable;

import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.common.domainmodel.lti.ResourceType;
import org.prosolo.services.nodes.data.activity.attachmentPreview.NodeData;

/**
 * @author Nikola Milikic
 * @version 0.5
 *			
 */
public class ExternalToolData implements Serializable {
	
	private static final long serialVersionUID = -3099994321890833951L;

	private long id;
	private String title;
	private boolean enabled;
	private NodeData resource;
	private ResourceType resType;
	
	public ExternalToolData(long id, String title, boolean enabled, NodeData resource) {
		this.id = id;
		this.title = title;
	    this.enabled = enabled;
		this.resource = resource;
	}
	
	public ExternalToolData(LtiTool tool){
		id = tool.getId();
		title = tool.getName();
		enabled = tool.isEnabled();
		resType = tool.getToolType();
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
	

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public NodeData getResource() {
		return resource;
	}

	public void setResource(NodeData resource) {
		this.resource = resource;
	}

	public ResourceType getResType() {
		return resType;
	}

	public void setResType(ResourceType resType) {
		this.resType = resType;
	}
	

}
