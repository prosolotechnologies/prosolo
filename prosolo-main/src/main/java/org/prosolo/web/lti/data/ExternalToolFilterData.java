package org.prosolo.web.lti.data;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.prosolo.common.domainmodel.lti.ResourceType;
import org.prosolo.services.lti.filter.Filter;

/**
 * @author Nikola Milikic
 * @version 0.5
 *			
 */
public class ExternalToolFilterData implements Serializable {
	
	private static final long serialVersionUID = -4063157951149079274L;
	
	private long id;
	private String title;
	private long parentId;
	private ResourceType resType;
	private Filter filter;
	private List<ExternalToolFilterData> children = new LinkedList<ExternalToolFilterData>();
	
	public ExternalToolFilterData(long id, String title, long parentId, ResourceType resType, Filter filter) {
		this.id = id;
		this.title = title;
		this.parentId = parentId;
		this.resType = resType;
		this.filter = filter;
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
	
	public long getParentId() {
		return parentId;
	}

	public void setParentId(long parentId) {
		this.parentId = parentId;
	}

	public ResourceType getResType() {
		return resType;
	}

	public void setResType(ResourceType resType) {
		this.resType = resType;
	}

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public List<ExternalToolFilterData> getChildren() {
		return children;
	}

	public void setChildren(List<ExternalToolFilterData> children) {
		this.children = children;
	}
	
	public void addChild(ExternalToolFilterData child) {
		children.add(child);
	}
	
}
