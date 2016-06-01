package org.prosolo.services.nodes.data.instructor;

import java.util.ArrayList;
import java.util.List;

public class StudentAssignData {

	private List<StudentInstructorPair> assigned;
	private List<Long> unassigned;
	
	public StudentAssignData() {
		assigned = new ArrayList<>();
		unassigned = new ArrayList<>();
	}

	public StudentAssignData(List<StudentInstructorPair> assigned, List<Long> unassigned) {
		this.assigned = assigned;
		this.unassigned = unassigned;
	}

	public void addAssignedPair(StudentInstructorPair pair) {
		assigned.add(pair);
	}
	
	public void addUnassignedId(long id) {
		unassigned.add(id);
	}
	
	public List<StudentInstructorPair> getAssigned() {
		return assigned;
	}

	public void setAssigned(List<StudentInstructorPair> assigned) {
		this.assigned = assigned;
	}

	public List<Long> getUnassigned() {
		return unassigned;
	}

	public void setUnassigned(List<Long> unassigned) {
		this.unassigned = unassigned;
	}
	
}
