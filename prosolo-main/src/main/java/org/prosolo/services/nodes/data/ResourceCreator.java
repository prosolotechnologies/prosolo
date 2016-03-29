package org.prosolo.services.nodes.data;

public class ResourceCreator {

	private long id;
	private String name;
	private String avatar;
	
	public ResourceCreator() {
		
	}
	
	public ResourceCreator(long creatorId, String creatorName, String creatorAvatar) {
		this.name = creatorName;
		this.id = creatorId;
		this.avatar = creatorAvatar;
	}

	public String getName() {
		return name;
	}
	public void setName(String creatorName) {
		this.name = creatorName;
	}
	public long getId() {
		return id;
	}
	public void setId(long creatorId) {
		this.id = creatorId;
	}
	public String getAvatar() {
		return avatar;
	}
	public void setAvatar(String creatorAvatar) {
		this.avatar = creatorAvatar;
	}
	
}
