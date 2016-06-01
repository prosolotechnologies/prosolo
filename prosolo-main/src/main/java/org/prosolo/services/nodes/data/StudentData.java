package org.prosolo.services.nodes.data;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.nodes.data.instructor.InstructorData;

public class StudentData {

	private UserData user;
	private InstructorData instructor;
	private int credProgress;
	private boolean assigned;
	
	public StudentData() {

	}
	
	public StudentData(User user) {
		this.user = new UserData(user);
	}

//	public UserData(User user, Map<String, Object> instructor, int progress, String profileType, String profileTitle) {
//		this(user);
//		if(instructor != null) {
//			this.instructor = new CourseInstructorData();
//			this.instructor.setName((String) instructor.get("firstName") + (instructor.get("lastName") != null ? " " + 
//					(String) instructor.get("lastName") : ""));
//			this.instructor.setInstructorId((long) instructor.get("instructorId"));
//		}
//		this.courseProgress = progress;
//	}

	public InstructorData getInstructor() {
		return instructor;
	}

	public void setInstructor(InstructorData instructor) {
		this.instructor = instructor;
	}

	public int getCredProgress() {
		return credProgress;
	}

	public void setCredProgress(int credProgress) {
		this.credProgress = credProgress;
	}

	public UserData getUser() {
		return user;
	}

	public void setUser(UserData user) {
		this.user = user;
	}

	public boolean isAssigned() {
		return assigned;
	}

	public void setAssigned(boolean assigned) {
		this.assigned = assigned;
	}
	
}
