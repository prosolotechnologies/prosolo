package org.prosolo.services.nodes.data.instructor;

public class StudentInstructorPair {

	private long targetCredId;
	private InstructorData instructor;
	
	public StudentInstructorPair() {
		
	}
	
	public StudentInstructorPair(long targetCredId, InstructorData instructor) {
		this.targetCredId = targetCredId;
		this.instructor = instructor;
	}

	public long getTargetCredId() {
		return targetCredId;
	}

	public void setTargetCredId(long targetCredId) {
		this.targetCredId = targetCredId;
	}

	public InstructorData getInstructor() {
		return instructor;
	}

	public void setInstructor(InstructorData instructor) {
		this.instructor = instructor;
	}
	
}
