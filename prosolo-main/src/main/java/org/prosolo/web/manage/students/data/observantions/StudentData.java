package org.prosolo.web.manage.students.data.observantions;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.web.util.AvatarUtils;

public class StudentData {
	private long id;
	private String name;
	private String avatar;
	private String position;
	private String location;
	//private List<String> interests;
	
	public StudentData(User user) {
		this.id = user.getId();
		this.name = user.getName() + (user.getLastname() != null ? " " + user.getLastname() : "");
		this.avatar = AvatarUtils.getAvatarUrlInFormat(user, ImageFormat.size120x120);
		this.position = user.getPosition();
		this.location = user.getLocationName();
	}
	
//	public void addInterests(Set<Tag> preferredKeywords) {
//		if (interests == null) {
//			interests = new ArrayList<>();
//		}
//		for (Tag t : preferredKeywords) {
//			interests.add(t.getTitle());
//		}
//	}
	
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
	
//	public List<String> getInterests() {
//		return interests;
//	}
//	
//	public void setInterests(List<String> interests) {
//		this.interests = interests;
//	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
}
