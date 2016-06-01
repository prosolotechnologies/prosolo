package org.prosolo.services.nodes.data.instructor;

import java.util.Date;
import java.util.Optional;

import org.prosolo.services.common.observable.StandardObservable;
import org.prosolo.services.nodes.data.UserData;

public class InstructorData extends StandardObservable {

	private UserData user;
	private long instructorId;
	private int maxNumberOfStudents;
	private int numberOfAssignedStudents;
	private Date dateAssigned;
	
	public InstructorData(boolean listenChanges) {
		this.listenChanges = listenChanges;
	}
	
	public void setMaxNumberOfStudentsToOriginalValue() {
		Optional<Integer> res = getMaxNumberOfStudentsBeforeUpdate();
		if(res.isPresent()) {
			this.maxNumberOfStudents = res.get();
		}
	}
	
	public boolean isFull() {
		if(maxNumberOfStudents == 0) {
			return false;
		}
		return numberOfAssignedStudents == maxNumberOfStudents;
	}
	
	public String getMaxNumberOfStudentsString() {
		if(maxNumberOfStudents == 0) {
			return "unlimited";
		}
			return maxNumberOfStudents + "";
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
	
	public long getInstructorId() {
		return instructorId;
	}


	public void setInstructorId(long instructorId) {
		this.instructorId = instructorId;
	}

	public int getMaxNumberOfStudents() {
		return maxNumberOfStudents;
	}

	public void setMaxNumberOfStudents(int maxNumberOfStudents) {
		observeAttributeChange("maxNumberOfStudents", this.maxNumberOfStudents, maxNumberOfStudents);
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

	public Date getDateAssigned() {
		return dateAssigned;
	}

	public void setDateAssigned(Date dateAssigned) {
		this.dateAssigned = dateAssigned;
	}
	
	public Optional<Integer> getMaxNumberOfStudentsBeforeUpdate() {
		Integer no = (Integer) changedAttributes.get("maxNumberOfStudents");
		if(no == null) {
			return Optional.empty();
		} else {
			return Optional.of(no);
		}
	}

}
