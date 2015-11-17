package org.prosolo.web.students.data.learning;

import java.util.ArrayList;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.web.util.AvatarUtils;

public class EvaluatorData {

	private long id;
	private String name;
	private String avatar;
	
	public EvaluatorData(User user){
		this.id = user.getId();
		this.name = user.getName() + (user.getLastname() != null ? " " + user.getLastname() : "");
		this.avatar = AvatarUtils.getAvatarUrlInFormat(user, ImageFormat.size60x60);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	
	
}