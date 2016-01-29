package org.prosolo.web.courses.data;

import java.util.Map;

import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.web.util.AvatarUtils;

public class UserData {

	private long id;
	private String name;
	private String lastName;
	private String fullName;
	private String position;
	private String avatarUrl = "/" + CommonSettings.getInstance().config.services.userService.defaultAvatarPath + "size60x60.png";
	private CourseInstructorData instructor;
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
	
	public UserData(User user, Map<String, Object> instructor, int progress, String profileType, String profileTitle) {
		this(user);
		if(instructor != null) {
			this.instructor = new CourseInstructorData();
			this.instructor.setName((String) instructor.get("firstName") + (instructor.get("lastName") != null ? " " + 
					(String) instructor.get("lastName") : ""));
			this.instructor.setInstructorId((long) instructor.get("instructorId"));
		}
		this.courseProgress = progress;
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

	public CourseInstructorData getInstructor() {
		return instructor;
	}

	public void setInstructor(CourseInstructorData instructor) {
		this.instructor = instructor;
	}

	public int getCourseProgress() {
		return courseProgress;
	}

	public void setCourseProgress(int courseProgress) {
		this.courseProgress = courseProgress;
	}
	
	
}
