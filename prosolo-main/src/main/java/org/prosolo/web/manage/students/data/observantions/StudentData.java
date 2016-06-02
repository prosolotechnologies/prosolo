package org.prosolo.web.manage.students.data.observantions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.web.util.AvatarUtils;

public class StudentData {
	
	private String name;
	private String avatar;
	private String position;
	private List<String> interests;
	
	public StudentData(User user) {
		this.name = user.getName() + (user.getLastname() != null ? " " + user.getLastname() : "");
		this.avatar = AvatarUtils.getAvatarUrlInFormat(user, ImageFormat.size120x120);
		this.position = user.getPosition();
	}
	
	public void addInterests(Set<Tag> preferredKeywords) {
		if (interests == null) {
			interests = new ArrayList<>();
		}
		for (Tag t : preferredKeywords) {
			interests.add(t.getTitle());
		}
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
	
	public String getPosition() {
		return position;
	}
	
	public void setPosition(String position) {
		this.position = position;
	}
	
	public List<String> getInterests() {
		return interests;
	}
	
	public void setInterests(List<String> interests) {
		this.interests = interests;
	}
	
}
