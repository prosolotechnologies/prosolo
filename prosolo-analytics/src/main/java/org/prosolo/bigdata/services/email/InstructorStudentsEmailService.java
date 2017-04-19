package org.prosolo.bigdata.services.email;

import org.prosolo.bigdata.scala.spark.emails.CourseInstructorEmail;

import java.util.List;

public interface InstructorStudentsEmailService {

	void sendEmailToInstructor(long courseId, long instructorId, List<Long> assignedStudents,
			List<Long> unassignedStudents);
	public void sendEmailsToInstructors(List<CourseInstructorEmail> batchEmails);

}