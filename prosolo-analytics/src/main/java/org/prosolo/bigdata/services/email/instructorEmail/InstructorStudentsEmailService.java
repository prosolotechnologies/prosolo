package org.prosolo.bigdata.services.email.instructorEmail;

import java.util.List;

public interface InstructorStudentsEmailService {

	void sendEmailToInstructor(long courseId, long instructorId, List<Long> assignedStudents,
			List<Long> unassignedStudents);

}