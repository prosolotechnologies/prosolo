package org.prosolo.bigdata.services.email.instructorEmail.emailGenerator;

import java.util.List;

import org.prosolo.common.email.generators.EmailContentGenerator;


public class InstructorEmailGenerator extends EmailContentGenerator {
	
	@Override
	public String getTemplateName() {
		return "instructor-credential-updates";
	}
	
	@Override
	public String getSubject() {
		return this.courseName + " - assinged/unassigned students";
	}
	
	private String name;
	private String courseName;
	private List<String> assignedStudents;
	private List<String> unassignedStudents;
	
	public InstructorEmailGenerator(String name, String courseName, List<String> assignedStudents,
			List<String> unassignedStudents) {
		this.name = name;
		this.courseName = courseName;
		this.assignedStudents = assignedStudents;
		this.unassignedStudents = unassignedStudents;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public List<String> getAssignedStudents() {
		return assignedStudents;
	}

	public void setAssignedStudents(List<String> assignedStudents) {
		this.assignedStudents = assignedStudents;
	}
	
	public boolean isHasAssignedStudents() {
		return this.assignedStudents != null && !this.assignedStudents.isEmpty();
	}

	public List<String> getUnassignedStudents() {
		return unassignedStudents;
	}

	public void setUnassignedStudents(List<String> unassignedStudents) {
		this.unassignedStudents = unassignedStudents;
	}
	
	public boolean isHasUnassignedStudents() {
		return this.unassignedStudents != null && !this.unassignedStudents.isEmpty();
	}
	
}