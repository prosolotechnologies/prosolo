package org.prosolo.web.courses.data;

import java.util.Map;

import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.web.util.AvatarUtils;

public class CourseInstructorData {

	private long instructorId;
	private String avatarUrl = "/" + Settings.getInstance().config.services.userService.defaultAvatarPath + "size60x60.png";
	private String name;
	private String position;
	private Integer maxNumberOfStudents;
	private int numberOfAssignedStudents;
	private long userId;
	
	public CourseInstructorData() {
		
	}
	
	public CourseInstructorData(Map<String, Object> instructorMap) {
		this.instructorId = (long) instructorMap.get("instructorId");
		String avatarUrl = (String) instructorMap.get("avatarUrl");
		User user = new User();
		user.setAvatarUrl(avatarUrl);
		this.avatarUrl = AvatarUtils.getAvatarUrlInFormat(user, ImageFormat.size60x60);
		String firstName = (String) instructorMap.get("firstName");
		String lastName = (String) instructorMap.get("lastName");
		this.name = firstName + (lastName != null ? " " + lastName : "");
		this.position = (String) instructorMap.get("position");
		this.maxNumberOfStudents = (int) instructorMap.get("maxNumberOfStudents");
		this.numberOfAssignedStudents = (int) instructorMap.get("numberOfAssignedStudents");
		Long instructorUserId = (Long) instructorMap.get("userId");
		if(instructorUserId != null) {
			this.userId = instructorUserId;
		}
	}
	
	public void setFullName(String name, String lastName) {
		this.name = name + (lastName != null ? " " + lastName : "");
	}

	public boolean isFull() {
		return numberOfAssignedStudents == maxNumberOfStudents;
	}
	
	public long getInstructorId() {
		return instructorId;
	}


	public void setInstructorId(long instructorId) {
		this.instructorId = instructorId;
	}


	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public Integer getMaxNumberOfStudents() {
		return maxNumberOfStudents;
	}

	public void setMaxNumberOfStudents(Integer maxNumberOfStudents) {
		this.maxNumberOfStudents = maxNumberOfStudents;
	}

	public int getNumberOfAssignedStudents() {
		return numberOfAssignedStudents;
	}

	public void setNumberOfAssignedStudents(int numberOfAssignedStudents) {
		this.numberOfAssignedStudents = numberOfAssignedStudents;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}
	
}
