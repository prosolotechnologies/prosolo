package org.prosolo.web.courses.data;

import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.web.util.AvatarUtils;

public class UserData {

	private long id;
	private String name;
	private String lastName;
	private String fullName;
	private String position;
	private String avatarUrl = "/" + Settings.getInstance().config.services.userService.defaultAvatarPath + "size60x60.png";
	private UserData instructor;
	private int courseProgress;
	
	public UserData() {
		
	}
	
	public UserData(User user) {
		this.id = user.getId();
		this.name = user.getName();
		this.lastName = user.getLastname();
		setFullName(user.getName(), user.getLastname());
		this.position = user.getPosition();
		this.avatarUrl = AvatarUtils.getAvatarUrlInFormat(user, ImageFormat.size60x60);
	}
	
	public void setFullName(String name, String lastName) {
		this.fullName = name + (lastName != null ? " " + lastName : "");
	}

	public UserData(User user, User instructor, int progress) {
		this(user);
		if(instructor != null) {
			this.instructor = new UserData();
			this.instructor.setFullName(instructor.getName() + (instructor.getLastname() != null ? " " + 
					instructor.getLastname() : ""));
			this.instructor.id = instructor.getId();
		}
		this.courseProgress = progress;
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

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public UserData getInstructor() {
		return instructor;
	}

	public void setInstructor(UserData instructor) {
		this.instructor = instructor;
	}

	public int getCourseProgress() {
		return courseProgress;
	}

	public void setCourseProgress(int courseProgress) {
		this.courseProgress = courseProgress;
	}
	
	
}
