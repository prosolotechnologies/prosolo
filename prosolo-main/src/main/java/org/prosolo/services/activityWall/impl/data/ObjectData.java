package org.prosolo.services.activityWall.impl.data;

import java.io.Serializable;

import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.services.nodes.data.UserData;

public class ObjectData implements Serializable {

	private static final long serialVersionUID = -5941653331913179781L;
	
	private long id;
	private UserData creator;
	private String title;
	private String shortType;
	private ResourceType type;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public UserData getCreator() {
		return creator;
	}
	public void setCreator(UserData creator) {
		this.creator = creator;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public ResourceType getType() {
		return type;
	}
	public void setType(ResourceType type) {
		this.type = type;
	}
	public String getShortType() {
		return shortType;
	}
	public void setShortType(String shortType) {
		this.shortType = shortType;
	}
	
}
