package org.prosolo.web.lti.data;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

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
	private List<ExternalToolFilterData> children = new LinkedList<ExternalToolFilterData>();
	
	public ExternalToolFilterData(long id, String title, long parentId) {
		this.id = id;
		this.title = title;
		this.parentId = parentId;
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
