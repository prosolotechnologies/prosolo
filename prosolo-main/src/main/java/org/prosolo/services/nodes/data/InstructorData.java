package org.prosolo.services.nodes.data;

public class InstructorData {

	private UserData user;
	private long instructorId;
	private Integer maxNumberOfStudents;
	private int numberOfAssignedStudents;
	
	public InstructorData() {
		
	}
	
//	public CourseInstructorData(Map<String, Object> instructorMap) {
//		this.instructorId = (long) instructorMap.get("instructorId");
//		String avatarUrl = (String) instructorMap.get("avatarUrl");
//		User user = new User();
//		user.setAvatarUrl(avatarUrl);
//		this.avatarUrl = AvatarUtils.getAvatarUrlInFormat(user, ImageFormat.size60x60);
//		String firstName = (String) instructorMap.get("firstName");
//		String lastName = (String) instructorMap.get("lastName");
//		this.name = firstName + (lastName != null ? " " + lastName : "");
//		this.position = (String) instructorMap.get("position");
//		this.maxNumberOfStudents = (int) instructorMap.get("maxNumberOfStudents");
//		this.numberOfAssignedStudents = (int) instructorMap.get("numberOfAssignedStudents");
//		Long instructorUserId = (Long) instructorMap.get("userId");
//		if(instructorUserId != null) {
//			this.userId = instructorUserId;
//		}
//	}

	public boolean isFull() {
		return numberOfAssignedStudents == maxNumberOfStudents;
	}
	
	public long getInstructorId() {
		return instructorId;
	}


	public void setInstructorId(long instructorId) {
		this.instructorId = instructorId;
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

	public UserData getUser() {
		return user;
	}

	public void setUser(UserData user) {
		this.user = user;
	}

}
