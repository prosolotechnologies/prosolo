package org.prosolo.services.nodes.data;

import java.io.Serializable;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.web.util.AvatarUtils;

public class UserData implements Serializable {

	private static final long serialVersionUID = 8668238017709751223L;
	
	private long id;
	private String fullName;
	private String avatarUrl;
	private String position;
	private String email;
	
	public UserData() {}
	
	public UserData(User user) {
		this.id = user.getId();
		setFullName(user.getName(), user.getLastname());
		this.avatarUrl = AvatarUtils.getAvatarUrlInFormat(user.getAvatarUrl(), ImageFormat.size60x60);
		this.position = user.getPosition();
		this.email = user.getEmail();
	}
	
	public UserData(long id, String firstName, String lastName, String avatar, String position,
			String email) {
		this.id = id;
		setFullName(firstName, lastName);
		if(avatar != null) {
			this.avatarUrl = AvatarUtils.getAvatarUrlInFormat(avatar, ImageFormat.size60x60);
		}
		this.position = position;
		this.email = email;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
