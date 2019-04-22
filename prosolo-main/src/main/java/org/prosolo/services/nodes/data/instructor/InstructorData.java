package org.prosolo.services.nodes.data.instructor;

import java.util.List;
import java.util.Optional;

import org.prosolo.services.common.observable.StandardObservable;
import org.prosolo.services.user.data.UserData;

public class InstructorData extends StandardObservable {

	private UserData user;
	private long instructorId;
	private int maxNumberOfStudents;
	private int numberOfAssignedStudents;
	//ids of students for which instructor has withdrawn
	private List<Long> withdrawList;

	public InstructorData(boolean listenChanges) {
		this.listenChanges = listenChanges;
	}

	public void setMaxNumberOfStudentsToOriginalValue() {
		Optional<Integer> res = getMaxNumberOfStudentsBeforeUpdate();
		if (res.isPresent()) {
			this.maxNumberOfStudents = res.get();
		}
	}
	
	public boolean isFull() {
		if (maxNumberOfStudents == 0) {
			return false;
		}
		return numberOfAssignedStudents == maxNumberOfStudents;
	}

	public String getMaxNumberOfStudentsString() {
		if (maxNumberOfStudents == 0) {
			return "unlimited";
		}
		return maxNumberOfStudents + "";
	}
	

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

	public Optional<Integer> getMaxNumberOfStudentsBeforeUpdate() {
		Integer no = (Integer) changedAttributes.get("maxNumberOfStudents");
		if (no == null) {
			return Optional.empty();
		} else {
			return Optional.of(no);
		}
	}

	public List<Long> getWithdrawList() {
		return withdrawList;
	}

	public void setWithdrawList(List<Long> withdrawList) {
		this.withdrawList = withdrawList;
	}
}
