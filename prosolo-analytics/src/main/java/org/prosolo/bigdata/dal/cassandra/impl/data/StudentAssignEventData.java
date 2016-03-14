package org.prosolo.bigdata.dal.cassandra.impl.data;

public class StudentAssignEventData {

	private long courseId;
	private long instructorId;
	private long studentId;
	private StudentAssign type;
	
	public StudentAssignEventData() {
		
	}

	public StudentAssignEventData(long courseId, long instructorId, long studentId, StudentAssign type) {
		this.courseId = courseId;
		this.instructorId = instructorId;
		this.studentId = studentId;
		this.type = type;
	}

	public long getCourseId() {
		return courseId;
	}

	public void setCourseId(long courseId) {
		this.courseId = courseId;
	}

	public long getInstructorId() {
		return instructorId;
	}

	public void setInstructorId(long instructorId) {
		this.instructorId = instructorId;
	}

	public long getStudentId() {
		return studentId;
	}

	public void setStudentId(long studentId) {
		this.studentId = studentId;
	}

	public StudentAssign getType() {
		return type;
	}

	public void setType(StudentAssign type) {
		this.type = type;
	}
	
}
