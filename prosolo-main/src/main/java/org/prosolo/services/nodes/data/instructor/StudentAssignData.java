package org.prosolo.services.nodes.data.instructor;

import java.util.ArrayList;
import java.util.List;

import org.prosolo.common.domainmodel.credential.TargetCredential1;

public class StudentAssignData {

	private List<StudentInstructorPair> assigned;
	private List<TargetCredential1> unassigned;
	
	public StudentAssignData() {
		assigned = new ArrayList<>();
		unassigned = new ArrayList<>();
	}

	public StudentAssignData(List<StudentInstructorPair> assigned, List<TargetCredential1> unassigned) {
		this.assigned = assigned;
		this.unassigned = unassigned;
	}

	public void addAssignedPair(StudentInstructorPair pair) {
		assigned.add(pair);
	}
	
	public void addUnassigned(TargetCredential1 tc) {
		unassigned.add(tc);
	}
	
	public List<StudentInstructorPair> getAssigned() {
		return assigned;
	}

	public void setAssigned(List<StudentInstructorPair> assigned) {
		this.assigned = assigned;
	}

	public List<TargetCredential1> getUnassigned() {
		return unassigned;
	}

	public void setUnassigned(List<TargetCredential1> unassigned) {
		this.unassigned = unassigned;
	}
	
}
