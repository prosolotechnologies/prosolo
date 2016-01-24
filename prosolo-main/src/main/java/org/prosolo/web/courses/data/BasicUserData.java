package org.prosolo.web.courses.data;

import java.util.Map;

import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.web.util.AvatarUtils;

public class BasicUserData {

	private long id;
	private String fullName;
	private String avatarUrl = "/" + Settings.getInstance().config.services.userService.defaultAvatarPath + "size60x60.png";
	private String position;
	private boolean selected;
	
	public BasicUserData() {
		
	}
	
	public BasicUserData(User user) {
		this.id = user.getId();
		setFullName(user.getName(), user.getLastname());
		this.avatarUrl = AvatarUtils.getAvatarUrlInFormat(user, ImageFormat.size60x60);
	}
	
	public BasicUserData(Map<String, Object> userMap) {
		this.id = (long) userMap.get("id");
		String avatarUrl = (String) userMap.get("avatarUrl");
		User user = new User();
		user.setAvatarUrl(avatarUrl);
		this.avatarUrl = AvatarUtils.getAvatarUrlInFormat(user, ImageFormat.size60x60);
		String firstName = (String) userMap.get("firstName");
		String lastName = (String) userMap.get("lastName");
		setFullName(firstName, lastName);
		this.position = (String) userMap.get("position");
	}
	
	public void setFullName(String name, String lastName) {
		this.fullName = name + (lastName != null ? " " + lastName : "");
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
}
