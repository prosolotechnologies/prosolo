package org.prosolo.web.courses.data;

import java.util.List;

public class InstructorStudentsData {

	private CourseInstructorData instructor;
	private List<ExtendedUserData> students;
	private List<Long> studentsToAssign;
	
	public InstructorStudentsData() {
		
	}
	
	public InstructorStudentsData(CourseInstructorData instructor, List<ExtendedUserData> students, 
			List<Long> studentsToAssign, int maxNumberOfStudents) {
		this.instructor = instructor;
		this.students = students;
		this.studentsToAssign = studentsToAssign;
	}
	
	public CourseInstructorData getInstructor() {
		return instructor;
	}

	public void setInstructor(CourseInstructorData instructor) {
		this.instructor = instructor;
	}

	public List<ExtendedUserData> getStudents() {
		return students;
	}

	public void setStudents(List<ExtendedUserData> students) {
		this.students = students;
	}

	public List<Long> getStudentsToAssign() {
		return studentsToAssign;
	}

	public void setStudentsToAssign(List<Long> studentsToAssign) {
		this.studentsToAssign = studentsToAssign;
	}

}
